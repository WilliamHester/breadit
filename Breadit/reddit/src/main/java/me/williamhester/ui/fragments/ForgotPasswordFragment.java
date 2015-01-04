package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/28/14.
 */
public class ForgotPasswordFragment extends Fragment {

    private boolean mKillOnStart;

    public static ForgotPasswordFragment newInstance(String username) {
        ForgotPasswordFragment fragment = new ForgotPasswordFragment();
        Bundle args = new Bundle();
        args.putString("username", username);
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
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_forgot_password, container, false);

        final EditText username = (EditText) v.findViewById(R.id.username);
        username.setText(getArguments().getString("username"));
        View emailMe = v.findViewById(R.id.email_me);
        emailMe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = username.getText().toString();
                if (TextUtils.isEmpty(name)) {
                    return;
                }
                Ion.with(getActivity())
                        .load(RedditApi.REDDIT_URL + "/api/password")
                        .setBodyParameter("api_type", "json")
                        .setBodyParameter("name", name)
                        .asJsonObject()
                        .setCallback(mForgotPasswordCallback);
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mKillOnStart) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("killOnStart", mKillOnStart);
    }

    private FutureCallback<JsonObject> mForgotPasswordCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            Log.d("ForgotPasswordFragment", result.toString());
            JsonObject json = result.get("json").getAsJsonObject();
            JsonArray errors = json.get("errors").getAsJsonArray();
            if (errors.size() == 0) {
                if (isResumed()) {
                    Toast.makeText(getActivity(), R.string.email_sent, Toast.LENGTH_SHORT).show();
                    getActivity().onBackPressed();
                } else {
                    mKillOnStart = true;
                }
                return;
            }
            StringBuilder sb = new StringBuilder();
            for (JsonElement element : errors) {
                JsonArray array = (JsonArray) element;
                String value = array.get(0).getAsString();
                switch (value) {
                    case "RATELIMIT":
                        long minutes = (Math.round(json.get("ratelimit").getAsDouble())) / 60;
                        sb.append(getResources().getString(R.string.ratelimited))
                                .append(' ')
                                .append(minutes)
                                .append(' ')
                                .append(getResources().getString(R.string.minutes))
                                .append('\n');
                        break;
                    case "NO_EMAIL_FOR_USER":
                        sb.append(getResources().getString(R.string.no_email_for_user))
                                .append('\n');
                        break;
                    case "USER_DOESNT_EXIST":
                        sb.append(getResources().getString(R.string.user_doesnt_exist))
                                .append('\n');
                        break;
                }
            }
            if (getView() != null) {
                TextView error = (TextView) getView().findViewById(R.id.errors);
                error.setVisibility(View.VISIBLE);
                error.setText(sb.toString().trim());
            }
        }
    };
}
