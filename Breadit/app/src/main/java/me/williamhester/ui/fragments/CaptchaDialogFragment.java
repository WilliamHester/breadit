package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.knapsack.Knapsack;
import me.williamhester.knapsack.Save;
import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by William on 10/21/14.
 */
public class CaptchaDialogFragment extends DialogFragment {

    public static final int COMPLETE_CAPTCHA = 1;

    private String mCaptchaIden;
    @Save boolean mKillOnStart;

    public static CaptchaDialogFragment newInstance() {
        Bundle args = new Bundle();
        CaptchaDialogFragment fragment = new CaptchaDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Knapsack.restore(this, savedInstanceState);
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
                EditText attempt = (EditText) view.findViewById(R.id.captcha_response);
                if (getTargetFragment() != null) {
                    Intent i = new Intent();
                    i.putExtra("iden", mCaptchaIden);
                    i.putExtra("attempt", attempt.getText().toString());
                    getTargetFragment().onActivityResult(COMPLETE_CAPTCHA, Activity.RESULT_OK, i);
                } else if (getActivity() instanceof OnCaptchaAttemptListener) {
                    ((OnCaptchaAttemptListener) getActivity()).onCaptchaAttempt(mCaptchaIden,
                            attempt.getText().toString());
                }
            }
        });
        getDialog().setTitle(R.string.captcha_response);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mKillOnStart) {
            dismiss();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Knapsack.save(this, outState);
    }

    public void setKillOnStart() {
        mKillOnStart = true;
    }

    public void newCaptcha(String captchaIden) {
        mCaptchaIden = captchaIden;
        loadCaptcha(getView());
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

    public interface OnCaptchaAttemptListener {
        public void onCaptchaAttempt(String iden, String attempt);
    }
}
