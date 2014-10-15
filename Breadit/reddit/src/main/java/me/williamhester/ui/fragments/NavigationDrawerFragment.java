package me.williamhester.ui.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SettingsActivity;

;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends AccountFragment {

    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private final ArrayList<String> mSubredditList = new ArrayList<>();
    private ArrayAdapter<String> mSubredditArrayAdapter;

    private boolean mIsOpen = false;
    private Context mContext;
    private CheckBox mCheckbox;
    private Subreddit mSubreddit;

    public static NavigationDrawerFragment newInstance() {
        return newInstance(null);
    }

    public static NavigationDrawerFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString("subreddit", subreddit);
        NavigationDrawerFragment fragment = new NavigationDrawerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
            mContext = activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerLayout = (DrawerLayout) container.getRootView().findViewById(R.id.drawer_layout);
        mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
        ListView drawerListView = (ListView) v.findViewById(R.id.list);
        drawerListView.addHeaderView(createHeaderView(inflater));
        drawerListView.setAdapter(mSubredditArrayAdapter);
        loadSubreddits();

        drawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectItem(i - 2 >= 0 ? mSubredditList.get(i - 2) : null);
            }
        });

        ActionBar actionBar = getActivity().getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
//
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
                R.drawable.ic_drawer,             /* nav drawer image to replace 'Up' caret */
                R.string.navigation_drawer_open,  /* "open drawer" description for accessibility */
                R.string.navigation_drawer_close  /* "close drawer" description for accessibility */
        ) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                mIsOpen = false;
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                mIsOpen = true;
                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }
        };

        // Defer code dependent on restoration of previous instance state.
        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);

        return v;
    }

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.header_drawer, null);
        final EditText subredditSearch = (EditText) v.findViewById(R.id.search_subreddit);
        final ImageButton search = (ImageButton) v.findViewById(R.id.search_button);
        ImageButton clear = (ImageButton) v.findViewById(R.id.clear);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                selectItem(subredditSearch.getText().toString().trim());
            }
        });
        subredditSearch.setImeActionLabel(getResources().getString(R.string.go),
                EditorInfo.IME_ACTION_GO);
        subredditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    selectItem(subredditSearch.getText().toString().trim().replace(" ", ""));
                }
                return false;
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subredditSearch.setText("");
            }
        });

        Spinner accountSpinner = (Spinner) v.findViewById(R.id.accounts_spinner);
        accountSpinner.setAdapter(new AccountAdapter());
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == AccountManager.getAccounts().size()) {
                    if (AccountManager.getAccount() != null) {
                        AccountManager.setAccount(null);
                        mCallbacks.onAccountChanged();
                        onAccountChanged();
                    }
                } else {
                    Account a = AccountManager.getAccounts().get(i);
                    if (!a.equals(AccountManager.getAccount())) {
                        AccountManager.setAccount(AccountManager.getAccounts().get(i));
                        mCallbacks.onAccountChanged();
                        onAccountChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        selectCurrentAccount(v);
        View unread = v.findViewById(R.id.messages);
        unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Do nothing for now
            }
        });
        TextView unreadMessages = (TextView) v.findViewById(R.id.unread_count);
        unreadMessages.setText("0 Unread Messages");

        View settings = v.findViewById(R.id.preferences);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                Bundle b = new Bundle();
                i.putExtras(b);
                startActivity(i);
            }
        });
        mCheckbox = (CheckBox) v.findViewById(R.id.subscribed_checkbox);
        mCheckbox.setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSubreddits();
    }

    @Override
    public void onAccountChanged() {
        super.onAccountChanged();
        selectCurrentAccount(getView());
        loadSubreddits();
    }

    private void selectCurrentAccount(View v) {
        if (v != null) {
            Spinner accountSpinner = (Spinner) v.findViewById(R.id.accounts_spinner);
            if (AccountManager.getAccount() == null) {
                accountSpinner.setSelection(AccountManager.getAccounts().size());
            } else {
                accountSpinner.setSelection(AccountManager.getAccounts().indexOf(AccountManager.getAccount()));
            }
        }
    }

    private void loadSubreddits() {
        mSubredditList.clear();
        if (mAccount != null) {
            mSubredditList.addAll(mAccount.getSubreddits());
            Collections.sort(mSubredditList, String.CASE_INSENSITIVE_ORDER);
            new GetUserSubreddits().execute();
        } else {
            String[] subs = getResources().getStringArray(R.array.default_subreddits);
            Collections.addAll(mSubredditList, subs);
        }
        mSubredditArrayAdapter.notifyDataSetChanged();
    }

    private void selectItem(String subreddit) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        if (mCallbacks != null) {
            mCallbacks.onNavigationDrawerItemSelected(subreddit);
        }
    }

    public void setSubreddit(String subreddit) {
        RedditApi.getSubredditDetails(getActivity(), subreddit, new FutureCallback<Subreddit>() {
            @Override
            public void onCompleted(Exception e, Subreddit result) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                mSubreddit = result;
                if (mCheckbox != null) {
                    if (mSubreddit != null) {
                        mCheckbox.setChecked(mSubreddit.userIsSubscriber());
                        mCheckbox.setEnabled(true);
                    } else {
                        mCheckbox.setChecked(false);
                        mCheckbox.setEnabled(false);
                    }
                }
            }
        });
        if (!AccountManager.isLoggedIn()) {
            mCheckbox.setVisibility(View.GONE);
        } else if (subreddit == null || subreddit.equals("")) {
            mCheckbox.setVisibility(View.GONE);
        } else {
            mCheckbox.setVisibility(View.VISIBLE);
        }
        mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, final boolean b) {
                final Subreddit subreddit1 = mSubreddit;
                if (b != subreddit1.userIsSubscriber()) { // Only need to call this if one has changed
                    RedditApi.subscribeSubreddit(getActivity(), b, subreddit1, new FutureCallback<String>() {
                        @Override
                        public void onCompleted(Exception e, String result) {
                            boolean contains = mSubredditList.contains(subreddit1.getTitle());
                            if (b && !contains) {
                                mSubredditList.add(subreddit1.getDisplayName());
                                Collections.sort(mSubredditList, String.CASE_INSENSITIVE_ORDER);
                                mSubredditArrayAdapter.notifyDataSetChanged();
                            } else if (contains) {
                                mSubredditList.remove(subreddit1.getDisplayName());
                                mSubredditArrayAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Forward the new configuration the drawer toggle component.
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    public boolean isOpen() {
        return mIsOpen;
    }

    public void toggle() {
        if (mIsOpen)
            mDrawerLayout.closeDrawer(Gravity.START);
        else
            mDrawerLayout.openDrawer(Gravity.START);
    }

    public static interface NavigationDrawerCallbacks {
        public void onNavigationDrawerItemSelected(String subreddit);
        public void onAccountChanged();
    }

    private class GetUserSubreddits extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                List<Subreddit> subreddits = mAccount.getSubscribedSubreddits();
                List<String> newSubs = new ArrayList<String>();
                for (Subreddit s : subreddits) {
                    newSubs.add(s.getDisplayName());
                }
                Boolean isNew = !newSubs.equals(mSubredditList);
                if (isNew) {
                    mAccount.setSubreddits(newSubs);
                    AccountDataSource dataSource = new AccountDataSource(mContext);
                    try {
                        dataSource.open();
                        dataSource.setSubredditList(mAccount);
                        dataSource.close();
                    } catch (NullPointerException e) {
                        Log.e("Breadit", "Error accessing SQLite database");
                    }
                }
                return isNew;
            } catch (IOException e) {
                e.printStackTrace();
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isNew) {
            if (isNew) {
                Collections.sort(mSubredditList, String.CASE_INSENSITIVE_ORDER);
                mSubredditArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    private class SubredditAdapter extends ArrayAdapter<String> {

        public SubredditAdapter(List<String> items) {
            super(mContext, R.layout.list_item_subreddit, R.id.subreddit_list_item_title, items);
        }

        @Override
        public String getItem(int position) {
            if (position == 0) {
                return getResources().getString(R.string.front_page);
            } else {
                return super.getItem(position - 1);
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + 1; // Have to account for the "Front Page" option
        }
    }

    private class AccountAdapter extends ArrayAdapter<String> {

        public AccountAdapter() {
            super(mContext, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
        }

        @Override
        public String getItem(int position) {
            if (position == AccountManager.getAccounts().size()) {
                return getResources().getString(R.string.logged_out);
            } else {
                return AccountManager.getAccounts().get(position).getUsername();
            }
        }

        @Override
        public int getCount() {
            return AccountManager.getAccounts().size() + 1; // Have to account for the "Not Logged In" option
        }
    }
}
