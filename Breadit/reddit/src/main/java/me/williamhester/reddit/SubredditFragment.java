package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import me.williamhester.areddit.Submission;
import me.williamhester.areddit.SubmissionsListViewHelper;
import me.williamhester.areddit.User;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William Hester on 1/3/14.
 * This class is the Fragment that contains the list of Submissions for that specific Subreddit.
 */
public class SubredditFragment extends Fragment {

    private ActionBar mAction;
    private Context mContext;
    private ListView mSubmissions;
    private String mSubredditName;
    private SubmissionArrayAdapter mSubmissionsAdapter;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Submission> mSubmissionList;
    private HashSet<String> mNames;
    private User mUser;

    private GestureDetector mGestureDetector;
    private View.OnTouchListener mGestureListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            mAction = getActivity().getActionBar();
        }
        if (getArguments() != null) {
            mUser = getArguments().getParcelable("user");
            mSubredditName = getArguments().getString("subreddit");
        }
        if (mAction != null) {
            mAction.setTitle("Front page of Reddit");
        }
        mContext = getActivity();
    }

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
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateSubmissions();
            }
        });
        mSwipeRefreshLayout.setColorScheme(R.color.orangered, R.color.periwinkle,
                R.color.orangered,R.color.periwinkle);
        populateSubmissions();
        return v;
    }

    public void setSubreddit(String subreddit) {
        mSubredditName = subreddit;
        populateSubmissions();
    }

    public static SubredditFragment newInstance(User user, String subredditName) {
        SubredditFragment sf = new SubredditFragment();
        Bundle b = new Bundle();
        b.putString("subreddit", subredditName);
        b.putParcelable("user", user);
        sf.setArguments(b);
        return sf;
    }

    /**
     * This method is called to begin the list of submissions. It is called during onCreate and
     *     when the SwipeRefreshLayout's onRefresh method is called.
     */
    private void populateSubmissions() {
        mNames = new HashSet<String>();
        mSubmissionList = new ArrayList<Submission>();
//        mSubmissions.addHeaderView(createHeaderView());
        mSubmissionsAdapter = new SubmissionArrayAdapter(mContext);
        mSubmissions.setAdapter(mSubmissionsAdapter);
        mSubmissions.setOnTouchListener(mGestureListener);
        mSubmissions.setOnScrollListener(new InfiniteLoadingScrollListener());
        new RefreshUserClass().execute();
    }

    private View createHeaderView() {
        LayoutInflater inflater
                = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.header_subreddit, null);
        TextView title = (TextView) v.findViewById(R.id.subreddit);
        title.setText("/r/" + mSubredditName);
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
            View spacer = convertView.findViewById(R.id.spacer);

            // if the submission is a self post, we need to hide the thumbnail
            if (s.isSelf()) {
                thumbnail.setVisibility(View.GONE);
                spacer.setVisibility(View.GONE);
            } else {
                thumbnail.setVisibility(View.VISIBLE);
                spacer.setVisibility(View.VISIBLE);
                UrlImageViewHelper.setUrlDrawable(thumbnail, s.getThumbnailUrl());
            }

            if (mSubredditName == null || mSubredditName.equals("")) {
                nameAndTime.setText(" in " + s.getSubredditName() + " " + calculateTimeShort(s.getCreatedUtc()));
            }
            switch (s.getVoteStatus()) {
                case Submission.DOWNVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                    break;
                case Submission.UPVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                    break;
                default:
                    voteStatus.setVisibility(View.GONE);
                    break;
            }

            title.setText(s.getTitle());
            author.setText(s.getAuthor());
            domain.setText("(" + s.getDomain() + ")");
            points.setText(s.getScore() + " points by ");
            
            return convertView;
        }
    }

    private String calculateTimeShort(long postTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long difference = currentTime - postTime;
        String time;
        if (difference / 31536000 > 0) {
            time = difference / 3156000 + "y";
        } else if (difference / 2592000 > 0) {
            time = difference / 2592000 + "m";
        } else if (difference / 604800 > 0) {
            time = difference / 604800 + "w";
        } else if (difference / 86400 > 0) {
            time = difference / 86400 + "d";
        } else if (difference / 3600 > 0) {
            time = difference / 3600 + "h";
        } else if (difference / 60 > 0) {
            time = difference / 60 + "m";
        } else {
            time = difference + "s";
        }
        return time;
    }

    private String calculateTime(long postTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long difference = currentTime - postTime;
        String time;
        if (difference / 31536000 > 0) {
            if (difference / 3156000 == 1)
                time = "1 year ago";
            else
                time = difference / 3156000 + " years ago";
        } else if (difference / 2592000 > 0) {
            if (difference / 2592000 == 1)
                time = "1 month ago";
            else
                time = difference / 2592000 + " months ago";
        } else if (difference / 604800 > 0) {
            if (difference / 604800 == 1)
                time = "1 week ago";
            else
                time = difference / 604800 + " Weeks ago";
        } else if (difference / 86400 > 0) {
            if (difference / 86400 == 1)
                time = "1 day ago";
            else
                time = difference / 86400 + " day ago";
        } else if (difference / 3600 > 0) {
            if (difference / 3600 == 1)
                time = "1 hour ago";
            else
                time = difference / 3600 + " hours ago";
        } else if (difference / 60 > 0) {
            if (difference / 60 == 1)
                time = "1 minute ago";
            else
                time = difference / 60 + " minutes ago";
        } else {
            if (difference == 1)
                time = "1 second ago";
            else
                time = difference + " seconds ago";
        }
        return time;
    }

    private class RefreshUserClass extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            if (mUser != null) {
                mUser.refreshUserData();
            }

            SubmissionsListViewHelper list = new SubmissionsListViewHelper(mSubredditName,
                    Submission.HOT, -1, null, null, mUser, mSubmissions);
            new RetrieveSubmissionsTask().execute(list);
            return null;
        }
    }

    private class RetrieveSubmissionsTask extends AsyncTask<SubmissionsListViewHelper, Void,
            List<Submission>> {

        String exceptionText;

        @Override
        protected List<Submission> doInBackground(SubmissionsListViewHelper... submissionsList) {
            List<Submission> submissions;
            try {
                mSwipeRefreshLayout.setRefreshing(true);
                String data;
                if (mUser != null)
                    data = Utilities.get("", submissionsList[0].getUrl(),
                        mUser.getCookie(), mUser.getModhash());
                else
                    data = Utilities.get("", submissionsList[0].getUrl(), null, null);
                JsonObject rootObject = new JsonParser().parse(data).getAsJsonObject();
                JsonArray array = rootObject.get("data").getAsJsonObject().get("children").getAsJsonArray();

                submissions = new ArrayList<Submission>();
                for (int i = 0; i < array.size(); i++) {
                    JsonObject jsonData = array.get(i).getAsJsonObject();
                    submissions.add(Submission.fromJsonString(jsonData));
                }
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return null;
            }
            return submissions;
        }

        @Override
        protected void onPostExecute(List<Submission> result) {
            if (result != null) {
                // If result has a size of 0, then we need to tell the user that they must start from
                //     the beginning because there are no more submissions that can be loaded.
//                if (result.size() == 0) {
//                mSubmissions.addFooterView(v);
//                }
                for (Submission s : result) {
                    if (!mNames.contains(s.getName())) {
                        mSubmissionList.add(s);
                        mNames.add(s.getName());
                    }
                }
                mSubmissionsAdapter.notifyDataSetChanged();
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    public class InfiniteLoadingScrollListener implements AbsListView.OnScrollListener {

        private int visibleThreshold = 5;
        private int currentPage = 0;
        private int previousTotal = 0;
        private boolean loading = true;

        public InfiniteLoadingScrollListener() {
        }
        public InfiniteLoadingScrollListener(int visibleThreshold) {
            this.visibleThreshold = visibleThreshold;
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem,
                             int visibleItemCount, int totalItemCount) {
            if (loading) {
                if (totalItemCount > previousTotal) {
                    loading = false;
                    previousTotal = totalItemCount;
                    currentPage++;
                }
            }
            if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
                SubmissionsListViewHelper list = new SubmissionsListViewHelper(mSubredditName,
                        Submission.HOT, -1, null,
                        mSubmissionList.get(mSubmissionList.size() - 1).getName(),
                        mUser, mSubmissions);
                new RetrieveSubmissionsTask().execute(list);
                loading = true;
            }
        }

        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
        }
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
            if (position >= 0) {
                ImageView iv = null;
                if (mSubmissions.getChildAt(position
                        - mSubmissions.getFirstVisiblePosition()) != null)
                    iv = (ImageView) mSubmissions.getChildAt(position
                            - mSubmissions.getFirstVisiblePosition()).findViewById(R.id.thumbnail);
                Intent i = new Intent(getActivity(), SubmissionActivity.class);
                Bundle b = new Bundle();
                b.putString("permalink", mSubmissionList.get(position).getPermalink());
                b.putString("url", mSubmissionList.get(position).getUrl());
                b.putBoolean("isSelf", mSubmissionList.get(position).isSelf());
                b.putParcelable("user", mUser);
                // Clicked on the image side
                if (iv != null && x >= iv.getLeft() + mSubmissions.getLeft()) {
                    b.putInt("tab", SubmissionActivity.CONTENT_TAB);
                } else { // Clicked on the text side
                    b.putInt("tab", SubmissionActivity.COMMENT_TAB);
                }
                i.putExtras(b);
                mContext.startActivity(i);
            }
            return false;
        }


        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            if (mUser != null) {
                try {
                    if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                        return false;
                    // right to left swipe
                    if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        int position = mSubmissions.pointToPosition((int) e1.getX(), (int) e1.getY());
                        Submission s = mSubmissionsAdapter.getItem(position);
                        if (s.getVoteStatus() == Submission.DOWNVOTED) {
                            new VoteAsyncTask(s.getName(), mUser, VoteAsyncTask.NEUTRAL).execute();
                            s.setVoteStatus(Submission.NEUTRAL);
                        } else {
                            new VoteAsyncTask(s.getName(), mUser, VoteAsyncTask.DOWNVOTE).execute();
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
                    } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                        int position = mSubmissions.pointToPosition((int) e1.getX(), (int) e1.getY());
                        Submission s = mSubmissionsAdapter.getItem(position);
                        if (s.getVoteStatus() == Submission.UPVOTED) {
                            new VoteAsyncTask(s.getName(), mUser, VoteAsyncTask.NEUTRAL).execute();
                            s.setVoteStatus(Submission.NEUTRAL);
                        } else {
                            new VoteAsyncTask(s.getName(), mUser, VoteAsyncTask.UPVOTE).execute();
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
                } catch (Exception e) {
                    // nothing
                }
            }
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return super.onDown(e);
        }

    }
}
