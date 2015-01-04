package me.williamhester.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/21/14.
 */
public class ComposeMessageFragment extends AsyncSendFragment {

    private boolean mCaptchaOnStart;

    private CaptchaDialogFragment mCaptchaDialog;
    private EditText mComposeTo;
    private EditText mSubject;

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
    protected int getMarkdownBodyId() {
        return R.id.body_container;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCaptchaOnStart = savedInstanceState.getBoolean("captchaOnStart");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message, container, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });
        toolbar.setTitle(R.string.compose_message);
        onCreateOptionsMenu(toolbar.getMenu(), getActivity().getMenuInflater());

        mComposeTo = (EditText) v.findViewById(R.id.compose_to);
        mSubject = (EditText) v.findViewById(R.id.subject);

        if (getArguments() != null && getArguments().containsKey("to")) {
            mComposeTo.setText(getArguments().getString("to"));
            mComposeTo.setVisibility(View.GONE);
        }

        return v;
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("captchaOnStart", mCaptchaOnStart);
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
