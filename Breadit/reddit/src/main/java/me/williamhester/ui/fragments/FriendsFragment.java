package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import me.williamhester.models.Friend;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.views.DividerItemDecoration;

/**
 * FriendsFragment is a simple fragment that shows the currently logged in user's friends.
 */
public class FriendsFragment extends AccountFragment {

    private final ArrayList<Friend> mFriends = new ArrayList<>();
    private FriendsAdapter mFreindsAdapter;
    private ProgressBar mProgressBar;
    private TopLevelFragmentCallbacks mCallback;
    private boolean mHasFetchedFriends;
    private boolean mLoading;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mLoading = savedInstanceState.getBoolean("loading");
            mHasFetchedFriends = savedInstanceState.getBoolean("hasFetchedFriends");
            ArrayList<Friend> friends = savedInstanceState.getParcelableArrayList("friends");
            mFriends.addAll(friends);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_friends, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);
        toolbar.setTitle(R.string.friends);
        toolbar.setNavigationIcon(R.drawable.ic_drawer);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onHomeClicked();
            }
        });
        mFreindsAdapter = new FriendsAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.friends);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(mFreindsAdapter);
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
                mFreindsAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onAccountChanged() {
        mFriends.clear();
        mFreindsAdapter.notifyDataSetChanged();
        loadFriends();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("loading", mLoading);
        outState.putBoolean("hasFetchedFriends", mHasFetchedFriends);
        outState.putParcelableArrayList("friends", mFriends);
    }

    private class FriendsAdapter extends RecyclerView.Adapter<FriendViewHolder> {

        @Override
        public FriendViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return new FriendViewHolder(inflater.inflate(R.layout.list_item_friend, parent, false));
        }

        @Override
        public void onBindViewHolder(FriendViewHolder holder, int position) {
            holder.setContent(mFriends.get(position));
        }

        @Override
        public int getItemCount() {
            return mFriends.size();
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
