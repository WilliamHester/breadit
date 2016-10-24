package me.williamhester.reddit.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import me.williamhester.reddit.models.reddit.Submission;
import me.williamhester.reddit.R;
import me.williamhester.reddit.tools.Url;
import me.williamhester.reddit.ui.fragments.CommentFragment;
import me.williamhester.reddit.ui.fragments.ComposeMessageFragment;
import me.williamhester.reddit.ui.fragments.MessagesFragment;
import me.williamhester.reddit.ui.fragments.SubredditFragment;
import me.williamhester.reddit.ui.fragments.UserFragment;
import me.williamhester.reddit.ui.fragments.WebViewFragment;

/**
 * This Activity handles the external link requests then proceeds to open the proper Activity with
 * the proper content displaying.
 */
public class BrowseActivity extends BaseActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_container);

    Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
    // If the fragment is already populating the view, we're done.
    if (f != null) {
      return;
    }

    if (getIntent().getData() != null) {
      f = getMainFragmentFromIntentData();
    } else {
      f = getMainFragmentFromIntentExtras();
    }

    getSupportFragmentManager().beginTransaction()
        .add(R.id.main_container, f, "content")
        .commit();
  }

  private Fragment getMainFragmentFromIntentData() {
    Url url = new Url(getIntent().getDataString());
    switch (url.getType()) {
      case Url.USER:
        return UserFragment.newInstance(url.getLinkId());
      case Url.SUBREDDIT:
        return SubredditFragment.newInstance(url.getLinkId());
      case Url.SUBMISSION:
        return CommentFragment.newInstance(url.getUrl(), false);
      case Url.MESSAGES:
        return MessagesFragment.newInstance(url.getLinkId());
      case Url.COMPOSE:
        return ComposeMessageFragment.newInstance();
      default:
        return WebViewFragment.newInstance(url.getUrl());
    }
  }

  private Fragment getMainFragmentFromIntentExtras() {
    Bundle extras = getIntent().getExtras();
    String type = extras.getString("type");
    switch (type) {
      case "user":
        return UserFragment.newInstance(extras.getString("username"));
      case "subreddit":
        return SubredditFragment.newInstance(extras.getString("subreddit"));
      case "messages":
        return MessagesFragment.newInstance();
      case "comments":
        if (extras.containsKey("permalink")) {
          boolean isSingleThread = getIntent().getExtras() != null &&
              getIntent().getExtras().getBoolean("isSingleThread");
          return CommentFragment.newInstance(extras.getString("permalink"), isSingleThread);
        } else {
          return CommentFragment.newInstance((Submission) extras.getParcelable("submission"));
        }
    }
    return null;
  }
}
