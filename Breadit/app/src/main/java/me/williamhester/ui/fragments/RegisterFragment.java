package me.williamhester.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import butterknife.Bind;
import me.williamhester.databases.AccountDataSource;
import me.williamhester.knapsack.Save;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.LogInActivity;

/**
 * Created by william on 10/28/14.
 */
public class RegisterFragment extends BaseFragment {

    @Bind(R.id.username) EditText mUsername;
    @Bind(R.id.password) EditText mPassword;
    @Bind(R.id.email) EditText mEmail;
    @Bind(R.id.captcha_response) EditText mCaptchaText;
    @Bind(R.id.username_availability) TextView mUserAvailability;
    @Bind(R.id.availability_icon) ImageView mAvailabilityIcon;
    @Bind(R.id.progress_bar) ProgressBar mAvailabilityProgress;

    @Save String mCaptchaIden;

    private Handler mHandler = new Handler();
    private ProgressDialog mProgressDialog;

    public static RegisterFragment newInstance() {
        return new RegisterFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_register, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mProgressDialog = new ProgressDialog(getActivity());
        mProgressDialog.setMessage(getResources().getString(R.string.attempting_to_create));
        mProgressDialog.setCancelable(false);

        mAvailabilityProgress.setVisibility(View.GONE);

        loadCaptcha(view);
        View imageButton = view.findViewById(R.id.reload_captcha);
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadCaptcha(view);
            }
        });
        View register = view.findViewById(R.id.register);
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                String email = mEmail.getText().toString();
                String captcha = mCaptchaText.getText().toString();

                boolean valid = true;
                if (TextUtils.isEmpty(username) || username.length() < 3) {
                    // username invalid
                    valid = false;
                }
                if (password.length() < 6) {
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
