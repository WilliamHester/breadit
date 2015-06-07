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
import me.williamhester.models.AccountManager;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.fragments.SubredditListFragment;

/**
 * Created by william on 5/27/15.
 */
public class SelectSubredditActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_container);

        Fragment frag = getSupportFragmentManager().findFragmentById(R.id.main_container);
        if (frag == null) {
            frag = SubredditListFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, frag, "ListFragment")
                    .commit();
        }

        loadSubreddits();
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

            RedditApi.getSubscribedSubreddits(new FutureCallback<ArrayList<Subreddit>>() {
                @Override
                public void onCompleted(Exception e, ArrayList<Subreddit> result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    AccountDataSource dataSource = new AccountDataSource(SelectSubredditActivity.this);
                    dataSource.open();
                    ArrayList<Subreddit> allSubs = dataSource.getAllSubreddits();
                    ArrayList<Subreddit> savedSubscriptions = dataSource.getCurrentAccountSubreddits();

                    for (Subreddit s : result) {
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
                        subredditList.clear();
                        subredditList.addAll(result);
                    }

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if (isNew) {
                                Collections.sort(subredditList);
                            }
                        }
                    });
                }
            });
        }
    }
}
