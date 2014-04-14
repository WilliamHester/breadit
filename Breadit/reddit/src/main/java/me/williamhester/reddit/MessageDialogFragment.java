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

    private String mUsername;
    private Account mAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUsername = getArguments().getString("username");
            mAccount = getArguments().getParcelable("account");
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

        if (mUsername != null) {
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
                } else if (mUsername == null && mTo.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.please_enter_username, Toast.LENGTH_SHORT)
                            .show();
                } else if (mSubject.getText().toString().length() == 0) {
                    Toast.makeText(getActivity(), R.string.please_enter_subject, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    new SendMessageAsyncTask().execute();
                }
            }
        });
        getDialog().setTitle(R.string.compose_message);
        return v;
    }

    public static MessageDialogFragment newInstance(Account account, String username) {
        Bundle b = new Bundle();
        b.putParcelable("account", account);
        b.putString("username", username);
        MessageDialogFragment m = new MessageDialogFragment();
        m.setArguments(b);
        return m;
    }

    private class SendMessageAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("api_type", "json"));
            apiParams.add(new BasicNameValuePair("subject", mSubject.getText().toString()));
            apiParams.add(new BasicNameValuePair("text", mBody.getText().toString()));
            if (mUsername != null)
                apiParams.add(new BasicNameValuePair("to", mUsername));
            else
                apiParams.add(new BasicNameValuePair("to", mTo.getText().toString()));
            Log.i("MessageDialogFragment", Utilities.post(apiParams,
                    "http://www.reddit.com/api/compose", mAccount));
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            dismiss();
        }
    }

}
