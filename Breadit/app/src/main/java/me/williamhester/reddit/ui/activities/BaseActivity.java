package me.williamhester.reddit.ui.activities;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;

import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.fragments.BackableFragment;

/**
 * Created by william on 1/4/15.
 */
public abstract class BaseActivity extends ActionBarActivity {

  @Override
  public void onBackPressed() {
    Fragment f = getSupportFragmentManager().findFragmentById(R.id.main_container);
    if (f instanceof BackableFragment && ((BackableFragment) f).canGoBack()) {
      ((BackableFragment) f).goBack();
      return;
    }
    super.onBackPressed();
  }
}
