package me.williamhester.reddit.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import me.williamhester.reddit.BreaditApplication;
import me.williamhester.reddit.databases.AccountDataSource;
import me.williamhester.knapsack.Knapsack;
import me.williamhester.knapsack.Save;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.Subreddit;
import me.williamhester.reddit.network.Callback;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.fragments.SubredditListFragment;

/**
 * Created by william on 5/27/15.
 */
public class SelectSubredditActivity extends AppCompatActivity {

  @Inject RedditApi mApi;

  @Save ArrayList<Subreddit> mSubreddits = new ArrayList<>();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    Knapsack.restore(this, savedInstanceState);
    BreaditApplication application = (BreaditApplication) getApplicationContext();
    application.getApiComponent().inject(this);

    setContentView(R.layout.activity_container);

    Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_container);
    if (frag == null) {
      frag = SubredditListFragment.newInstance();
      getSupportFragmentManager().beginTransaction()
          .add(R.id.main_container, frag, "ListFragment")
          .commit();
    }

    if (mSubreddits.size() == 0) {
      loadSubreddits();
    }
  }

  private void loadSubreddits() {
    if (AccountManager.getAccount() != null) {
      final ArrayList<Subreddit> subredditList = new ArrayList<>();
      AccountDataSource dataSource = new AccountDataSource(this);
      dataSource.open();
      subredditList.addAll(dataSource.getCurrentAccountSubreddits());
      dataSource.close();
      HashMap<String, Subreddit> subscriptions = AccountManager.getAccount().getSubscriptions();
      for (Subreddit s : subredditList) {
        subscriptions.put(s.getDisplayName().toLowerCase(), s);
      }
      Collections.sort(subredditList);

      mApi.getSubscribedSubreddits(new Callback<List<Subreddit>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onSuccess(final List<Subreddit> data) {
          Log.d("SelectSubredditActivity", "" + data.size());

          AccountDataSource dataSource = new AccountDataSource(SelectSubredditActivity.this);
          dataSource.open();
          ArrayList<Subreddit> allSubs = dataSource.getAllSubreddits();
          ArrayList<Subreddit> savedSubscriptions = dataSource.getCurrentAccountSubreddits();

          for (Subreddit s : data) {
            int index = allSubs.indexOf(s); // Get the subreddit WITH the table id
            if (index < 0) { // if it doesn't exist, create one with a table id
              dataSource.addSubreddit(s);
              dataSource.addSubscriptionToCurrentAccount(s);
            } else if (!savedSubscriptions.contains(s)) {
              dataSource.addSubscriptionToCurrentAccount(allSubs.get(index));
            }
          }

          dataSource.close();

          boolean isNew = !data.equals(savedSubscriptions);

          if (isNew) {
            subredditList.clear();
            subredditList.addAll(data);
          }

          Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
          if (f instanceof SubredditListFragment) {
            mSubreddits.addAll(data);
            ((SubredditListFragment) f).setSubreddits(mSubreddits);
          }
        }
      });
    } else {
      mApi.getDefaultSubreddits(new Callback<List<Subreddit>>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onSuccess(List<Subreddit> data) {
          Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
          if (f instanceof SubredditListFragment) {
            mSubreddits.addAll(data);
            ((SubredditListFragment) f).setSubreddits(mSubreddits);
          }
        }
      });
    }
  }
}
