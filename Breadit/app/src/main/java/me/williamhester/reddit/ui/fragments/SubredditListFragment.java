package me.williamhester.reddit.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Collections;

import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.Subreddit;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 5/26/15.
 */
public class SubredditListFragment extends AccountFragment {

  public static final String SELECTED_SUBREDDIT = "selectedSubreddit";
  public static final String TRENDING_SUBREDDITS = "trendingSubreddits";
  public static final String SUBSCRIPTIONS = "subscriptions";

  private final ArrayList<String> mTrendingSubreddits = new ArrayList<>();
  private final ArrayList<Subreddit> mSubscriptions = new ArrayList<>();
  private SubredditsExpandableListAdapter mAdapter;

  /**
   * Creates a new SubredditListFragment in an empty state.
   *
   * @return a new SubredditListFragment.
   */
  public static SubredditListFragment newInstance() {
    Bundle args = new Bundle();
    SubredditListFragment fragment = new SubredditListFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (mTrendingSubreddits.size() == 0) {
      RedditApi.getTrendingSubreddits(getActivity(), new FutureCallback<ArrayList<String>>() {
        @Override
        public void onCompleted(Exception e, ArrayList<String> result) {
          if (e != null) {

          } else {
            mTrendingSubreddits.clear();
            mTrendingSubreddits.addAll(result);
            mAdapter.notifyTrendingSubredditsLoaded();
          }
        }
      });
    }
  }

  @Override
  public void onAccountChanged() {
  }

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_subreddit_list, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    ExpandableListView elv = (ExpandableListView) view.findViewById(R.id.expandable_list_view);
    mAdapter = new SubredditsExpandableListAdapter();
    elv.setAdapter(mAdapter);
    elv.expandGroup(SubredditsExpandableListAdapter.SUBSCRIPTIONS_GROUP_INDEX);
    elv.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
      @Override
      public boolean onChildClick(ExpandableListView parent, View v, int groupPosition,
                                  int childPosition, long id) {
        switch (groupPosition) {
          case SubredditsExpandableListAdapter.SPECIAL_GROUP_INDEX:
            selectSubreddit(mAdapter.mSpecials[childPosition]);
            return true;
          case SubredditsExpandableListAdapter.TRENDING_GROUP_INDEX:
            selectSubreddit(mTrendingSubreddits.get(childPosition));
            return true;
          case SubredditsExpandableListAdapter.SUBSCRIPTIONS_GROUP_INDEX:
            selectSubreddit(mSubscriptions.get(childPosition).getDisplayName());
            return true;
        }
        return false;
      }
    });
    setUpHeader();
  }

  private void setUpHeader() {
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });
    final EditText subreddit = (EditText) mToolbar.findViewById(R.id.go_to_subreddit);
    subreddit.setOnKeyListener(new View.OnKeyListener() {
      @Override
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_ENTER) {
          selectSubreddit(subreddit.getText().toString());
          return true;
        }
        return false;
      }
    });
  }

  /**
   * Sets the list of subreddits that will be displayed in the last section of the
   * ExpandableListViewAdapter.
   *
   * @param subs the list of subreddits to put into the adapter.
   */
  public void setSubreddits(ArrayList<Subreddit> subs) {
    mSubscriptions.clear();
    mSubscriptions.addAll(subs);
    Collections.sort(mSubscriptions);
    mAdapter.notifyDataSetChanged();
  }

  private void selectSubreddit(String subreddit) {
    if (subreddit.equals(mAdapter.mSpecials[0])) {
      subreddit = "";
    }
    Intent i = new Intent();
    i.putExtra(SELECTED_SUBREDDIT, subreddit);
    getActivity().setResult(Activity.RESULT_OK, i);
    getActivity().finish();
  }

  private class SubredditsExpandableListAdapter extends BaseExpandableListAdapter {

    public static final int SPECIAL_GROUP_INDEX = 0;
    public static final int TRENDING_GROUP_INDEX = 1;
    public static final int SUBSCRIPTIONS_GROUP_INDEX = 2;

    private final String[] mSpecials;
    private LayoutInflater mLayoutInflater;

    public SubredditsExpandableListAdapter() {
      mSpecials = getResources().getStringArray(R.array.special_subs);
      mLayoutInflater = (LayoutInflater) getActivity()
          .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
      return 3;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
      switch (groupPosition) {
        case SPECIAL_GROUP_INDEX:
          return mSpecials.length;
        case TRENDING_GROUP_INDEX:
          return mTrendingSubreddits.size();
        case SUBSCRIPTIONS_GROUP_INDEX:
          return mSubscriptions.size();
      }
      return 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
      return null;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
      return null;
    }

    @Override
    public long getGroupId(int groupPosition) {
      return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
      switch (groupPosition) {
        case SPECIAL_GROUP_INDEX:
          return mSpecials[childPosition].hashCode();
        case TRENDING_GROUP_INDEX:
          return mTrendingSubreddits.get(childPosition).hashCode();
        case SUBSCRIPTIONS_GROUP_INDEX:
          return mSubscriptions.get(childPosition).getName().hashCode();
      }
      return 0;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
      if (convertView == null) {
        convertView = mLayoutInflater.inflate(R.layout.list_item_subreddit_group, parent, false);
        convertView.setTag(new SubredditGroupViewHolder(convertView));
      }
      SubredditGroupViewHolder vh = (SubredditGroupViewHolder) convertView.getTag();

      int title = 0;
      switch (groupPosition) {
        case SPECIAL_GROUP_INDEX:
          title = R.string.special;
          break;
        case TRENDING_GROUP_INDEX:
          title = R.string.trending;
          break;
        case SUBSCRIPTIONS_GROUP_INDEX:
          title = AccountManager.isLoggedIn() ? R.string.subscriptions : R.string.defaults;
          break;
      }
      vh.setContent(title);

      return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
      if (convertView == null) {
        convertView = mLayoutInflater.inflate(R.layout.list_item_subreddit, parent, false);
        convertView.setTag(new SubredditListItemViewHolder(convertView));
      }
      SubredditListItemViewHolder vh = (SubredditListItemViewHolder) convertView.getTag();

      switch (groupPosition) {
        case SPECIAL_GROUP_INDEX:
          vh.setContent(mSpecials[childPosition], false);
          break;
        case TRENDING_GROUP_INDEX:
          vh.setContent(mTrendingSubreddits.get(childPosition), false);
          break;
        case SUBSCRIPTIONS_GROUP_INDEX:
          Subreddit sub = mSubscriptions.get(childPosition);
          vh.setContent(sub.getDisplayName(), sub.userIsModerator());
          break;
      }

      return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
      return true;
    }

    public void notifyTrendingSubredditsLoaded() {
      notifyDataSetChanged();
    }

    private class SubredditListItemViewHolder {
      private TextView mTitle;
      private TextView mModerator;

      public SubredditListItemViewHolder(View view) {
        mTitle = (TextView) view.findViewById(R.id.subreddit_list_item_title);
        mModerator = (TextView) view.findViewById(R.id.moderator_status);
      }

      public void setContent(String subreddit, boolean isModerator) {
        mTitle.setText(subreddit);
        mModerator.setVisibility(isModerator ? View.VISIBLE : View.GONE);
      }
    }

    private class SubredditGroupViewHolder {
      private TextView mTitle;

      public SubredditGroupViewHolder(View view) {
        mTitle = (TextView) view.findViewById(R.id.subreddit_group_title);
      }

      public void setContent(int title) {
        mTitle.setText(title);
      }
    }
  }
}
