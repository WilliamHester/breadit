package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/21/14.
 */
public class ComposeMessageFragment extends AsyncSendFragment {

    public static final int COMPLETE_CAPTCHA = 1;

    private boolean mCaptchaOnStart;

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
    protected int getContainerId() {
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
        if (requestCode == COMPLETE_CAPTCHA) {
            if (resultCode == Activity.RESULT_OK) {
                if (isResumed()) {
                    Toast.makeText(getActivity(), R.string.message_sent, Toast.LENGTH_SHORT).show();
                    getFragmentManager().popBackStack();
                } else {
                    mKillOnStart = true;
                }
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
                            mSubject.getText().toString(), mBodyFragment.getMarkdownBody(),
                            sentMessage);
                }
            }
        };
        RedditApi.needsCaptcha(getActivity(), needsCaptchaCallback);
    }

    private void onMessageSent() {
        Toast.makeText(getActivity(), R.string.message_sent, Toast.LENGTH_SHORT).show();
        getFragmentManager().popBackStack();
    }

    private void showCaptchaDialog() {
        CaptchaDialogFragment fragment = CaptchaDialogFragment
                .newInstance(mComposeTo.getText().toString(),
                        mSubject.getText().toString(), mBodyFragment.getMarkdownBody());
        fragment.setTargetFragment(this, COMPLETE_CAPTCHA);
        fragment.show(getFragmentManager(), "captcha");
    }
}
