package me.williamhester.ui.fragments;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.List;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.LogInActivity;

/**
 * Created by william on 10/28/14.
 */
public class LogInFragment extends BaseFragment {

    private EditText mUsername;
    private EditText mPassword;
    private ProgressDialog mProgressDialog;

    public static LogInFragment newInstance() {
        return new LogInFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup root,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, root, false);
        mUsername = (EditText) v.findViewById(R.id.username);
        mPassword = (EditText) v.findViewById(R.id.password);
        View forgot = v.findViewById(R.id.forgot_password);
        Button confirm = (Button) v.findViewById(R.id.login_confirm);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LogInActivity) getActivity()).openForgotPassword(mUsername.getText().toString());
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                List<Account> accounts = AccountManager.getAccounts();
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                if (TextUtils.isEmpty(username)) {
                    Toast.makeText(getActivity(), R.string.please_enter_a_username,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(password)) {
                    Toast.makeText(getActivity(), R.string.please_enter_a_password,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                for (Account account : accounts) {
                    if (account.getUsername().equalsIgnoreCase(username)) {
                        AccountManager.setAccount(account);
                        return;
                    }
                }
                mProgressDialog = new ProgressDialog(getActivity());
                mProgressDialog.setMessage(getResources().getString(R.string.logging_in));
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                logIn();
            }
        });
        return v;
    }

    private void logIn() {
        RedditApi.logIn(getActivity(), mUsername.getText().toString(),
                mPassword.getText().toString(),
                new FutureCallback<JsonObject>() {
                    @Override
                    public void onCompleted(Exception e, JsonObject result) {
                        mProgressDialog.dismiss();
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), R.string.network_problems,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        RedditApi.printOutLongString(result.toString());

                        JsonObject json = result.get("json").getAsJsonObject();
                        JsonArray errors = json.get("errors").getAsJsonArray();

                        if (errors.size() > 0) {
                            StringBuilder sb = new StringBuilder();
                            for (JsonElement element : errors) {
                                JsonArray array = element.getAsJsonArray();
                                String errorName = array.get(0).getAsString();
                                switch (errorName) {
                                    case "WRONG_PASSWORD":
                                        sb.append(getResources().getString(
                                                R.string.invalid_entries));
                                        break;
                                }
                            }
                            Toast.makeText(getActivity(), sb, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        JsonObject data = json.get("data").getAsJsonObject();
                        String username = mUsername.getText().toString();
                        String cookie = data.get("cookie").getAsString();
                        String modhash = data.get("modhash").getAsString();
                        Account account = new Account(username, modhash, cookie);
                        AccountDataSource dataSource = new AccountDataSource(getActivity());
                        dataSource.open();
                        dataSource.addAccount(account);
                        dataSource.close();
                        AccountManager.setAccount(account);
                        ((LogInActivity) getActivity()).onLoggedIn();
                    }
                });
    }

}
