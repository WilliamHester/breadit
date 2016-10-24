package me.williamhester.reddit.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import butterknife.Bind;
import me.williamhester.knapsack.Save;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.views.MarkdownBodyView;

/**
 * This Fragment provides an abstraction between the ReplyFragment and ComposeMessageFragment. It
 * combines most of the logic between the two and keeps a variable, mKillOnStart, that will kill the
 * Fragment when it is started if the asynchronous task completes when the Fragment's state is
 * saved. It manages the saving of the state of the Fragment inside it.
 *
 * Created by William on 10/31/14.
 */
public abstract class AsyncSendFragment extends BaseFragment {

  @Save protected boolean mKillOnStart;
  @Bind(R.id.body_container) protected MarkdownBodyView mMarkdownBody;

  /**
   * This is called to get the body hint for the MarkdownBodyFragment
   *
   * @return the string for the hint for the MarkdownBodyFragment
   */
  protected abstract String getBodyHint();

  /**
   * This is called to get the text for the button that appears in the ActionBar to send the thing.
   *
   * @return the string that represents the text on the button
   */
  protected abstract String getButtonText();

  /**
   * This method is called when the save button in the ActionBar is clicked.
   */
  protected abstract void onSaveClick();

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    super.onCreateOptionsMenu(menu, inflater);
    inflater.inflate(R.menu.fragment_reply, menu);

    MenuItem reply = menu.findItem(R.id.action_reply);
    Button button = (Button) MenuItemCompat.getActionView(reply);
    if (button == null) {
      reply.setActionView(R.layout.button_reply);
      button = (Button) MenuItemCompat.getActionView(reply);
    }
    button.setText(getButtonText());
    button.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onSaveClick();
      }
    });
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mMarkdownBody.setHint(getBodyHint());
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mKillOnStart) {
      getActivity().onBackPressed();
    }
  }

  protected void kill() {
    mKillOnStart = true;
    if (isResumed() && getView() != null) {
      InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
          Context.INPUT_METHOD_SERVICE);
      imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
      getActivity().onBackPressed();
    }
  }
}
