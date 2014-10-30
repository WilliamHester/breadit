package me.williamhester.ui.fragments;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

import me.williamhester.models.AbsComment;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Comment;
import me.williamhester.models.Listing;
import me.williamhester.models.MoreComments;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.User;
import me.williamhester.models.Votable;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.SubmissionViewHolder;
import me.williamhester.ui.views.VotableViewHolder;

public class UserFragment extends AccountFragment {

    private String mUsername;
    private User mUser;

    private Context mContext;
    private final ArrayList<Votable> mSubmittedThings = new ArrayList<>();
    private VotableArrayAdapter mSubmittedAdapter;
    private TextView mCommentKarma;
    private TextView mLinkKarma;
    private TextView mCakeDay;

    public static UserFragment newInstance(String username) {
        Bundle b = new Bundle();
        b.putString("username", username);
        UserFragment fragment = new UserFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mUsername = getArguments().getString("username");
        }
        mAccount = AccountManager.getAccount();
        if (mUsername == null && mAccount != null) {
            mUsername = mAccount.getUsername();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, root, false);
        ListView listView = (ListView) v.findViewById(R.id.submitted);
        mSubmittedAdapter = new VotableArrayAdapter();
        listView.addHeaderView(createHeaderView(inflater));
        listView.setAdapter(mSubmittedAdapter);
        listView.setOnScrollListener(new InfiniteLoadingScrollListener());
        RedditApi.getUserDetails(getActivity(), mUsername, null, mSubmittedThingsCallback);
        new LoadUserDataTask().execute();
        return v;
    }

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.header_user, null);
        TextView username = (TextView) v.findViewById(R.id.username);
        mCommentKarma = (TextView) v.findViewById(R.id.comment_karma);
        mLinkKarma = (TextView) v.findViewById(R.id.link_karma);
        mCakeDay = (TextView) v.findViewById(R.id.cakeday);
        username.setText("/u/" + mUsername);
        return v;
    }

    private FutureCallback<JsonObject> mSubmittedThingsCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            Gson gson = new Gson();
            ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, gson);
            Listing listing = null;
            if (wrapper.getData() instanceof Listing) {
                listing = (Listing) wrapper.getData();
                for (ResponseRedditWrapper wrap : listing.getChildren()) {
                    mSubmittedThings.add((Votable) wrap.getData());
                }
                mSubmittedAdapter.notifyDataSetChanged();
            }
        }
    };

    private class VotableArrayAdapter extends BaseAdapter implements
            CommentViewHolder.CommentClickCallbacks, SubmissionViewHolder.SubmissionCallbacks {

        @Override
        public int getCount() {
            return mSubmittedThings.size();
        }

        @Override
        public Votable getItem(int position) {
            return mSubmittedThings.get(position);
        }

        @Override
        public long getItemId(int position) {
            return Long.parseLong(mSubmittedThings.get(position).getId(), 36);
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Votable v = getItem(position);
            if (v instanceof Comment) {
                if (convertView == null || convertView.getTag() instanceof SubmissionViewHolder) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item_comment_card, parent, false);
                    convertView.setTag(new CommentViewHolder(convertView, this, mUsername));
                }
            } else {
                if (convertView == null || convertView.getTag() instanceof CommentViewHolder) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item_post, parent, false);
                    convertView.setTag(new SubmissionViewHolder(convertView, this));
                }
            }
            ((VotableViewHolder) convertView.getTag()).setContent(v);
            return convertView;
        }

        @Override
        public void onBodyClick(CommentViewHolder viewHolder, Comment comment) {

        }

        @Override
        public void onCommentLongPressed(CommentViewHolder holder) {

        }

        @Override
        public void onOptionsRowItemSelected(View view, Comment comment) {

        }

        @Override
        public void onImageViewClicked(Object imgurData) {

        }

        @Override
        public void onImageViewClicked(String imageUrl) {

        }

        @Override
        public void onYouTubeVideoClicked(String videoId) {

        }

        @Override
        public void onCardClicked(Submission submission) {

        }

        @Override
        public void onCardLongPressed(SubmissionViewHolder holder) {

        }

        @Override
        public void onOptionsRowItemSelected(View view, Submission submission) {

        }

        @Override
        public boolean isFrontPage() {
            return false;
        }
    }

    public class InfiniteLoadingScrollListener implements AbsListView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int previousTotal = 0;
        private boolean loading = true;

        @Override
        public void onScrollStateChanged(AbsListView absListView, int i) {

        }

        @Override
        public void onScroll(AbsListView absListView, int i, int i2, int i3) {
            if (loading) {
                if (mSubmittedAdapter.getCount() > previousTotal) {
                    previousTotal = mSubmittedAdapter.getCount();
                    loading = false;
                }
            } else if (mSubmittedThings.size() > 0
                    && (mSubmittedThings.size() - absListView.getChildCount()) // 25 - 4 = 21
                    <= (absListView.getFirstVisiblePosition() + VISIBLE_THRESHOLD)) { // 20 + 5 = 25
                loadMoreVotables();
                loading = true;
            }
        }
    }

    private void loadMoreVotables() {
        String after;
        if (mSubmittedThings.size() == 0) {
            after = null;
        } else {
            after = mSubmittedThings.get(mSubmittedThings.size() - 1).getName();
        }
        RedditApi.getUserDetails(getActivity(), mUsername, after, mSubmittedThingsCallback);
    }

    private class LoadUserDataTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... voids) {
            try {
                return User.getUser(mUsername, mAccount);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (User.UserNotFoundException e) {
                // Tell the user that the user could not be found.
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(User result) {
            if (result != null) {
                mUser = result;
                DecimalFormat format = new DecimalFormat("###,###,###,##0");
                mLinkKarma.setText(format.format(result.getLinkKarma()) + " Link karma");
                mCommentKarma.setText(format.format(result.getCommentKarma()) + " Comment karma");
                mCakeDay.setText(mUser.calculateCakeDay());
            }
            mSubmittedAdapter.notifyDataSetChanged();
        }
    }

}
