package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import me.williamhester.knapsack.Save;
import me.williamhester.models.Friend;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.widget.InfiniteLoadToolbarHideScrollListener;

/**
 * FriendsFragment is a simple fragment that shows the currently logged in user's friends.
 */
public class FriendsFragment extends AccountFragment {

    private FriendsAdapter mFriendsAdapter;
    private ProgressBar mProgressBar;
    private Toolbar mToolbar;
    private TopLevelFragmentCallbacks mCallback;
    @Save ArrayList<Friend> mFriends = new ArrayList<>();
    @Save boolean mHasFetchedFriends;
    @Save boolean mLoading;

    public static FriendsFragment newInstance() {
        return new FriendsFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof TopLevelFragmentCallbacks) {
            mCallback = (TopLevelFragmentCallbacks) activity;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);
        mToolbar.setTitle(R.string.friends);
        mToolbar.setNavigationIcon(R.drawable.ic_drawer);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onHomeClicked();
            }
        });
        mFriendsAdapter = new FriendsAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.friends);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mFriendsAdapter);
        recyclerView.setOnScrollListener(new InfiniteLoadToolbarHideScrollListener(mFriendsAdapter,
                mToolbar, recyclerView, mFriends, layoutManager, null));
        recyclerView.addItemDecoration(
                new DividerItemDecoration(getResources().getDrawable(R.drawable.card_divider)));

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);

        if (!mLoading) {
            mProgressBar.setVisibility(View.GONE);
        }
        if (!mHasFetchedFriends && !mLoading) {
            loadFriends();
        }
        return v;
    }

    public void loadFriends() {
        mLoading = true;
        RedditApi.getFriends(new FutureCallback<ArrayList<Friend>>() {
            @Override
            public void onCompleted(Exception e, ArrayList<Friend> result) {
                mLoading = false;
                mProgressBar.setVisibility(View.GONE);
                if (e != null) {
                    return;
                }
                mHasFetchedFriends = true;
                mFriends.addAll(result);
                mFriendsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAccountChanged() {
        mFriends.clear();
        mFriendsAdapter.notifyDataSetChanged();
        loadFriends();
    }

    private class FriendsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private static final int VIEW_TYPE_HEADER = 0;
        private static final int VIEW_TYPE_FRIEND = 1;
        private static final int VIEW_TYPE_FOOTER = 2;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            switch (viewType) {
                case VIEW_TYPE_HEADER:
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, mToolbar.getHeight());
                    View header = new View(getActivity());
                    header.setLayoutParams(params);
                    return new RecyclerView.ViewHolder(header) { };
                case VIEW_TYPE_FRIEND:
                    return new FriendViewHolder(inflater.inflate(R.layout.list_item_friend, parent,
                            false));
                case VIEW_TYPE_FOOTER:
                    return new RecyclerView.ViewHolder(inflater.inflate(R.layout.footer_spacer,
                            parent, false)) {};
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (getItemViewType(position) == VIEW_TYPE_FRIEND) {
                ((FriendViewHolder) holder).setContent(mFriends.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return mFriends.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return VIEW_TYPE_FOOTER;
            } else if (position == 0) {
                return VIEW_TYPE_HEADER;
            } else {
                return VIEW_TYPE_FRIEND;
            }
        }
    }

    private class FriendViewHolder extends RecyclerView.ViewHolder {

        private Friend mFriend;
        private TextView mUsername;
        private TextView mFriendsSince;

        public FriendViewHolder(View itemView) {
            super(itemView);

            View relativeLayout = itemView.findViewById(R.id.relative_layout);
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Bundle b = new Bundle();
                    b.putString("type", "user");
                    b.putString("username", mFriend.getName());
                    Intent i = new Intent(getActivity(), BrowseActivity.class);
                    i.putExtras(b);
                    getActivity().startActivity(i);
                }
            });

            mUsername = (TextView) itemView.findViewById(R.id.username);
            mFriendsSince = (TextView) itemView.findViewById(R.id.friends_since);
        }

        public void setContent(Friend friend) {
            mFriend = friend;
            mUsername.setText(friend.getName());
            Calendar date = Calendar.getInstance();
            date.setTimeInMillis(mFriend.getDate() * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, y");
            mFriendsSince.setText(getResources().getText(R.string.friends_since) + " "
                    + sdf.format(date.getTime()));
        }
    }
}
