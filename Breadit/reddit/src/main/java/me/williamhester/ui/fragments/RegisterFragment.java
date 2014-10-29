package me.williamhester.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Captcha;
import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.LogInActivity;

/**
 * Created by william on 10/28/14.
 */
public class RegisterFragment extends Fragment {

    private EditText mUsername;
    private EditText mPassword;
    private EditText mEmail;
    private EditText mCaptchaText;
    private Handler mHandler = new Handler();
    private ImageView mAvailabilityIcon;
    private ProgressBar mAvailabilityProgress;
    private ProgressDialog mProgressDialog;
    private TextView mUserAvailability;

    private String mCaptchaIden;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_register, container, false);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.attempting_to_create));
        mProgressDialog.setCancelable(false);

        mUsername = (EditText) v.findViewById(R.id.username);
        mPassword = (EditText) v.findViewById(R.id.password);
        mEmail = (EditText) v.findViewById(R.id.email);
        mCaptchaText = (EditText) v.findViewById(R.id.captcha_response);
        mUserAvailability = (TextView) v.findViewById(R.id.username_availability);
        mAvailabilityIcon = (ImageView) v.findViewById(R.id.availability_icon);
        mAvailabilityProgress = (ProgressBar) v.findViewById(R.id.progress_bar);
        mAvailabilityProgress.setVisibility(View.GONE);

        loadCaptcha(v);
        View imageButton = v.findViewById(R.id.reload_captcha);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCaptcha(v);
            }
        });
        View register = v.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String captcha = mCaptchaText.getText().toString();

                boolean valid = true;
                if (TextUtils.isEmpty(username) && username.length() > 3) {
                    // username invalid
                    valid = false;
                }
                if (password.length() > 2) {
                    // password invalid
                    valid = false;
                }
                if (TextUtils.isEmpty(captcha)) {
                    // captcha response invalid
                    valid = false;
                }
                if (valid) {
                    mProgressDialog.show();
                    Ion.with(getActivity())
                            .load(RedditApi.REDDIT_URL + "/api/register")
                            .setBodyParameter("api_type", "json")
                            .setBodyParameter("captcha", captcha)
                            .setBodyParameter("iden", mCaptchaIden)
                            .setBodyParameter("passwd", password)
                            .setBodyParameter("passwd2", password)
                            .setBodyParameter("rem", "true")
                            .setBodyParameter("user", username)
                            .setBodyParameter("email", email)
                            .asJsonObject()
                            .setCallback(mRegisterCallback);
                }
            }
        });

        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                mUserAvailability.setVisibility(View.INVISIBLE);
                mHandler.removeCallbacks(mCheckUsernameRunnable);
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s) && s.length() > 3) {
                    mHandler.postDelayed(mCheckUsernameRunnable, 1500);
                }
            }
        });

        return v;
    }

    @Override
    public void onPause() {
        super.onPause();

        mHandler.removeCallbacks(mCheckUsernameRunnable);
    }

    private void showUsernameAvailable() {
        mUserAvailability.setText(R.string.username_available);
        mUserAvailability.setVisibility(View.VISIBLE);
        mAvailabilityProgress.setVisibility(View.GONE);
    }

    private void showUsernameNotAvailable() {
        mUserAvailability.setText(R.string.username_not_available);
        mUserAvailability.setVisibility(View.VISIBLE);
        mAvailabilityProgress.setVisibility(View.GONE);
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
                mCaptchaText.setText(""); // Clear the captcha field, because we just got a new one
                ImageView captcha = (ImageView) v.findViewById(R.id.captcha);
                ImgurApi.loadImage(RedditApi.REDDIT_URL + "/captcha/" + mCaptchaIden, captcha, null);
            }
        });
    }

    private FutureCallback<JsonObject> mRegisterCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            if (e != null) {
                mProgressDialog.dismiss();
                Toast.makeText(getActivity(), R.string.failed_to_load, Toast.LENGTH_SHORT).show();
                e.printStackTrace();
                return;
            }
            JsonObject data = result.get("json").getAsJsonObject().get("data").getAsJsonObject();
            Account a = new Account(mUsername.getText().toString(),
                    data.get("modhash").getAsString(),
                    data.get("cookie").getAsString());
            AccountDataSource dataSource = new AccountDataSource(getActivity());
            dataSource.open();
            dataSource.addAccount(a);
            dataSource.close();
            AccountManager.setAccount(a);
            mProgressDialog.dismiss();
            ((LogInActivity) getActivity()).onLoggedIn();
        }
    };

    private Runnable mCheckUsernameRunnable = new Runnable() {
        @Override
        public void run() {
            mUserAvailability.setText(R.string.checking_availability);
            mUserAvailability.setVisibility(View.VISIBLE);
            mAvailabilityProgress.setVisibility(View.VISIBLE);
            Ion.with(getActivity())
                    .load(RedditApi.REDDIT_URL + "/user/"
                            + mUsername.getText().toString()
                            + "/about.json")
                    .asJsonObject()
                    .setCallback(new FutureCallback<JsonObject>() {
                        @Override
                        public void onCompleted(Exception e, JsonObject result) {
                            if (e != null) {
                                e.printStackTrace();
                                return;
                            }
                            if (result.get("error") != null) {
                                showUsernameAvailable();
                            } else {
                                showUsernameNotAvailable();
                            }
                        }
                    });
        }
    };
}
