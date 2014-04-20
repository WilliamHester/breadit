package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import android.widget.Toast;

import com.google.gson.JsonParseException;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

;import me.williamhester.areddit.Account;
import me.williamhester.areddit.Submission;
import me.williamhester.areddit.Subreddit;
import me.williamhester.areddit.utils.Utilities;

/**
 * Fragment used for managing interactions for and presentation of a navigation drawer.
 * See the <a href="https://developer.android.com/design/patterns/navigation-drawer.html#Interaction">
 * design guidelines</a> for a complete explanation of the behaviors implemented here.
 */
public class NavigationDrawerFragment extends Fragment {

    /**
     * Remember the position of the selected item.
     */
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";

    /**
     * Per the design guidelines, you should show the drawer on launch until the user manually
     * expands it. This shared preference tracks this.
     */
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";

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
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;
    private boolean mSubredditIsLoading = false;
    private Account mAccount;
    private TextView mCurrentSubreddit;
    private CheckBox mCheckbox;
    private Subreddit mSubreddit;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccount = getArguments().getParcelable("account");
        }
        // Read in the flag indicating whether or not the user has demonstrated awareness of the
        // drawer. See PREF_USER_LEARNED_DRAWER for details.
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
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
        View mHeaderView = createHeaderView(inflater);
        mDrawerListView.addHeaderView(mHeaderView);
        // make default alphabetical
        if (mAccount != null) {
            mSubredditList = mAccount.getSubreddits();
            Collections.sort(mSubredditList, mOrderList);
            mSubredditList.add(0, "FrontPage");
            mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
            mDrawerListView.setAdapter(mSubredditArrayAdapter);
            new GetUserSubreddits().execute();
        } else {
            mSubredditList = new ArrayList<String>();
            String[] subs = getResources().getStringArray(R.array.default_subreddits);
            for (String s : subs) {
                mSubredditList.add(s);
            }
            mSubredditList.add(0, "FrontPage");
            mSubredditArrayAdapter = new SubredditAdapter(mSubredditList);
            mDrawerListView.setAdapter(mSubredditArrayAdapter);
        }
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);

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

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
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

    /**
     * Users of this fragment must call this method to set up the navigation drawer interactions.
     *
     * @param drawerLayout The DrawerLayout containing this fragment's UI.
     */
    public void setUp(DrawerLayout drawerLayout, Activity host) {

        final Activity c = host;

//        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
        // set up the drawer's list view with items and click listener

        ActionBar actionBar = host.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);
//
        // ActionBarDrawerToggle ties together the the proper interactions
        // between the navigation drawer and the action bar app icon.
        mDrawerToggle = new ActionBarDrawerToggle(
                host,                             /* host Activity */
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

                getActivity().invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }
                c.invalidateOptionsMenu(); // calls onPrepareOptionsMenu()
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
    }

    private void selectItem(final String subreddit) {
        new SubredditDataAsyncTask(subreddit).execute();
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawers();
        }
        if (mCallbacks != null) {
            if (mAccount == null) {
                mCheckbox.setVisibility(View.GONE);
            } else if (subreddit.equals("FrontPage") || subreddit.equals("")) {
                mCallbacks.onNavigationDrawerItemSelected(null);
                mCheckbox.setVisibility(View.GONE);
            } else {
                mCallbacks.onNavigationDrawerItemSelected(subreddit);
                mCheckbox.setVisibility(View.VISIBLE);
            }
            mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    new SubscribeAsyncTask(b).execute();
                }
            });
            mCurrentSubreddit.setText("Currently viewing " + subreddit);
        }
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

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        // If the drawer is open, show the global app actions in the action bar. See also
//        // showGlobalContextActionBar, which controls the top-left area of the action bar.
//        if (mDrawerLayout != null && isDrawerOpen()) {
//            inflater.inflate(R.menu.global, menu);
//            showGlobalContextActionBar();
//        }
//        super.onCreateOptionsMenu(menu, inflater);
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Per the navigation drawer design guidelines, updates the action bar to show the global app
     * 'context', rather than just what's in the current screen.
     */
