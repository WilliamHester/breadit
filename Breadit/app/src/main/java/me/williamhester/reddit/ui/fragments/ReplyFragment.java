package me.williamhester.reddit.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.reddit.models.reddit.Thing;
import me.williamhester.reddit.models.reddit.Votable;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/19/14.
 */
public class ReplyFragment extends AsyncSendFragment {

  private boolean mIsEditing;

  public static ReplyFragment newInstance(Thing thing) {
    Bundle b = new Bundle();
    b.putParcelable("thing", thing);
    ReplyFragment fragment = new ReplyFragment();
    fragment.setArguments(b);
    return fragment;
  }

  public static ReplyFragment newInstance(Thing parent, Votable redditVotable) {
    Bundle b = new Bundle();
    b.putParcelable("thing", parent);
    b.putParcelable("redditVotable", redditVotable);
    ReplyFragment fragment = new ReplyFragment();
    fragment.setArguments(b);
    return fragment;
  }

  @Override
  protected String getBodyHint() {
    Thing parent = getArguments().getParcelable("thing");
    if (parent != null) {
      return getResources().getString(R.string.reply_hint) + " /u/" + parent.getAuthor();
    } else {
      return getResources().getString(R.string.enter_reply_text);
    }
  }

  @Override
  protected String getButtonText() {
    return getResources().getString(R.string.send_reply);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mIsEditing = getArguments().containsKey("votable");
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_reply, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });
    mToolbar.setTitle(R.string.reply);
    onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());

    if (mIsEditing && savedInstanceState == null) {
      Votable redditVotable = getArguments().getParcelable("redditVotable");
      mMarkdownBody.setBody(redditVotable.getBodyMarkdown());
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    mMarkdownBody.showKeyboard();
  }

  @Override
  protected void onSaveClick() {
    final ProgressDialog dialog = new ProgressDialog(getActivity());
    dialog.setMessage(getResources().getString(R.string.sending_reply));
    dialog.setCancelable(false);
    dialog.show();
    final Thing parent = getArguments().getParcelable("thing");
    final Votable redditVotable = getArguments().getParcelable("redditVotable");
    FutureCallback<ArrayList<Thing>> callback = new FutureCallback<ArrayList<Thing>>() {
      @Override
      public void onCompleted(final Exception e,
                              final ArrayList<Thing> result) {
        if (getView() != null)
          getView().post(new Runnable() {
            @Override
            public void run() {
              dialog.dismiss();
              if (e != null) {
                if (e instanceof RedditApi.ArchivedSubmissionException) {
                  Toast.makeText(getActivity(), R.string.archived,
                      Toast.LENGTH_SHORT).show();
                  kill();
                } else {
                  e.printStackTrace();
                  Toast.makeText(getActivity(),
                      R.string.failed_to_post_reply,
                      Toast.LENGTH_SHORT).show();
                }
                return;
              }
              Intent i = new Intent();
              Bundle resultBundle = new Bundle();
              resultBundle.putParcelable("oldThing", redditVotable);
              resultBundle.putParcelable("newComment", result.get(0));
              resultBundle.putParcelable("parentThing", parent);
              i.putExtras(resultBundle);
              InputMethodManager imm = (InputMethodManager) getActivity()
                  .getSystemService(Context.INPUT_METHOD_SERVICE);
              imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
              if (getTargetFragment() != null) {
                getTargetFragment().onActivityResult(getTargetRequestCode(),
                    Activity.RESULT_OK, i);
              }
              if (isResumed()) {
                getActivity().onBackPressed();
              } else {
                mKillOnStart = true;
              }
            }
          });
      }
    };
    if (mIsEditing) {
      redditVotable.setMarkdownBody(mMarkdownBody.getBody());
      RedditApi.editThing(redditVotable, callback);
    } else {
      RedditApi.replyToComment(getActivity(), parent, mMarkdownBody.getBody(),
          callback);
    }
  }
}
