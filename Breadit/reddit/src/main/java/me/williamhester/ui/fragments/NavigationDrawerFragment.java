package me.williamhester.ui.fragments;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
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

import com.google.gson.JsonParseException;
import com.koushikdutta.async.future.FutureCallback;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.AccountManager;
import me.williamhester.models.RedditLive;
import me.williamhester.models.Submission;
import me.williamhester.models.Subreddit;
import me.williamhester.models.utils.Utilities;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends AccountFragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * A pointer to the current callbacks instance (the Activity).
     */
    private NavigationDrawerCallbacks mCallbacks;

    /**
     * Helper component that ties the action bar to the navigation drawer.
     */
    private ActionBarDrawerToggle mDrawerToggle;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private List<String> mSubredditList;
    private ArrayAdapter<String> mSubredditArrayAdapter;

    private int mCurrentSelectedPosition = 0;
    private boolean mIsOpen = false;
    private Context mContext;
    private CheckBox mCheckbox;
    private Subreddit mSubreddit;
    private String mSubName;

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mSubName = getArguments().getString("subreddit");
            if (mSubName == null) {
                mSubName = "";
            }
        }

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        // Indicate that this fragment would like to influence the set of actions in the action bar.
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        mDrawerLayout = (DrawerLayout) container.getRootView().findViewById(R.id.drawer_layout);
        mDrawerListView = (ListView) v.findViewById(R.id.list);
        View headerView = createHeaderView(inflater);
        mDrawerListView.addHeaderView(headerView);
        // make default alphabetical
        if (mAccount != null) {
            mSubredditList = mAccount.getSubreddits();
            Collections.sort(mSubredditList, String.CASE_INSENSITIVE_ORDER);
            mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
            mDrawerListView.setAdapter(mSubredditArrayAdapter);
        } else {
            mSubredditList = new ArrayList<>();
            String[] subs = getResources().getStringArray(R.array.default_subreddits);
            for (String s : subs) {
                mSubredditList.add(s);
            }
            mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
            mDrawerListView.setAdapter(mSubredditArrayAdapter);
        }

        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                selectItem(mSubredditList.get(i - 1));
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
        mCheckbox = (CheckBox) v.findViewById(R.id.subscribed_CheckBox);
        mCheckbox.setVisibility(View.GONE);

        TextView frontpage = (TextView) v.findViewById(R.id.subreddit_list_item_title);
        frontpage.setText("Front Page");
        frontpage.setBackgroundResource(R.drawable.breadit_activated_background_holo_light);
        frontpage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                selectItem("");
            }
        });
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mAccount != null) {
            mSubredditList = mAccount.getSubreddits();
            Collections.sort(mSubredditList, String.CASE_INSENSITIVE_ORDER);
            mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
            mDrawerListView.setAdapter(mSubredditArrayAdapter);
            new GetUserSubreddits().execute();
        } else {
            mSubredditList = new ArrayList<String>();
            String[] subs = getResources().getStringArray(R.array.default_subreddits);
            for (String s : subs) {
                mSubredditList.add(s);
            }
            mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
            mDrawerListView.setAdapter(mSubredditArrayAdapter);
        }
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
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

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(String subreddit);
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
            super(mContext, 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.list_item_subreddit, parent, false);
            }
            if (position == mDrawerListView.getSelectedItemPosition())
                v.setBackgroundColor(getResources().getColor(R.color.auburn_orange));
            String subreddit = mSubredditList.get(position);
            TextView subredditName = (TextView)v.findViewById(R.id.subreddit_list_item_title);
            subredditName.setText(subreddit);
            return v;
        }
    }
}
