package me.williamhester.reddit.ui.fragments;

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

import butterknife.Bind;
import me.williamhester.reddit.databases.AccountDataSource;
import me.williamhester.reddit.models.reddit.Account;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.activities.LogInActivity;

/**
 * Created by william on 10/28/14.
 */
public class LogInFragment extends BaseFragment {

  @Bind(R.id.username) EditText mUsername;
  @Bind(R.id.password) EditText mPassword;
  private ProgressDialog mProgressDialog;

  public static LogInFragment newInstance() {
    return new LogInFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup root,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_login, root, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    View forgot = view.findViewById(R.id.forgot_password);
    Button confirm = (Button) view.findViewById(R.id.login_confirm);
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