//    private void showGlobalContextActionBar() {
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayShowTitleEnabled(true);
//        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
//        actionBar.setTitle(R.string.app_name);
//    }

    /**
     * Callbacks interface that all activities using this fragment must implement.
     */
    public static interface NavigationDrawerCallbacks {
        /**
         * Called when an item in the navigation drawer is selected.
         */
        void onNavigationDrawerItemSelected(String subreddit);
        void onSortSelected(int sortType);
        void onSubSortSelected(int sortType);
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
                if (!isNew) {
                    mAccount.setSubreddits(newSubs);
                    AccountDataSource dataSource = new AccountDataSource(getActivity());
                    dataSource.open();
                    dataSource.setSubredditList(mAccount);
                    dataSource.close();
                }
                return isNew;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean isNew) {
            if (isNew) {
                Collections.sort(mSubredditList, mOrderList);
                mSubredditList.add(0, "FrontPage");
                mSubredditArrayAdapter.notifyDataSetChanged();
            }
        }
    }

    public static NavigationDrawerFragment newInstance(Account account) {
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        NavigationDrawerFragment fragment = new NavigationDrawerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private static Comparator<String> mOrderList = new Comparator<String>() {
        public int compare(String one, String two) {
            int result = String.CASE_INSENSITIVE_ORDER.compare(one, two);
            return (result != 0) ? result : one.compareTo(two);
        }
    };

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.header_drawer_2, null);
        final EditText mSubredditSearch = (EditText) v.findViewById(R.id.search_subreddit);
        final ImageButton search = (ImageButton) v.findViewById(R.id.search_button);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                selectItem(mSubredditSearch.getText().toString().trim());
            }
        });
        mCurrentSubreddit = (TextView) v.findViewById(R.id.current_subreddit);
        mCurrentSubreddit.setText("Currently viewing FrontPage");
        mCheckbox = (CheckBox) v.findViewById(R.id.subscribed_CheckBox);
        mCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b)
                    Toast.makeText(getActivity(), "Subscribed to "
                            + mCurrentSubreddit.getText().toString(), Toast.LENGTH_SHORT).show();
                else
                    Toast.makeText(getActivity(), "Unsubscribed from "
                            + mCurrentSubreddit.getText().toString(), Toast.LENGTH_SHORT).show();
            }
        });
        mCheckbox.setVisibility(View.GONE);

        Spinner subSpinner = (Spinner) v.findViewById(R.id.header_spinner1);
        final Spinner filterSpinner = (Spinner) v.findViewById(R.id.header_spinner2);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item,
                R.id.orange_spinner_text,
                getResources().getStringArray(R.array.subreddit_sort_types));
        subSpinner.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(getActivity(),
                R.layout.spinner_item,
                R.id.orange_spinner_text,
                getResources().getStringArray(R.array.sub_sort_types));
        filterSpinner.setAdapter(adapter2);
        filterSpinner.setSelection(Submission.ALL);

        subSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i > 2) {
                    filterSpinner.setVisibility(View.VISIBLE);
                } else {
                    filterSpinner.setVisibility(View.GONE);
                }
                mCallbacks.onSortSelected(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });

        filterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mCallbacks.onSubSortSelected(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                return;
            }
        });
        return v;
    }


    private class SubredditAdapter extends ArrayAdapter<String> {

        public SubredditAdapter(List<String> items) {
            super(getActivity().getActionBar().getThemedContext(), 0, items);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getActivity().getLayoutInflater()
                        .inflate(R.layout.list_item_subreddit, null);
            }
            if (position == mDrawerListView.getSelectedItemPosition())
                v.setBackgroundColor(getResources().getColor(R.color.auburn));
            String subreddit = mSubredditList.get(position);
            TextView subredditName = (TextView)v.findViewById(R.id.subreddit_list_item_title);
            subredditName.setText(subreddit);
            return v;
        }
    }

    private class SubscribeAsyncTask extends AsyncTask<Void, Void, Void> {

        private boolean mSubscribe;

        public SubscribeAsyncTask(boolean sub) {
            mSubscribe = sub;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            if (mSubscribe) {
                apiParams.add(new BasicNameValuePair("action", "sub"));
            } else {
                apiParams.add(new BasicNameValuePair("action", "unsub"));
            }
            if (mSubreddit != null) {
                apiParams.add(new BasicNameValuePair("sr", mSubreddit.getName()));
                Utilities.post(apiParams, "http://www.reddit.com/api/subscribe/", mAccount);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            if (mSubscribe) {
                mSubredditList.add(mSubreddit.getDisplayName());
                mSubredditList.remove(0);
                Collections.sort(mSubredditList, mOrderList);
                mSubredditList.add(0, "FrontPage");
            } else {
                if (mSubreddit != null)
                    mSubredditList.remove(mSubreddit.getDisplayName());
            }
            mSubredditArrayAdapter.notifyDataSetChanged();
        }
    }

    private class SubredditDataAsyncTask extends AsyncTask<Void, Void, Boolean> {

        private String mSubName;

        public SubredditDataAsyncTask(String sub) {
            mSubName = sub;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            try {
                mSubredditIsLoading = true;
                String s = Utilities.get("", "http://www.reddit.com/r/" + mSubName + "/about.json",
                        mAccount);
                mSubreddit = Subreddit.fromString(s);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (JsonParseException e) {
                e.printStackTrace();
                return false;
            } finally {
                mSubredditIsLoading = false;
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                mCheckbox.setChecked(mSubreddit.userIsSubscriber());
                mCheckbox.setEnabled(true);
            } else {
                mCheckbox.setChecked(false);
                mCheckbox.setEnabled(false);
            }
        }
    }
}
