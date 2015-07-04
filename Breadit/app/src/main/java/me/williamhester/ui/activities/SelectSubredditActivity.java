package me.williamhester.ui.activities;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.knapsack.Knapsack;
import me.williamhester.knapsack.Save;
import me.williamhester.models.AccountManager;
import me.williamhester.models.reddit.RedditSubreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.SubredditListFragment;

/**
 * Created by william on 5/27/15.
 */
public class SelectSubredditActivity extends AppCompatActivity {

    @Save ArrayList<RedditSubreddit> mRedditSubreddits = new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Knapsack.restore(this, savedInstanceState);

        setContentView(R.layout.activity_container);

        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (frag == null) {
            frag = SubredditListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, frag, "ListFragment")
                    .commit();
        }

        if (mRedditSubreddits.size() == 0) {
            loadSubreddits();
        }
    }

    private void loadSubreddits() {
        if (AccountManager.getAccount() != null) {
            final ArrayList<RedditSubreddit> redditSubredditList = new ArrayList<>();
            AccountDataSource dataSource = new AccountDataSource(this);
            dataSource.open();
            redditSubredditList.addAll(dataSource.getCurrentAccountSubreddits());
            dataSource.close();
            HashMap<String, RedditSubreddit> subscriptions = AccountManager.getAccount().getSubscriptions();
            for (RedditSubreddit s : redditSubredditList) {
                subscriptions.put(s.getDisplayName().toLowerCase(), s);
            }
            Collections.sort(redditSubredditList);

            RedditApi.getSubscribedSubreddits(new FutureCallback<ArrayList<RedditSubreddit>>() {
                @Override
                public void onCompleted(Exception e, final ArrayList<RedditSubreddit> result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    AccountDataSource dataSource = new AccountDataSource(SelectSubredditActivity.this);
                    dataSource.open();
                    ArrayList<RedditSubreddit> allSubs = dataSource.getAllSubreddits();
                    ArrayList<RedditSubreddit> savedSubscriptions = dataSource.getCurrentAccountSubreddits();

                    for (RedditSubreddit s : result) {
                        int index = allSubs.indexOf(s); // Get the subreddit WITH the table id
                        if (index < 0) { // if it doesn't exist, create one with a table id
                            dataSource.addSubreddit(s);
                            dataSource.addSubscriptionToCurrentAccount(s);
                        } else if (!savedSubscriptions.contains(s)) {
                            dataSource.addSubscriptionToCurrentAccount(allSubs.get(index));
                        }
                    }

                    dataSource.close();

                    final boolean isNew = !result.equals(savedSubscriptions);

                    if (isNew) {
                        redditSubredditList.clear();
                        redditSubredditList.addAll(result);
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
                            if (f instanceof SubredditListFragment) {
                                mRedditSubreddits.addAll(result);
                                ((SubredditListFragment) f).setSubreddits(mRedditSubreddits);
                            }
                        }
                    });
                }
            });
        } else {
            RedditApi.getDefaultSubreddits(new FutureCallback<ArrayList<RedditSubreddit>>() {
                @Override
                public void onCompleted(Exception e, final ArrayList<RedditSubreddit> result) {
                    if (e != null) {
                        return;
                    }
                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
                            if (f instanceof SubredditListFragment) {
                                mRedditSubreddits.addAll(result);
                                ((SubredditListFragment) f).setSubreddits(mRedditSubreddits);
                            }
                        }
                    });
                }
            });
        }
    }
}
