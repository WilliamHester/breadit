package me.williamhester.ui.fragments;

import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by Tyler on 4/22/2014.
 */
public class LogInDialogFragment extends DialogFragment {

    private EditText mUsername;
    private EditText mPassword;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_login, null);
        mUsername = (EditText) v.findViewById(R.id.username);
        mPassword = (EditText) v.findViewById(R.id.password);
        Button cancel = (Button) v.findViewById(R.id.login_cancel);
        Button confirm = (Button) v.findViewById(R.id.login_confirm);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        confirm.setOnClickListener(new View.OnClickListener() {
           @Override
            public void onClick(View view) {
               final ProgressDialog dialog = new ProgressDialog(getActivity());
               dialog.setTitle(R.string.hang_on);
               dialog.setMessage(getResources().getString(R.string.signing_in));
               dialog.setCancelable(false);
               dialog.show();
               final String username = mUsername.getText().toString().trim();
               FutureCallback<JsonObject> loginCallback = new FutureCallback<JsonObject>() {
                   @Override
                   public void onCompleted(Exception e, JsonObject result) {
                       if (e != null) {
                           Toast.makeText(getActivity(), R.string.invalid_entries, Toast.LENGTH_LONG).show();
                           e.printStackTrace();
                           dialog.dismiss();
                           return;
                       }
                       try {
                           JsonObject data = result.get("json").getAsJsonObject().get("data").getAsJsonObject();
                           String modhash = data.get("modhash").getAsString();
                           String cookie = data.get("cookie").getAsString();
                           Account account = new Account(username, modhash, cookie);
                           AccountDataSource dataSource = new AccountDataSource(getActivity());
                           dataSource.open();
                           dataSource.addAccount(account);
                           dataSource.close();
                           AccountManager.setAccount(account);
                       } catch (NullPointerException ex) {
                           Toast.makeText(getActivity(), R.string.invalid_entries, Toast.LENGTH_LONG).show();
                       }
                   }
               };
               RedditApi.logIn(getActivity(), username, mPassword.getText().toString().trim(), loginCallback);
           }
        });
        getDialog().setTitle(R.string.login);
        return v;
    }
}
