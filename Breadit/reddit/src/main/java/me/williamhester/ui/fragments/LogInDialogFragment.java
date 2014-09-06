package me.williamhester.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;

import me.williamhester.BreaditApplication;
import me.williamhester.models.Account;
import me.williamhester.databases.AccountDataSource;
import me.williamhester.models.AccountManager;
import me.williamhester.reddit.R;

/**
 * Created by Tyler on 4/22/2014.
 */
public class LogInDialogFragment extends DialogFragment {
    private EditText mUsername;
    private EditText mPassword;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    }

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
               new LoginUserTask().execute();
           }
        });
        getDialog().setTitle(R.string.login);
        return v;
    }

    private class LoginUserTask extends AsyncTask<Void, Void, Account> {
        private Dialog mDialog;

        @Override
        public void onPreExecute() {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                    android.R.style.Theme_DeviceDefault_Dialog);
            builder.setCancelable(false);
            builder.setTitle(R.string.hang_on);
            builder.setView(inflater.inflate(R.layout.dialog_sign_in, null));
            mDialog = builder.create();
            mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            mDialog.show();
        }

        @Override
        protected Account doInBackground(Void... nothing) {
            Bundle b;
            try {
                b = new Bundle();

                Account account = Account.newAccount(mUsername.getText().toString(),
                        mPassword.getText().toString(), getActivity());
                b.putParcelable("account", account);
                return account;
            } catch (MalformedURLException e) {
                Log.e("BreaditDebug", e.toString());
                return null;
            } catch (IOException e) {
                Log.e("BreaditDebug", e.toString());
                return null;
            } catch (NullPointerException e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Account a) {
            if (a != null) {
                AccountDataSource dataSource = new AccountDataSource(getActivity());
                dataSource.open();
                dataSource.addAccount(a);
                dataSource.close();
                AccountManager.setAccount(a);
                if (getActivity() != null) {
                    SharedPreferences prefs = getActivity()
                            .getSharedPreferences("preferences", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putLong("accountId", a.getId());
                    editor.commit();
                }
                mDialog.dismiss();
                dismiss();
            } else {
                Toast.makeText(getActivity(), R.string.invalid_entries, Toast.LENGTH_LONG).show();
                mDialog.dismiss();
            }
        }
    }
}
