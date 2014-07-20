package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.models.SubmissionsListViewHelper;
import me.williamhester.models.Account;
import me.williamhester.models.utils.Utilities;
import me.williamhester.databases.AccountDataSource;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.adapters.SubmissionsRecyclerAdapter;

public class SubredditFragment extends Fragment implements SubmissionsRecyclerAdapter.AdapterCallbacks {

    public static final int HISTORY = 0;
    public static final int SAVED = 1;

    private Context mContext;
    private String mSubredditName;
    private SubmissionsRecyclerAdapter mSubmissionsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Submission> mSubmissionList;
    private HashSet<String> mNames;
    private Account mAccount;

    private RecyclerView mSubmissionsView;

    private SubredditFragment mThis = this;
    private boolean mFailedToLoad = false;
    private boolean mHideNsfw = false;
    private boolean mHideViewed = false;
    private int mPrimarySortType = Submission.HOT;
    private int mSecondarySortType = Submission.ALL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mContext = getActivity();
        if (savedInstanceState != null) {
            mAccount = savedInstanceState.getParcelable("account");
            mSubredditName = savedInstanceState.getString("subreddit");
            mSubmissionList = savedInstanceState.getParcelableArrayList("submissions");
            String[] array = savedInstanceState.getStringArray("names");
            mNames = new HashSet<>();
            for (String name : array) {
                mNames.add(name);
            }
        } else if (getArguments() != null) {
            mAccount = getArguments().getParcelable("account");
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<>();
            mSubmissionList = new ArrayList<>();
        }
        mSubmissionsAdapter = new SubmissionsRecyclerAdapter(mSubmissionList, this);
        loadPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_subreddit, null);

        mSubmissionsView = (RecyclerView) v.findViewById(R.id.Submissions_recycler);
        mSubmissionsView.setHasFixedSize(false);
        RecyclerView.LayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        mSubmissionsView.setLayoutManager(linearLayoutManager);
        mSubmissionsView.setAdapter(mSubmissionsAdapter);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshUserClass(true).execute();
            }
        });

        mSwipeRefreshLayout.setColorSchemeResources(R.color.orangered, R.color.periwinkle,
                R.color.orangered, R.color.periwinkle);
        populateSubmissions();
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mSubredditName != null && getActivity() != null
                && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle("/r/" + mSubredditName);
        } else if (getActivity() != null && getActivity().getActionBar() != null) {
            getActivity().getActionBar().setTitle("FrontPage");
        }
        loadPrefs();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList("submissions", mSubmissionList);
        outState.putParcelable("account", mAccount);
        outState.putString("subreddit", mSubredditName);
        String[] array = new String[mNames.size()];
        mNames.toArray(array);
        outState.putStringArray("names", array);
        super.onSaveInstanceState(outState);
    }

    public void setPrimarySort(int sortType) {
        if (mPrimarySortType != sortType) {
            mPrimarySortType = sortType;
            refreshData();
        }
    }

    public void setSecondarySort(int sortType) {
        if (mSecondarySortType != sortType) {
            mSecondarySortType = sortType;
            refreshData();
        }
    }

    public int getPrimarySortType() {
        return mPrimarySortType;
    }

    public int getSecondarySortType() {
        return mSecondarySortType;
    }

    public String getSubreddit() {
        return mSubredditName;
    }

    public void refreshData() {
        Log.d("SubredditFragment", "refreshData");
        new RefreshUserClass(true).execute();
    }

    public static SubredditFragment newInstance(Account account, String subredditName) {
        SubredditFragment sf = new SubredditFragment();
        Bundle b = new Bundle();
        b.putString("subreddit", subredditName);
        b.putParcelable("account", account);
        sf.setArguments(b);
        return sf;
    }

    public static SubredditFragment newInstance(Account account, int type) {
        SubredditFragment sf = new SubredditFragment();
        Bundle b = new Bundle();
        b.putInt("type", type);
        b.putParcelable("account", account);
        sf.setArguments(b);
        return sf;
    }

    private void loadPrefs() {
        SharedPreferences prefs = mContext.getSharedPreferences("preferences", Context.MODE_PRIVATE);
        boolean oldHideViewed = mHideViewed;
        long oldId;
        long id = prefs.getLong("accountId", -1);
        if (mAccount != null) {
            oldId = mAccount.getId();
        } else {
            oldId = id;
        }
        if (mSubredditName != null) {
            mHideViewed = prefs.getBoolean("pref_remove_viewed_sub", false);
        } else {
            mHideViewed = prefs.getBoolean("pref_remove_viewed_front", false);
        }
        if (id != -1) {
            try {
                AccountDataSource dataSource = new AccountDataSource(mContext);
                dataSource.open();
                mAccount = dataSource.getAccount(id);
                dataSource.close();
            } catch (NullPointerException e) {
                Log.e("Breadit", "Error opening database");
            }
        } else {
            mAccount = null;
        }
        if (getActivity() != null)
            getActivity().invalidateOptionsMenu();
        if (mSubmissionsView != null && oldId != id || oldHideViewed != mHideViewed) {
            new RefreshUserClass(true).execute();
        }
    }

    /**
     * This method is called to begin the list of submissions. It is called during onCreate and
     *     when the SwipeRefreshLayout's onRefresh method is called.
     */
    private void populateSubmissions() {
        mSubmissionsAdapter = new SubmissionsRecyclerAdapter(mSubmissionList, this);
        mSubmissionsView.setOnScrollListener(new InfiniteLoadingScrollListener());
        Log.d("SubredditFragment", "populateSubmissions");
        new RefreshUserClass().execute();
    }

    private View createFooterView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.footer_subreddit_fragment, null);
        return v;
    }

    @Override
    public void onImageViewClicked(ImgurImage image) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, ImagePagerFragment.newInstance(image), "ImagePagerFragment")
                .addToBackStack("ImagePagerFragment")
                .commit();
    }

    @Override
    public void onImageViewClicked(ImgurAlbum album) {
        getFragmentManager().beginTransaction()
                .add(R.id.container, ImagePagerFragment.newInstance(album), "ImagePagerFragment")
                .addToBackStack("ImagePagerFragment")
                .commit();
    }

    @Override
    public void onCardClicked(Submission submission) {
        Intent i = new Intent(getActivity(), SubmissionActivity.class);
        Bundle args = new Bundle();
        args.putParcelable(SubmissionActivity.SUBMISSION, submission);
        args.putParcelable(SubmissionActivity.ACCOUNT, mAccount);
        args.putString(SubmissionActivity.TAB, SubmissionActivity.COMMENT_TAB);
        i.putExtras(args);
        getActivity().startActivity(i);
    }

    private class RefreshUserClass extends AsyncTask<Void, Void, Void> {
        private boolean mRefreshList;

        public RefreshUserClass() {
            this(false);
        }

        public RefreshUserClass(boolean refreshList) {
            mRefreshList = refreshList;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (mAccount != null) {
                mAccount.refreshUserData();
                mSwipeRefreshLayout.setRefreshing(true);
            }
            SubmissionsListViewHelper list = new SubmissionsListViewHelper(mSubredditName,
                    mPrimarySortType, mSecondarySortType, null, null, mAccount);
            new RetrieveSubmissionsTask(mRefreshList).execute(list);
            return null;
        }
    }

    private class RetrieveSubmissionsTask extends AsyncTask<SubmissionsListViewHelper, Void,
            List<Submission>> {
        private boolean mRefreshList;
        private boolean mNothingHere = false;

        public RetrieveSubmissionsTask() {
            this(false);
        }

        public RetrieveSubmissionsTask(boolean refreshList) {
            mRefreshList = refreshList;
        }

        @Override
        protected List<Submission> doInBackground(SubmissionsListViewHelper... submissionsList) {
            List<Submission> submissions = null;
            try {
                mSwipeRefreshLayout.setRefreshing(true);
                String data;
                if (mAccount != null)
                    data = Utilities.get("", submissionsList[0].getUrl(),
                        mAccount.getCookie(), mAccount.getModhash());
                else
                    data = Utilities.get("", submissionsList[0].getUrl(), null, null);

                JsonObject rootObject = new JsonParser().parse(data).getAsJsonObject();
                JsonArray array = rootObject.get("data").getAsJsonObject().get("children").getAsJsonArray();

                submissions = new ArrayList<Submission>();
                for (int i = 0; i < array.size(); i++) {
                    array.get(i);
                    Gson gson = new Gson();
                    JsonElement element = array.get(i).getAsJsonObject().get("data");
                    submissions.add(gson.fromJson(element, Submission.class));
                }
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            } catch (JsonSyntaxException e) {
                mNothingHere = true;
                e.printStackTrace();
                return submissions;
            }
            return submissions;
        }

        @Override
        protected void onPostExecute(List<Submission> result) {
            try {
                if (result != null) {
                    if (mRefreshList) {
                        mSubmissionList.clear();
                        mNames.clear();
                    }
                    int i = 0;
                    for (Submission s : result) {
                        Log.d("SubredditFragment", "" + i++);
                        if (!mNames.contains(s.getName()) && (!mHideNsfw || !s.isNsfw())
                                && !(mHideViewed && mAccount != null
                                && mAccount.hasVisited(s.getName()))) {
                            mSubmissionList.add(s);
                            mNames.add(s.getName());
                        }
                    }
                    Log.d("SubredditFragment", "Went here " + mSubmissionList.size());
                    mSubmissionsView.setAdapter(new SubmissionsRecyclerAdapter(mSubmissionList, mThis));
                    mSubmissionsAdapter.notifyDataSetChanged();
                } else if (mNothingHere) {

                } else if (mFailedToLoad) {
                    mFailedToLoad = true;
                }
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (NullPointerException e) {
                Log.e("Breadit", "Something bad happened");
            }
        }
    }

    public class InfiniteLoadingScrollListener implements RecyclerView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int previousTotal = 0;
        private boolean loading = true;

//        @Override
//        public void onScroll(AbsListView view, int firstVisibleItem,
//                             int visibleItemCount, int totalItemCount) {
//            if (loading) {
//                if (totalItemCount > previousTotal) {
//                    previousTotal = totalItemCount;
//                    loading = false;
//                }
//            } else if (mSubmissionList.size() > 0
//                    && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
//                SubmissionsListViewHelper list = new SubmissionsListViewHelper(mSubredditName,
//                        mPrimarySortType, mSecondarySortType, null,
//                        mSubmissionList.get(mSubmissionList.size() - 1).getName(),
//                        mAccount);
//                new RetrieveSubmissionsTask().execute(list);
//                loading = true;
//            }
//        }

        @Override
        public void onScrollStateChanged(int i) {
            // Don't care
        }

        @Override
        public void onScrolled(int i, int i2) {

        }
    }
}
