package me.williamhester.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by William on 10/21/14.
 */
public class CaptchaDialogFragment extends DialogFragment {

    private String mCaptchaIden;
    private boolean mKillOnStart;

    public static CaptchaDialogFragment newInstance(String to, String subject, String body) {
        Bundle args = new Bundle();
        args.putString("to", to);
        args.putString("subject", subject);
        args.putString("body", body);
        CaptchaDialogFragment fragment = new CaptchaDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mKillOnStart = savedInstanceState.getBoolean("killOnStart");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.dialog_captcha, null);

        loadCaptcha(view);

        View cancel = view.findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        View reload = view.findViewById(R.id.reload_captcha);
        reload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadCaptcha(view);
            }
        });
        View continueButton = view.findViewById(R.id.continue_button);
        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.sending_message));
                dialog.show();
                String to = getArguments().getString("to");
                String subject = getArguments().getString("subject");
                String body = getArguments().getString("body");
                EditText attempt = (EditText) view.findViewById(R.id.captcha_response);
                RedditApi.compose(getActivity(), mCaptchaIden, attempt.getText().toString(), to,
                        subject, body, new FutureCallback<JsonObject>() {
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
                                    if (isResumed()) {
                                        getTargetFragment().onActivityResult(
                                                ComposeMessageFragment.COMPLETE_CAPTCHA,
                                                Activity.RESULT_OK, null);
                                        getDialog().dismiss();
                                    } else {
                                        mKillOnStart = true;
                                    }
                                } else {
                                    mCaptchaIden = json.get("captcha").getAsString();
                                    loadCaptcha(view);
                                    Toast.makeText(getActivity(), R.string.failed_captcha,
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
        getDialog().setTitle(R.string.captcha_response);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mKillOnStart) {
            getTargetFragment().onActivityResult(ComposeMessageFragment.COMPLETE_CAPTCHA,
                    Activity.RESULT_OK, null);
            dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("killOnStart", mKillOnStart);
    }

    private void loadCaptcha(final View v) {
        RedditApi.getCaptcha(getActivity(), new FutureCallback<JsonObject>() {
            @Override
            public void onCompleted(Exception e, JsonObject result) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                mCaptchaIden = result.get("json").getAsJsonObject()
                        .get("data").getAsJsonObject()
                        .get("iden").getAsString();

                ImageView captcha = (ImageView) v.findViewById(R.id.captcha);
                ImgurApi.loadImage(RedditApi.REDDIT_URL + "/captcha/" + mCaptchaIden, captcha, null);
            }
        });
    }
}
