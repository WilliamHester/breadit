package me.williamhester.reddit.ui.activities;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import me.williamhester.reddit.models.reddit.Submission;
import me.williamhester.reddit.R;
import me.williamhester.reddit.tools.Url;
import me.williamhester.reddit.ui.fragments.ImagePagerFragment;
import me.williamhester.reddit.ui.fragments.WebViewFragment;
import me.williamhester.reddit.ui.fragments.YouTubeFragment;

/**
 * Created by william on 3/25/15.
 */
public class OverlayContentActivity extends FragmentActivity {

  public static final int TYPE_SUBMISSION = 0;
  public static final int TYPE_LINK = 1;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.activity_overlay_content);

    Bundle extras = getIntent().getExtras();
    Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
    if (f == null) {
      switch (extras.getInt("type")) {
        case TYPE_SUBMISSION:
          f = getContentFragment((Submission) extras.getParcelable("submission"));
          break;
        case TYPE_LINK:
          f = getContentFragment((Url) extras.getParcelable("url"));
          break;
      }
      getSupportFragmentManager().beginTransaction()
          .replace(R.id.main_container, f, "Content")
          .commit();
    }
  }

  /**
   * Gets the proper fragment to display the content that the submission is showing.
   *
   * @return returns the fragment needed to display the content
   */
  private Fragment getContentFragment(Submission submission) {
    return getContentFragment(submission.getLinkDetails());
  }

  private Fragment getContentFragment(Url url) {
    Fragment f = null;
    switch (url.getType()) {
      case Url.IMGUR_GALLERY: // For now, we're going to go to a WebView because weird things happen with galleries
      case Url.NOT_SPECIAL: // Go to a webview
        f = WebViewFragment.newInstance(url.getUrl());
        break;
      case Url.IMGUR_ALBUM:
        f = ImagePagerFragment.newInstanceLazyLoaded(url.getLinkId(), true);
        break;
      case Url.IMGUR_IMAGE:
        f = ImagePagerFragment.newInstanceLazyLoaded(url.getLinkId(), false);
        break;
      case Url.YOUTUBE:
        f = YouTubeFragment.newInstance(url.getLinkId());
        break;
      case Url.DIRECT_GFY:
      case Url.GFYCAT_LINK:
      case Url.GIF:
      case Url.NORMAL_IMAGE:
        f = ImagePagerFragment.newInstance(url);
        break;
    }
    return f;
  }

  @Override
  public void finish() {
    super.finish();
    overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
  }
}
