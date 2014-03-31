package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getActivity() != null) {
            mAction = getActivity().getActionBar();
        }
        if (getArguments() != null) {
            mUser = getArguments().getParcelable("user");
        }
        if (mAction != null) {
            mAction.setTitle("Front page of Reddit");
        }
        mContext = getActivity();
        mSubmissionList = new ArrayList<Submission>();
        mNames = new HashSet<String>();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_subreddit, null);
        mSubmissions = (ListView) v.findViewById(R.id.submissions);
        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateSubmissions();
            }
        });
//        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_blue_bright,
//                android.R.color.holo_green_light, android.R.color.holo_orange_light,
//                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setColorScheme(android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark, android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark);
        populateSubmissions();
        return v;
    }

    private void populateSubmissions() {
        mSubmissionsAdapter = new SubmissionArrayAdapter(mContext);
        mSubmissions.setAdapter(mSubmissionsAdapter);
        mSubmissions.setOnScrollListener(new InfiniteLoadingScrollListener());
        mSubmissions.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(getActivity(), SubmissionActivity.class);
                Bundle b = new Bundle();
                b.putString("permalink", mSubmissionList.get(position).getPermalink());
                b.putString("url", mSubmissionList.get(position).getUrl());
                b.putBoolean("isSelf", mSubmissionList.get(position).isSelf());
//                b.putParcelable("user", mUser);
                i.putExtras(b);
                Log.i("BreaditDebug", "Should be starting activity");
                mContext.startActivity(i);
            }
        });
        new RefreshUserClass().execute();
    }

    private class SubmissionArrayAdapter extends ArrayAdapter<Submission> {
        Context mContext;

        private Typeface slabBold = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/RobotoSlab-Bold.ttf");
        private Typeface slabThin = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/RobotoSlab-Thin.ttf");
        private Typeface slabReg = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/RobotoSlab-Regular.ttf");

        public SubmissionArrayAdapter(Context context) {
            super(context, R.layout.list_item_link_post, mSubmissionList);
            mContext = context;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            Submission s = (Submission) mSubmissions.getItemAtPosition(position);

            View v = inflater.inflate(R.layout.list_item_link_post, parent, false);
            TextView subredditName = (TextView) v.findViewById(R.id.subreddit_name);
            TextView time = (TextView) v.findViewById(R.id.time);
            TextView score = (TextView) v.findViewById(R.id.score);
            ImageView image = (ImageView) v.findViewById(R.id.thumbnail);
            TextView title = (TextView) v.findViewById(R.id.title);
            View spacer = v.findViewById(R.id.spacer);

            // if the submission is a self post, we need to hide the thumbnail
            if (s.isSelf()) {
                image.setVisibility(View.GONE);
                spacer.setVisibility(View.GONE);
            } else {
                UrlImageViewHelper.setUrlDrawable(image, s.getThumbnailUrl());
            }

            if (mSubredditName == null || mSubredditName.equals("")) {
                subredditName.setText(s.getSubredditName());
            } else {
                subredditName.setText(s.getAuthor());
            }
            time.setText(calculateTime(s.getCreatedUtc(), System.currentTimeMillis() / 1000));
            score.setText(s.getScore() + "");
            title.setText(s.getTitle());

            subredditName.setTypeface(slabBold);
            time.setTypeface(slabBold);
            score.setTypeface(slabBold);

            return v;
        }
    }

    private String calculateTime(long postTime, long currentTime) {
        long difference = currentTime - postTime;
        String time;
        if (difference / 31536000 > 0) {
            if (difference / 3156000 == 1)
                time = "1 Year Ago";
            else
                time = difference / 3156000 + " Years Ago";
        } else if (difference / 2592000 > 0) {
            if (difference / 2592000 == 1)
                time = "1 Month Ago";
            else
                time = difference / 2592000 + " Months Ago";
        } else if (difference / 604800 > 0) {
            if (difference / 604800 == 1)
                time = "1 Week Ago";
            else
                time = difference / 604800 + " Weeks Ago";
        } else if (difference / 86400 > 0) {
            if (difference / 86400 == 1)
                time = "1 Day Ago";
            else
                time = difference / 86400 + " Day Ago";
        } else if (difference / 3600 > 0) {
            if (difference / 3600 == 1)
                time = "1 Hour Ago";
            else
                time = difference / 3600 + " Hours Ago";
        } else if (difference / 60 > 0) {
            if (difference / 60 == 1)
                time = "1 Minute Ago";
            else
                time = difference / 60 + " Minutes Ago";
        } else {
            if (difference == 1)
                time = "1 Second Ago";
            else
                time = difference + " Seconds Ago";
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
                    submissions.add(new Submission(jsonData));
                }
            } catch (MalformedURLException e) {
                exceptionText = e.toString();
                return null;
            } catch (IOException e) {
                exceptionText = e.toString();
                return null;
            } catch (NullPointerException e) {
                exceptionText = e.toString();
                return null;
            }
            return submissions;
        }

        @Override
        protected void onPostExecute(List<Submission> result) {
            if (result != null) {
                // If result has a size of 0, then we need to tell the user that they must start from
                //     the beginning because there are no more submissions that can be loaded.
                if (result.size() == 0) {
//                mSubmissions.addFooterView(v);
                }
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
}
