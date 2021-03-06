package me.williamhester.reddit.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.CardView;
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

import butterknife.Bind;
import me.williamhester.knapsack.Save;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.Comment;
import me.williamhester.reddit.models.reddit.GenericResponseWrapper;
import me.williamhester.reddit.models.reddit.Listing;
import me.williamhester.reddit.models.reddit.Submission;
import me.williamhester.reddit.models.reddit.ResponseWrapper;
import me.williamhester.reddit.models.reddit.User;
import me.williamhester.reddit.models.reddit.Votable;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.activities.BrowseActivity;
import me.williamhester.reddit.ui.views.CommentViewHolder;
import me.williamhester.reddit.ui.views.DividerItemDecoration;
import me.williamhester.reddit.ui.views.SubmissionCommentViewHolder;
import me.williamhester.reddit.ui.views.SubmissionViewHolder;
import me.williamhester.reddit.ui.views.VotableViewHolder;
import me.williamhester.reddit.ui.widget.InfiniteLoadToolbarHideScrollListener;

public class UserFragment extends AccountFragment implements Toolbar.OnMenuItemClickListener,
    SubmissionViewHolder.SubmissionCallbacks,
    SubmissionCommentViewHolder.SubmissionCommentCallbacks,
    InfiniteLoadToolbarHideScrollListener.OnLoadMoreListener {

  public static final int VOTE_REQUEST_CODE = 1;

  private String mUsername;

  private TopLevelFragmentCallbacks mCallback;
  private VotableAdapter mAdapter;
  private VotableViewHolder mFocusedVotable;
  private InfiniteLoadToolbarHideScrollListener mScrollListener;

  private TextView mCakeDay;
  private TextView mCommentKarma;
  private TextView mLinkKarma;
  private View mUserHeader;
  @Bind(R.id.toolbar_actionbar) Toolbar mToolbar;
  @Bind(R.id.progress_bar) ProgressBar mProgressBar;
  @Bind(R.id.swipe_refresh) SwipeRefreshLayout mRefreshLayout;

  @Save boolean mLoading;
  @Save boolean mRefreshing;
  @Save ArrayList<Votable> mRedditVotables = new ArrayList<>();
  @Save String mFilterType = User.OVERVIEW;
  @Save
  User mUser;

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

    if (getArguments() != null) {
      mUsername = getArguments().getString("username");
    }

    if (mUsername == null && mAccount != null) {
      mUsername = mAccount.getUsername();
    } else if (mAccount == null) {
      throw new IllegalStateException("A username must be provided.");
    }
  }

  @Override
  public void onAccountChanged() {

  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_user, root, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    View headerBar = view.findViewById(R.id.header_bar);
    if (mCallback != null) {
      mToolbar.setNavigationIcon(R.drawable.ic_drawer);
      mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View v) {
          mCallback.onHomeClicked();
        }
      });
    } else {
      mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
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

    mProgressBar.setVisibility(mLoading ? View.VISIBLE : View.GONE);

    mRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.primary);
    mRefreshLayout.setColorSchemeResources(R.color.white);
    mRefreshLayout.setRefreshing(mRefreshing);
    mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
      @Override
      public void onRefresh() {
        mRefreshing = true;
        mRedditVotables.clear();
        mAdapter.notifyDataSetChanged();
        mScrollListener.resetState();
        RedditApi.getUserContent(getActivity(), mUsername, null, mFilterType, mThingsCallback);
      }
    });
    mToolbar.post(new Runnable() {
      @Override
      public void run() {
        float density = getResources().getDisplayMetrics().density;
        int startAt = mToolbar.getHeight() - (int) (40 * density);
        int endAt = startAt + (int) (64 * density);
        mRefreshLayout.setProgressViewOffset(false, startAt, endAt);
      }
    });

    LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
    mAdapter = new VotableAdapter();
    RecyclerView votableRecyclerView = (RecyclerView) view.findViewById(R.id.content_list);
    votableRecyclerView.setLayoutManager(layoutManager);
    votableRecyclerView.addItemDecoration(new DividerItemDecoration(
        getResources().getDrawable(R.drawable.card_divider)));
    votableRecyclerView.setAdapter(mAdapter);
    mScrollListener = new InfiniteLoadToolbarHideScrollListener(mAdapter, headerBar,
        votableRecyclerView, mRedditVotables, layoutManager, this);
    votableRecyclerView.addOnScrollListener(mScrollListener);

    final Spinner spinner = (Spinner) view.findViewById(R.id.user_spinner);
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
          mRedditVotables.clear();
          mAdapter.notifyDataSetChanged();
          RedditApi.getUserContent(getActivity(), mUsername, null, mFilterType, mThingsCallback);
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> parent) {

      }
    });

    mLoading = true;
    if (mRedditVotables.size() == 0) {
      RedditApi.getUserContent(getActivity(), mUsername, null, mFilterType, mThingsCallback);
    }
    if (mUser == null) {
      RedditApi.getUserAbout(getActivity(), mUsername, new FutureCallback<GenericResponseWrapper<User>>() {
        @Override
        public void onCompleted(Exception e, GenericResponseWrapper<User> result) {
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
          updateHeaderView();
        }
      });
    }
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

  private View createHeaderView(LayoutInflater inflater, ViewGroup parent) {
    mUserHeader = inflater.inflate(R.layout.header_user, parent, false);
    TextView username = (TextView) mUserHeader.findViewById(R.id.username);
    mCommentKarma = (TextView) mUserHeader.findViewById(R.id.comment_karma);
    mLinkKarma = (TextView) mUserHeader.findViewById(R.id.link_karma);
    mCakeDay = (TextView) mUserHeader.findViewById(R.id.cakeday);
    username.setText("/u/" + mUsername);

    View friend = mUserHeader.findViewById(R.id.friends);
    friend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
//                RedditApi.friend
      }
    });
    View message = mUserHeader.findViewById(R.id.message);
    message.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Fragment f = ComposeMessageFragment.newInstance(mUsername);
        getFragmentManager().beginTransaction()
            .replace(R.id.main_container, f, "message")
            .addToBackStack("message")
            .commit();
      }
    });

    updateHeaderView();
    return mUserHeader;
  }

  private void updateHeaderView() {
    if (mUser != null) {
      DecimalFormat format = new DecimalFormat("###,###,###,##0");
      mLinkKarma.setText(format.format(mUser.getSubmissionPoints()));
      mCommentKarma.setText(format.format(mUser.getCommentPoints()));
      mCakeDay.setText(mUser.calculateCakeDay());

      View message = mUserHeader.findViewById(R.id.message);
      message.setVisibility(!mUser.isLoggedInAccount() && AccountManager.isLoggedIn() ?
          View.VISIBLE : View.GONE);
      View friend = mUserHeader.findViewById(R.id.friends);
      friend.setVisibility(!mUser.isLoggedInAccount() && AccountManager.isLoggedIn() ?
          View.VISIBLE : View.GONE);
    }
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
      ResponseWrapper wrapper = new ResponseWrapper(result, gson);
      if (wrapper.getData() instanceof Listing) {
        Listing listing = (Listing) wrapper.getData();
        for (ResponseWrapper wrap : listing.getChildren()) {
          mRedditVotables.add((Votable) wrap.getData());
        }
        mAdapter.notifyDataSetChanged();
      }
    }
  };

  @Override
  public void onCardClicked(Submission submission) {
    Intent i = new Intent(getActivity(), BrowseActivity.class);
    Bundle args = new Bundle();
    args.putString("type", "comments");
    args.putParcelable("submission", submission);
    args.putParcelable("media", submission.getMedia());
    i.putExtras(args);
    startActivityForResult(i, VOTE_REQUEST_CODE);
  }

  @Override
  public void onCardLongPressed(SubmissionViewHolder holder) {
    holder.expandOptions();
    if (mFocusedVotable != null) {
      mFocusedVotable.collapseOptions();
      if (mFocusedVotable == holder) {
        mFocusedVotable = null;
        return;
      }
    }
    mFocusedVotable = holder;
  }

  @Override
  public boolean onOptionsRowItemSelected(int itemId, Submission submission) {
    return false;
  }

  @Override
  public boolean isFrontPage() {
    return false;
  }

  @Override
  public void onBodyClick(CommentViewHolder viewHolder, Comment redditComment) {
    String permalink = "/r/" + redditComment.getSubreddit() + "/comments/"
        + redditComment.getLinkId().substring(3) + "/breadit/"
        + redditComment.getParentId().substring(3) + "?context=3";
    Bundle extras = new Bundle();
    extras.putString("type", "comments");
    extras.putString("permalink", permalink);
    extras.putBoolean("isSingleThread", true);
    Intent i = new Intent(getActivity(), BrowseActivity.class);
    i.putExtras(extras);
    startActivity(i);
  }

  @Override
  public void onCommentLongPressed(CommentViewHolder holder) {
    if (holder != null) {
      holder.expandOptions();
    }
    if (mFocusedVotable != null) {
      mFocusedVotable.collapseOptions();
      if (mFocusedVotable == holder) {
        mFocusedVotable = null;
        return;
      }
    }
    mFocusedVotable = holder;
  }

  @Override
  public void onOptionsRowItemSelected(View view, Comment redditComment) {

  }

  @Override
  public String getSubmissionAuthor() {
    return null;
  }

  @Override
  public void onLinkClicked(Comment redditComment) {
    String permalink = "/r/" + redditComment.getSubreddit() + "/comments/" + redditComment.getLinkId().substring(3);
    Bundle extras = new Bundle();
    extras.putString("permalink", permalink);
    extras.putString("type", "comments");
    Intent i = new Intent(getActivity(), BrowseActivity.class);
    i.putExtras(extras);
    startActivity(i);
  }

  @Override
  public void onLoadMore() {
    mProgressBar.setVisibility(View.VISIBLE);
    String after;
    if (mRedditVotables.size() == 0) {
      after = null;
    } else {
      after = mRedditVotables.get(mRedditVotables.size() - 1).getId();
    }
    RedditApi.getUserContent(getActivity(), mUsername, after, mFilterType, mThingsCallback);
  }

  private class VotableAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int SUBMISSION = 1;
    public static final int COMMENT = 2;
    public static final int VOTABLE_MASK = 3;
    public static final int USER_HEADER = 6;
    public static final int FOOTER = 4;
    public static final int HEADER_SPACER = 8;

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      LayoutInflater inflater = LayoutInflater.from(getActivity());
      switch (viewType) {
        case HEADER_SPACER: {
          LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT, mToolbar.getHeight());
          View header = new View(getActivity());
          header.setLayoutParams(params);
          return new RecyclerView.ViewHolder(header) {
          };
        }
        case USER_HEADER: {
          return new RecyclerView.ViewHolder(createHeaderView(inflater, parent)) {
          };
        }
        case SUBMISSION: {
          return new SubmissionViewHolder(
              inflater.inflate(R.layout.list_item_submission, parent, false),
              UserFragment.this);
        }
        case COMMENT: {
          CardView cardView = (CardView) inflater.inflate(R.layout.view_content_card, parent, false);
          return new SubmissionCommentViewHolder(
              inflater.inflate(R.layout.list_item_submission_comment, cardView, true),
              UserFragment.this);
        }
        case FOOTER: {
          return new RecyclerView.ViewHolder(inflater.inflate(R.layout.footer_spacer, parent,
              false)) {
          };
        }
      }
      return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
      if (holder instanceof VotableViewHolder) {
        ((VotableViewHolder) holder).setContent(mRedditVotables.get(position - 2));
      }
    }

    @Override
    public int getItemCount() {
      return mRedditVotables.size() + 3;
    }

    @Override
    public int getItemViewType(int position) {
      if (position == getItemCount() - 1) {
        return FOOTER;
      }
      if (position == 0) {
        return HEADER_SPACER;
      }
      if (position == 1) {
        return USER_HEADER;
      }
      return mRedditVotables.get(position - 2) instanceof Comment ? COMMENT : SUBMISSION;
    }
  }

}
