package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.TextUtils;
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
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SettingsActivity;

/**
 * The fragment that contains the user's subreddits and info.
 */
public class NavigationDrawerFragment extends AccountFragment {

    private NavigationDrawerCallbacks mCallbacks;
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private final ArrayList<Subreddit> mSubredditList = new ArrayList<>();
    private BaseAdapter mSubredditArrayAdapter;

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
                selectItem(i - 2 >= 0 ? mSubredditList.get(i - 2).getDisplayName() : null);
            }
        });

        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),                    /* host Activity */
                mDrawerLayout,                    /* DrawerLayout object */
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
        mCheckbox.setVisibility(mAccount == null || mSubreddit == null ? View.GONE : View.VISIBLE);
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
        if (getView() != null) {
            ListView listView = (ListView) getView().findViewById(R.id.list);
            if (mAccount != null) {
                mSubredditList.clear();
                AccountDataSource dataSource = new AccountDataSource(mContext);
                dataSource.open();
                mSubredditList.addAll(dataSource.getCurrentAccountSubreddits());
                dataSource.close();
                HashMap<String, Subreddit> subscriptions = AccountManager.getAccount().getSubscriptions();
                for (Subreddit s : mSubredditList) {
                    subscriptions.put(s.getDisplayName().toLowerCase(), s);
                }
                Collections.sort(mSubredditList);
                RedditApi.getSubscribedSubreddits(new FutureCallback<ArrayList<Subreddit>>() {
                    @Override
                    public void onCompleted(Exception e, ArrayList<Subreddit> result) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        AccountDataSource dataSource = new AccountDataSource(mContext);
                        dataSource.open();
                        ArrayList<Subreddit> allSubs = dataSource.getAllSubreddits();
                        ArrayList<Subreddit> savedSubscriptions = dataSource.getCurrentAccountSubreddits();

                        for (Subreddit s : result) {
                            int index = allSubs.indexOf(s); // Get the subreddit WITH the table id
                            if (index < 0) { // if it doesn't exist, create one with a table id
                                dataSource.addSubreddit(s);
                                dataSource.addSubscriptionToCurrentAccount(s);
                            } else if (!savedSubscriptions.contains(s)) {
                                dataSource.addSubscriptionToCurrentAccount(allSubs.get(index));
                            }
                        }

                        dataSource.close();

                        final boolean isNew = result.equals(savedSubscriptions);

                        if (isNew) {
                            mSubredditList.clear();
                            mSubredditList.addAll(result);
                        }

                        if (getView() != null) {
                            getView().post(new Runnable() {
                                @Override
                                public void run() {
                                    if (isNew) {
                                        Collections.sort(mSubredditList);
                                        mSubredditArrayAdapter.notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    }
                });
                if (listView.getAdapter() != mSubredditArrayAdapter) {
                    listView.setAdapter(mSubredditArrayAdapter);
                }
                mSubredditArrayAdapter.notifyDataSetChanged();
            } else {
                String[] subs = getResources().getStringArray(R.array.default_subreddits);
                listView.setAdapter(new SubredditStringAdapter(subs));
            }
        }
    }

    private void selectItem(String subreddit) {
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        if (mCallbacks != null) {
            mCallbacks.onSubredditSelected(subreddit);
        }
    }

    public void setSubreddit(String subreddit) {
        if (TextUtils.isEmpty(subreddit)) {
            mSubreddit = null;
            mCheckbox.setVisibility(View.GONE);
        } else {
            mCheckbox.setVisibility(View.VISIBLE);
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
                                    mSubredditList.add(subreddit1);
                                    Collections.sort(mSubredditList);
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
        public void onSubredditSelected(String subreddit);
        public void onAccountChanged();
    }

    private class SubredditAdapter extends ArrayAdapter<Subreddit> {

        public SubredditAdapter(List<Subreddit> items) {
            super(mContext, R.layout.list_item_subreddit, R.id.subreddit_list_item_title, items);
        }
        @Override
        public View getView (int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                LayoutInflater inflater =
                        (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_subreddit, parent, false);
            }
            TextView text = (TextView) convertView.findViewById(R.id.subreddit_list_item_title);
            text.setText(getItem(position) == null ? getResources().getString(R.string.front_page) : getItem(position).getDisplayName());
            convertView.findViewById(R.id.mod_indicator).setVisibility(getItem(position) != null
                    && getItem(position).userIsModerator() ? View.VISIBLE : View.GONE);

            return convertView;
        }

        @Override
        public Subreddit getItem(int position) {
            if (position == 0) {
                return null;
            } else {
                return super.getItem(position - 1);
            }
        }

        @Override
        public int getCount() {
            return super.getCount() + 1; // Have to account for the "Front Page" option
        }
    }

    private class SubredditStringAdapter extends ArrayAdapter<String> {

        public SubredditStringAdapter(String[] items) {
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
