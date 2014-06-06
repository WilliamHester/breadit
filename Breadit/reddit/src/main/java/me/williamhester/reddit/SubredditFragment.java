package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.williamhester.areddit.Submission;
import me.williamhester.areddit.SubmissionsListViewHelper;
import me.williamhester.areddit.Account;
import me.williamhester.areddit.Votable;
import me.williamhester.areddit.utils.Utilities;

public class SubredditFragment extends Fragment {

    public static final int HISTORY = 0;
    public static final int SAVED = 1;

    private Context mContext;
    private ListView mSubmissions;
    private String mSubredditName;
    private SubmissionArrayAdapter mSubmissionsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ArrayList<Submission> mSubmissionList;
    private HashSet<String> mNames;
    private Account mAccount;
    private View mFooterView;

    private GestureDetector mGestureDetector;
    private View.OnTouchListener mGestureListener;

    private boolean mFailedToLoad = false;
    private boolean mHideNsfw = true;
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
            mNames = new HashSet<String>();
            for (String name : array) {
                mNames.add(name);
            }
        } else if (getArguments() != null) {
            mAccount = getArguments().getParcelable("account");
            mSubredditName = getArguments().getString("subreddit");
            mNames = new HashSet<String>();
            mSubmissionList = new ArrayList<Submission>();
        }
        loadPrefs();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_subreddit, null);
        mGestureDetector = new GestureDetector(mContext, new SwipeDetector2());
        mGestureListener = new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return mGestureDetector.onTouchEvent(motionEvent);
            }
        };

        mSubmissions = (ListView) v.findViewById(R.id.submissions);
        mFooterView = createFooterView(inflater);
        mSubmissions.addFooterView(mFooterView);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new RefreshUserClass(true).execute();
            }
        });
        mSwipeRefreshLayout.setColorScheme(R.color.orangered, R.color.periwinkle,
                R.color.orangered,R.color.periwinkle);
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
        mPrimarySortType = sortType;
        refreshData();
    }

    public void setSecondarySort(int sortType) {
        mSecondarySortType = sortType;
        refreshData();
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
        long oldId = -1;
        if (mAccount != null) {
            oldId = mAccount.getId();
        }
        long id = prefs.getLong("accountId", -1);
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
        if (oldId != id || oldHideViewed != mHideViewed) {
            new RefreshUserClass(true).execute();
            mSubmissions.invalidateViews();
        }
    }

    /**
     * This method is called to begin the list of submissions. It is called during onCreate and
     *     when the SwipeRefreshLayout's onRefresh method is called.
     */
    private void populateSubmissions() {
        mSubmissionsAdapter = new SubmissionArrayAdapter(mContext);
        mSubmissions.setAdapter(mSubmissionsAdapter);
        mSubmissions.setOnTouchListener(mGestureListener);
        mSubmissions.setOnScrollListener(new InfiniteLoadingScrollListener());
        new RefreshUserClass().execute();
    }

    private View createFooterView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.footer_subreddit_fragment, null);
        return v;
    }

    private class SubmissionArrayAdapter extends ArrayAdapter<Submission> {
        Context mContext;

        public SubmissionArrayAdapter(Context context) {
            super(context, R.layout.list_item_post, mSubmissionList);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Submission s = (Submission) mSubmissions.getItemAtPosition(position);

            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_item_post, parent, false);
            else
                convertView.invalidate();

            final View voteStatus = convertView.findViewById(R.id.vote_status);
            TextView nameAndTime
                    = (TextView) convertView.findViewById(R.id.subreddit_name_and_time);
            TextView author = (TextView) convertView.findViewById(R.id.author);
            ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView domain = (TextView) convertView.findViewById(R.id.domain);
            final TextView points = (TextView) convertView.findViewById(R.id.points);

            // if the submission is a self post, we need to hide the thumbnail
            if (s.isNsfw()) {
                thumbnail.setScaleType(ImageView.ScaleType.CENTER);
                thumbnail.setImageDrawable(getResources().getDrawable(R.drawable.ic_nsfw));
            } else if (s.isSelf() || s.getThumbnailUrl().equals("")) {
                thumbnail.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                thumbnail.setImageDrawable(getResources()
                        .getDrawable(R.drawable.ic_action_next_item));
            } else {
                thumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                UrlImageViewHelper.setUrlDrawable(thumbnail, s.getThumbnailUrl());
            }

            if (mSubredditName == null || mSubredditName.equals("")) {
                nameAndTime.setText(" in " + s.getSubredditName() + " "
                        + Utilities.calculateTimeShort(s.getCreatedUtc()));
            }
            switch (s.getVoteStatus()) {
                case Votable.DOWNVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                    break;
                case Votable.UPVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                    break;
                default:
                    voteStatus.setVisibility(View.GONE);
                    break;
            }

            title.setText(StringEscapeUtils.unescapeHtml4(s.getTitle()));
            author.setText(s.getAuthor());
            domain.setText("(" + s.getDomain() + ")");
            points.setText(s.getScore() + " points by ");

            if (mAccount != null && mAccount.hasVisited(getItem(position).getName())) {
                title.setTypeface(title.getTypeface(), Typeface.ITALIC);
            } else {
                title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
            }
            
            return convertView;
        }
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
                    mPrimarySortType, mSecondarySortType, null, null, mAccount, mSubmissions);
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
            List<Submission> submissions;
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
                    JsonObject jsonData = array.get(i).getAsJsonObject();
                    submissions.add(Submission.fromJsonString(jsonData));
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
                return null;
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
                    if (result.size() < 25) {
                        mSubmissions.removeFooterView(mFooterView);
                    }
                    for (Submission s : result) {
                        if (!mNames.contains(s.getName()) && (!mHideNsfw || !s.isNsfw())
                                && !(mHideViewed && mAccount != null
                                && mAccount.hasVisited(s.getName()))) {
                            mSubmissionList.add(s);
                            mNames.add(s.getName());
                        }
                    }
                    mSubmissionsAdapter.notifyDataSetChanged();
                } else if (mNothingHere) {
                    TextView loading = (TextView) mFooterView.findViewById(R.id.loading_text);
                    if (loading != null)
                        loading.setText(R.string.nothing_here);
                    ProgressBar progressBar = (ProgressBar) mFooterView.findViewById(R.id.progress_bar);
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                } else if (mFailedToLoad) {
                    TextView loading = (TextView) mFooterView.findViewById(R.id.loading_text);
                    if (loading != null)
                        loading.setText(R.string.failed_to_load);
                    ProgressBar progressBar = (ProgressBar) mFooterView.findViewById(R.id.progress_bar);
                    if (progressBar != null)
                        progressBar.setVisibility(View.GONE);
                    mFailedToLoad = true;
                }
                mSwipeRefreshLayout.setRefreshing(false);
            } catch (NullPointerException e) {
                Log.e("Breadit", "Something bad happened");
            }
        }
    }

    public class InfiniteLoadingScrollListener implements AbsListView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int previousTotal = 0;
        private boolean loading = true;

        public InfiniteLoadingScrollListener() {
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                }
            }
            if (!loading && mSubmissionList.size() > 0
                    && (totalItemCount - visibleItemCount) <= (firstVisibleItem + VISIBLE_THRESHOLD)) {
                SubmissionsListViewHelper list = new SubmissionsListViewHelper(mSubredditName,
                        mPrimarySortType, mSecondarySortType, null,
                        mSubmissionList.get(mSubmissionList.size() - 1).getName(),
                        mAccount, mSubmissions);
                new RetrieveSubmissionsTask().execute(list);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) { }
    }

    public class SwipeDetector2 extends GestureDetector.SimpleOnGestureListener {

        private static final int SWIPE_MIN_DISTANCE = 120;
        private static final int SWIPE_MAX_OFF_PATH = 250;
        private static final int SWIPE_THRESHOLD_VELOCITY = 200;

        @Override
        public boolean onSingleTapUp(MotionEvent ev) {
            int x = (int) ev.getX();
            int y = (int) ev.getY();
            int position = mSubmissions.pointToPosition(x, y);
            if (position == mSubmissionList.size()) {
                if (mFailedToLoad) {
                    mFailedToLoad = false;
                    new RefreshUserClass().execute();
                    TextView loading = (TextView) mFooterView.findViewById(R.id.loading_text);
                    loading.setText(R.string.loading_submissions);
                    ProgressBar progressBar = (ProgressBar) mFooterView.findViewById(R.id.progress_bar);
                    progressBar.setVisibility(View.VISIBLE);
                }
            } else if (position >= 0) {
                ImageView iv = null;
                Submission s;
                if (mHideViewed) {
                    s = mSubmissionList.remove(position);
                } else {
                    s = mSubmissionList.get(position);
                }
                if (mSubmissions.getChildAt(position
                        - mSubmissions.getFirstVisiblePosition()) != null)
                    iv = (ImageView) mSubmissions.getChildAt(position
                            - mSubmissions.getFirstVisiblePosition()).findViewById(R.id.thumbnail);
                if (mAccount == null) {
                    Log.i("SubredditFragment", "mAccount is null");
                } else if (!mAccount.hasVisited(mSubmissionList.get(position).getName())) {
                    mAccount.visit(s.getName());
                    AccountDataSource dataSource = new AccountDataSource(mContext);
                    dataSource.open();
                    dataSource.setHistory(mAccount);
                    dataSource.close();
                }
                Intent i = new Intent(getActivity(), SubmissionActivity.class);
                Bundle b = new Bundle();
                b.putParcelable("submission", s);
                b.putParcelable("account", mAccount);
                // Clicked on the image side
                if (iv != null && x >= iv.getLeft() + mSubmissions.getLeft()) {
                    b.putString("tab", SubmissionActivity.CONTENT_TAB);
                } else { // Clicked on the text side
                    b.putString("tab", SubmissionActivity.COMMENT_TAB);
                }
                i.putExtras(b);
                mContext.startActivity(i);
                mSubmissionsAdapter.notifyDataSetChanged();
            }
            return false;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mAccount != null) {
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                        return false;
                } catch (NullPointerException e) {
                    return false;
                }
                // right to left swipe
                int position = mSubmissions.pointToPosition((int) e1.getX(), (int) e1.getY());
                if (position > -1 && e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                            Submission s = mSubmissionsAdapter.getItem(position);
                            if (s.getVoteStatus() == Submission.DOWNVOTED) {
                                new VoteAsyncTask(s.getName(), mAccount, VoteAsyncTask.NEUTRAL).execute();
                                s.setVoteStatus(Submission.NEUTRAL);
                            } else {
                                new VoteAsyncTask(s.getName(), mAccount, VoteAsyncTask.DOWNVOTE).execute();
                                s.setVoteStatus(Submission.DOWNVOTED);
                            }
                            View v = mSubmissions.getChildAt(position - mSubmissions.getFirstVisiblePosition());
                            View voteStatus = v.findViewById(R.id.vote_status);
                            TextView points = (TextView) v.findViewById(R.id.points);
                            switch (s.getVoteStatus()) {
                                case Submission.DOWNVOTED:
                                    voteStatus.setVisibility(View.VISIBLE);
                                    voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                                    points.setText(s.getScore() + " points by ");
                            break;
                        default:
                            voteStatus.setVisibility(View.GONE);
                            points.setText(s.getScore() + " points by ");
                            break;
                    }
                } else if (position > -1 && e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
                        && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    Submission s = mSubmissionsAdapter.getItem(position);
                    if (s.getVoteStatus() == Submission.UPVOTED) {
                        new VoteAsyncTask(s.getName(), mAccount, VoteAsyncTask.NEUTRAL).execute();
                        s.setVoteStatus(Submission.NEUTRAL);
                    } else {
                        new VoteAsyncTask(s.getName(), mAccount, VoteAsyncTask.UPVOTE).execute();
                        s.setVoteStatus(Submission.UPVOTED);
                    }
                    View v = mSubmissions.getChildAt(position - mSubmissions.getFirstVisiblePosition());
                    View voteStatus = v.findViewById(R.id.vote_status);
                    TextView points = (TextView) v.findViewById(R.id.points);
                    switch (s.getVoteStatus()) {
                        case Submission.UPVOTED:
                            voteStatus.setVisibility(View.VISIBLE);
                            voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                            points.setText(s.getScore() + " points by ");
                            break;
                        default:
                            voteStatus.setVisibility(View.GONE);
                            points.setText(s.getScore() + " points by ");
                            break;
                    }
                }
            }
            return false;
        }
    }
}
