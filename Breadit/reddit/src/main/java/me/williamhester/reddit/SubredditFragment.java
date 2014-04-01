package me.williamhester.reddit;

import android.app.ActionBar;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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

    /**
     * This method is called to begin the list of submissions. It is called during onCreate and
     *     when the SwipeRefreshLayout's onRefresh method is called.
     */
    private void populateSubmissions() {
        mNames = new HashSet<String>();
        mSubmissionList = new ArrayList<Submission>();
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
                b.putParcelable("user", mUser);
                i.putExtras(b);
                mContext.startActivity(i);
            }
        });
        new RefreshUserClass().execute();
    }

    private class SubmissionArrayAdapter extends ArrayAdapter<Submission> {
        Context mContext;

        public SubmissionArrayAdapter(Context context) {
            super(context, R.layout.list_item_link_post, mSubmissionList);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            LayoutInflater inflater = (LayoutInflater)
                    mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final Submission s = (Submission) mSubmissions.getItemAtPosition(position);

            if (convertView == null)
                convertView = inflater.inflate(R.layout.list_item_link_post, parent, false);

            Button ups = (Button) convertView.findViewById(R.id.ups);
            Button downs = (Button) convertView.findViewById(R.id.downs);
//            LinearLayout background = (LinearLayout) convertView.findViewById(R.id.background);
            TextView subredditName = (TextView) convertView.findViewById(R.id.subreddit_name);
            TextView time = (TextView) convertView.findViewById(R.id.time);
//            TextView score = (TextView) convertView.findViewById(R.id.score);
            ImageView image = (ImageView) convertView.findViewById(R.id.thumbnail);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            View spacer = convertView.findViewById(R.id.spacer);

            ups.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new VoteAsyncTask(getItem(position).getName(), mUser, VoteAsyncTask.UPVOTE).execute();
                    Toast.makeText(mContext, getItem(position).getName(), Toast.LENGTH_SHORT).show();
                }
            });
            downs.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new VoteAsyncTask(getItem(position).getName(), mUser, VoteAsyncTask.DOWNVOTE).execute();
                }
            });

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
//            score.setText(s.getScore() + "");
            title.setText(s.getTitle());
            ups.setText(s.getUpVotes() + "");
            downs.setText(s.getDownVotes() + "");
            
            return convertView;
        }
    }

    private String calculateTime(long postTime, long currentTime) {
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
                    submissions.add(new Submission(jsonData));
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
                if (result.size() == 0) {
//                mSubmissions.addFooterView(v);
                }
                for (Submission s : result) {
                    if (!mNames.contains(s.getName())) {
                        mSubmissionList.add(s);
                        mNames.add(s.getName());
//                        Log.i("SubredditFragment", "Submission added");
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
