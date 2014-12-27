package me.williamhester.ui.fragments;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;

import me.williamhester.models.Comment;
import me.williamhester.models.GenericResponseRedditWrapper;
import me.williamhester.models.Listing;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.User;
import me.williamhester.models.Votable;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.CommentViewHolder;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.SubmissionViewHolder;
import me.williamhester.ui.views.VotableViewHolder;

public class UserFragment extends AccountFragment implements Toolbar.OnMenuItemClickListener {

    private String mUsername;
    private User mUser;

    private final ArrayList<Votable> mVotables = new ArrayList<>();
    private String mFilterType = User.OVERVIEW;

    private TopLevelFragmentCallbacks mCallback;
    private VotableAdapter mAdapter;

    private InfiniteLoadingScrollListener mScrollListener;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private RecyclerView mVotableRecyclerView;
    private SwipeRefreshLayout mRefreshLayout;
    private TextView mCakeDay;
    private TextView mCommentKarma;
    private TextView mLinkKarma;
    private Toolbar mToolbar;
    private View mHeaderBar;

    private boolean mLoading;
    private boolean mRefreshing;

    public static UserFragment newInstance() {
        return new UserFragment();
    }

    public static UserFragment newInstance(String username) {
        Bundle b = new Bundle();
        b.putString("username", username);
        UserFragment fragment = new UserFragment();
        fragment.setArguments(b);
        return fragment;
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

        mFilterType = "";
        if (savedInstanceState != null) {
            mLoading = savedInstanceState.getBoolean("loading");
            mRefreshing = savedInstanceState.getBoolean("refreshing");
            mFilterType = savedInstanceState.getString("filterType");
            mUser = savedInstanceState.getParcelable("user");
            ArrayList<Votable> votables = savedInstanceState.getParcelableArrayList("votables");
            mVotables.addAll(votables);
        }
        if (getArguments() != null) {
            mUsername = getArguments().getString("username");
        }

        if (mUsername == null && mAccount != null) {
            mUsername = mAccount.getUsername();
        } else if (mAccount == null) {
            throw new IllegalStateException("A username must be provided.");
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, root, false);

        mHeaderBar = v.findViewById(R.id.header_bar);
        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);
        if (mCallback != null) {
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onHomeClicked();
                }
            });
        } else {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        }
        onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());
        mToolbar.setTitle(R.string.user);
        mToolbar.setOnMenuItemClickListener(this);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mProgressBar.setVisibility(mLoading ? View.VISIBLE : View.GONE);

        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setProgressBackgroundColor(R.color.darkest_gray);
        mRefreshLayout.setColorSchemeResources(R.color.orangered);
        mRefreshLayout.setRefreshing(mRefreshing);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshing = true;
                mVotables.clear();
                mAdapter.notifyDataSetChanged();
                mScrollListener.resetState();
                RedditApi.getUserContent(getActivity(), mUsername, null, mFilterType, mThingsCallback);
            }
        });

        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new VotableAdapter();
        mScrollListener = new InfiniteLoadingScrollListener();
        mVotableRecyclerView = (RecyclerView) v.findViewById(R.id.content_list);
        mVotableRecyclerView.setOnScrollListener(mScrollListener);
        mVotableRecyclerView.setLayoutManager(mLayoutManager);
        mVotableRecyclerView.addItemDecoration(new DividerItemDecoration(
                getResources().getDrawable(R.drawable.card_divider)));
        mVotableRecyclerView.setAdapter(mAdapter);
        mVotableRecyclerView.setOnScrollListener(new InfiniteLoadingScrollListener());

        final Spinner spinner = (Spinner) v.findViewById(R.id.user_spinner);
        int array = R.array.user_data_types;
        if (mUser != null && mUser.isLoggedInAccount()) {
            array = R.array.my_account_data_types;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_item, R.id.spinner_text,
                getResources().getStringArray(array));
        spinner.setAdapter(adapter);
        if (mFilterType.equals(User.OVERVIEW)) {
            spinner.setSelection(0);
        } else {
            String type = mFilterType.substring(0, 1).toUpperCase() + mFilterType.substring(1);
            spinner.setSelection(adapter.getPosition(type));
        }
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String type;
                switch (position) {
                    case 1:
                        type = User.COMMENTS;
                        break;
                    case 2:
                        type = User.SUBMITTED;
                        break;
                    case 3:
                        type = User.GILDED;
                        break;
                    case 4:
                        type = User.LIKED;
                        break;
                    case 5:
                        type = User.DISLIKED;
                        break;
                    case 6:
                        type = User.HIDDEN;
                        break;
                    case 7:
                        type = User.SAVED;
                        break;
                    default:
                    case 0:
                        type = User.OVERVIEW;
                        break;
                }
                if (!type.equals(mFilterType)) {
                    mFilterType = type;
                    mVotables.clear();
                    mAdapter.notifyDataSetChanged();
                    RedditApi.getUserContent(getActivity(), mUsername, null, mFilterType, mThingsCallback);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        mLoading = true;
        if (mVotables.size() == 0) {
            RedditApi.getUserContent(getActivity(), mUsername, null, mFilterType, mThingsCallback);
        }
        if (mUser == null) {
            RedditApi.getUserAbout(getActivity(), mUsername, new FutureCallback<GenericResponseRedditWrapper<User>>() {
                @Override
                public void onCompleted(Exception e, GenericResponseRedditWrapper<User> result) {
                    if (e != null) {
                        return;
                    }
                    mUser = result.getData();
                    if (mUser.isLoggedInAccount() && getActivity() != null) {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                                R.layout.spinner_item, R.id.spinner_text,
                                getResources().getStringArray(R.array.my_account_data_types));
                        spinner.setAdapter(adapter);
                    }
                    DecimalFormat format = new DecimalFormat("###,###,###,##0");
                    mLinkKarma.setText(format.format(mUser.getLinkKarma()) + " Link karma");
                    mCommentKarma.setText(format.format(mUser.getCommentKarma()) + " Comment karma");
                    mCakeDay.setText(mUser.calculateCakeDay());
                }
            });
        }
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.user, menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_compose_message:
                ComposeMessageFragment fragment = ComposeMessageFragment.newInstance(mUsername);
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment, "compose")
                        .addToBackStack("compose")
                        .commit();
                return true;
        }
        return false;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList("votables", mVotables);
        outState.putParcelable("user", mUser);
        outState.putString("filterType", mFilterType);
        outState.putBoolean("loading", mLoading);
        outState.putBoolean("refreshing", mRefreshing);
    }

    private View createHeaderView(LayoutInflater inflater, ViewGroup parent) {
        View v = inflater.inflate(R.layout.header_user, parent, false);
        TextView username = (TextView) v.findViewById(R.id.username);
        mCommentKarma = (TextView) v.findViewById(R.id.comment_karma);
        mLinkKarma = (TextView) v.findViewById(R.id.link_karma);
        mCakeDay = (TextView) v.findViewById(R.id.cakeday);
        username.setText("/u/" + mUsername);
        return v;
    }

    private FutureCallback<JsonObject> mThingsCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            mLoading = false;
            mRefreshing = false;
            mProgressBar.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);
            if (e != null) {
                e.printStackTrace();
                return;
            }
            Gson gson = new Gson();
            ResponseRedditWrapper wrapper = new ResponseRedditWrapper(result, gson);
            if (wrapper.getData() instanceof Listing) {
                Listing listing = (Listing) wrapper.getData();
                for (ResponseRedditWrapper wrap : listing.getChildren()) {
                    mVotables.add((Votable) wrap.getData());
                }
                mAdapter.notifyDataSetChanged();
            }
        }
    };

    private class VotableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public static final int HEADER_SPACER = 0;
        public static final int USER_HEADER = 1;
        public static final int SUBMISSION = 2;
        public static final int COMMENT = 3;

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            switch (viewType) {
                case HEADER_SPACER: {
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, mToolbar.getHeight());
                    View header = new View(getActivity());
                    header.setLayoutParams(params);
                    return new RecyclerView.ViewHolder(header) { };
                }
                case USER_HEADER: {
                    return new RecyclerView.ViewHolder(createHeaderView(inflater, parent)) { };
                }
                case SUBMISSION: {
                    return new SubmissionViewHolder(
                            inflater.inflate(R.layout.list_item_submission, parent, false),
                            null);
                }
                case COMMENT: {
                    return new CommentViewHolder(
                            inflater.inflate(R.layout.list_item_comment_card, parent, false),
                            null);
                }
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (position > 1) {
                ((VotableViewHolder) holder).setContent(mVotables.get(position - 2));
            }
        }

        @Override
        public int getItemCount() {
            return mVotables.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
                return HEADER_SPACER;
            }
            if (position == 1) {
                return USER_HEADER;
            }
            return mVotables.get(position - 2) instanceof Comment ? COMMENT : SUBMISSION;
        }
    }

    public class InfiniteLoadingScrollListener extends RecyclerView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int mPreviousTotal = 0;
        private boolean mLoading = true;

        @Override
        public void onScrolled(RecyclerView absListView, int dx, int dy) {
            if (mLoading) {
                if (mAdapter.getItemCount() > mPreviousTotal) {
                    mPreviousTotal = mAdapter.getItemCount();
                    mLoading = false;
                    mProgressBar.setVisibility(View.GONE);
                }
            } else if (mVotables.size() > 0
                    && (mAdapter.getItemCount() - mVotableRecyclerView.getChildCount())
                    <= (mLayoutManager.findFirstVisibleItemPosition() + VISIBLE_THRESHOLD)) {
                loadMoreVotables();
                mLoading = true;
                mProgressBar.setVisibility(View.VISIBLE);
            }

            float prevY = mHeaderBar.getTranslationY();
            mHeaderBar.setTranslationY(Math.min(Math.max(-mHeaderBar.getHeight(), prevY - dy), 0));
        }

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            switch (newState) {
                case RecyclerView.SCROLL_STATE_IDLE:
                    if (Math.abs(mHeaderBar.getTranslationY()) < mHeaderBar.getHeight() / 2
                            || mLayoutManager.findFirstVisibleItemPosition() == 0) {
                        // Need to move it back to completely visible.
                        ObjectAnimator objectAnimator =
                                ObjectAnimator.ofFloat(mHeaderBar, "translationY", mHeaderBar.getTranslationY(), 0.0F);
                        objectAnimator.setDuration((int) -mHeaderBar.getTranslationY());
                        objectAnimator.start();
                    } else {
                        // Hide the header bar.
                        ObjectAnimator objectAnimator =
                                ObjectAnimator.ofFloat(mHeaderBar, "translationY", mHeaderBar.getTranslationY(), -mHeaderBar.getHeight());
                        objectAnimator.setDuration(mHeaderBar.getHeight() - (long) mHeaderBar.getTranslationY());
                        objectAnimator.start();
                    }
                    break;
            }
        }

        public void resetState() {
            mPreviousTotal = mAdapter.getItemCount();
            mLoading = false;
            mProgressBar.setVisibility(View.GONE);
        }

        private void loadMoreVotables() {
            String after;
            if (mVotables.size() == 0) {
                after = null;
            } else {
                after = mVotables.get(mVotables.size() - 1).getName();
            }
            RedditApi.getUserContent(getActivity(), mUsername, after, mFilterType, mThingsCallback);
        }
    }

}
