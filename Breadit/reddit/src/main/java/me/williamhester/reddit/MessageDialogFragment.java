package me.williamhester.reddit;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/14/14.
 */
public class MessageDialogFragment extends DialogFragment {

    private EditText mTo;
    private EditText mSubject;
    private EditText mBody;

    private String mName;
    private Account mAccount;
    private boolean mReply;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mName = getArguments().getString("name");
            mAccount = getArguments().getParcelable("account");
            mReply = getArguments().getBoolean("reply");
        }
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_message, null);
        mTo = (EditText) v.findViewById(R.id.username);
        mSubject = (EditText) v.findViewById(R.id.subject);
        mBody = (EditText) v.findViewById(R.id.body);
        Button cancel = (Button) v.findViewById(R.id.cancel);
        Button send = (Button) v.findViewById(R.id.send);

        if (mName != null) {
            mTo.setVisibility(View.GONE);
        }

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mBody.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.please_enter_body, Toast.LENGTH_SHORT)
                            .show();
                } else if (mName == null && mTo.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.please_enter_username, Toast.LENGTH_SHORT)
                            .show();
                } else if (mSubject.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.please_enter_subject, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    if (mReply) {
                        new ReplyAsyncTask().execute();
                    } else {
                        new SendMessageAsyncTask().execute();
                    }
                }
            }
        });
        getDialog().setTitle(R.string.compose_message);
        return v;
    }

    public static MessageDialogFragment newInstance(Account account, String name, boolean reply) {
        Bundle b = new Bundle();
        b.putParcelable("account", account);
        b.putString("name", name);
        b.putBoolean("reply", reply);
        MessageDialogFragment m = new MessageDialogFragment();
        m.setArguments(b);
        return m;
    }

    public static MessageDialogFragment newInstance(Account account, String name) {
        return newInstance(account, name, false);
    }

    private class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("api_type", "json"));
            apiParams.add(new BasicNameValuePair("subject", mSubject.getText().toString()));
            apiParams.add(new BasicNameValuePair("text", mBody.getText().toString()));
            if (mName != null)
                apiParams.add(new BasicNameValuePair("to", mName));
            else
                apiParams.add(new BasicNameValuePair("to", mTo.getText().toString()));
            Log.i("MessageDialogFragment", Utilities.post(apiParams,
                    "http://www.reddit.com/api/compose", mAccount));
            Log.i("MessageDialogFragment", mAccount.getUsername());
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dismiss();
        }
    }

    private class ReplyAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (mAccount != null) {
                List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                apiParams.add(new BasicNameValuePair("api-type", "json"));
                apiParams.add(new BasicNameValuePair("text", mBody.getText().toString()));
                apiParams.add(new BasicNameValuePair("thing_id", mName));
                Log.i("SubmitDialogFragment", "Response = " + Utilities.post(apiParams,
                        "http://www.reddit.com/api/comment", mAccount));
                Log.i("SubmitDialogFragment", "name = " + mName);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {

        }
    }

}
