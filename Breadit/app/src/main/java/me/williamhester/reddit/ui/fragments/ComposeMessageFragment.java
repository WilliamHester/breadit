package me.williamhester.reddit.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.koushikdutta.async.future.FutureCallback;

import butterknife.Bind;
import me.williamhester.knapsack.Save;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/21/14.
 */
public class ComposeMessageFragment extends AsyncSendFragment {

  @Save boolean mCaptchaOnStart;

  @Bind(R.id.compose_to) EditText mComposeTo;
  @Bind(R.id.subject) EditText mSubject;

  private CaptchaDialogFragment mCaptchaDialog;

  public static ComposeMessageFragment newInstance() {
    return new ComposeMessageFragment();
  }

  public static ComposeMessageFragment newInstance(String username) {
    Bundle args = new Bundle();
    args.putString("to", username);
    ComposeMessageFragment fragment = new ComposeMessageFragment();
    fragment.setArguments(args);
    return fragment;
  }

  @Override
  protected String getBodyHint() {
    return getResources().getString(R.string.compose_message);
  }

  @Override
  protected String getButtonText() {
    return getResources().getString(R.string.send);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_message, container, false);
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mToolbar.setNavigationIcon(R.drawable.ic_arrow_back_white);
    mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        getActivity().onBackPressed();
      }
    });
    mToolbar.setTitle(R.string.compose_message);
    onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());

    if (getArguments() != null && getArguments().containsKey("to")) {
      mComposeTo.setText(getArguments().getString("to"));
      mComposeTo.setVisibility(View.GONE);
    }
  }

  @Override
  public void onResume() {
    super.onResume();

    if (mCaptchaOnStart) {
      showCaptchaDialog();
    } else if (mKillOnStart) {
      onMessageSent();
    }
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == CaptchaDialogFragment.COMPLETE_CAPTCHA) {
      if (resultCode == Activity.RESULT_OK) {
        String iden = data.getStringExtra("iden");
        String attempt = data.getStringExtra("attempt");
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getResources().getString(R.string.sending_message));
        dialog.show();
        RedditApi.compose(getActivity(), iden, attempt, mComposeTo.getText().toString(),
            mSubject.getText().toString(), mMarkdownBody.getBody(),
            new FutureCallback<JsonObject>() {
              @Override
              public void onCompleted(Exception e, JsonObject result) {
                dialog.dismiss();
                if (e != null) {
                  e.printStackTrace();
                  return;
                }
                JsonObject json = result.get("json").getAsJsonObject();
                JsonArray errors = json.get("errors").getAsJsonArray();
                if (errors.size() == 0) {
                  if (mCaptchaDialog.isResumed()) {
                    mCaptchaDialog.dismiss();
                  } else {
                    mCaptchaDialog.setKillOnStart();
                  }
                  Toast.makeText(getActivity(), R.string.message_sent, Toast.LENGTH_SHORT).show();
                  kill();
                } else {
                  mCaptchaDialog.newCaptcha(json.get("captcha").getAsString());
                  Toast.makeText(getActivity(), R.string.failed_captcha,
                      Toast.LENGTH_SHORT).show();
                }
              }
            });
      }
      return;
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  protected void onSaveClick() {
    final FutureCallback<JsonObject> sentMessage = new FutureCallback<JsonObject>() {
      @Override
      public void onCompleted(Exception e, JsonObject result) {
        if (e != null) {
          e.printStackTrace();
          return;
        }
        if (isResumed()) {
          onMessageSent();
        } else {
          mKillOnStart = true;
        }
      }
    };
    FutureCallback<String> needsCaptchaCallback = new FutureCallback<String>() {
      @Override
      public void onCompleted(Exception e, String result) {
        if (e != null) {
          e.printStackTrace();
          return;
        }
        boolean needsCaptcha = Boolean.parseBoolean(result);
        if (needsCaptcha) {
          if (isResumed()) {
            showCaptchaDialog();
          } else {
            mCaptchaOnStart = true;
          }
        } else {
          RedditApi.compose(getActivity(), mComposeTo.getText().toString(),
              mSubject.getText().toString(), mMarkdownBody.getBody(),
              sentMessage);
        }
      }
    };
    RedditApi.needsCaptcha(getActivity(), needsCaptchaCallback);
  }

  private void onMessageSent() {
    Toast.makeText(getActivity(), R.string.message_sent, Toast.LENGTH_SHORT).show();
    getActivity().onBackPressed();
  }

  private void showCaptchaDialog() {
    mCaptchaDialog = CaptchaDialogFragment.newInstance();
    mCaptchaDialog.setTargetFragment(this, CaptchaDialogFragment.COMPLETE_CAPTCHA);
    mCaptchaDialog.show(getFragmentManager(), "captcha");
  }
}
