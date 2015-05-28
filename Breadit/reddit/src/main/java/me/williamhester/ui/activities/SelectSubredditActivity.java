package me.williamhester.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

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
            frag = SubredditListFragment.newInstance(
                    getIntent().getExtras().getString(SubredditListFragment.SELECTED_SUBREDDIT));
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.main_container, frag, "ListFragment")
                    .commit();
        }
    }
}
