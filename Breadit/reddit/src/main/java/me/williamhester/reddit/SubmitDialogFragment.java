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
import android.widget.ImageView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Captcha;
import me.williamhester.areddit.User;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/6/14.
 */
public class SubmitDialogFragment extends DialogFragment {

    private Button mConfirm;
    private Button mCancel;
    private EditText mCaptchaResponse;
    private EditText mSubmitText;
    private EditText mTitle;
    private ImageView mCaptchaImage;
    private User mUser;
    private String mSubreddit;

    private Captcha mCaptcha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mUser = getArguments().getParcelable("user");
            mSubreddit = getArguments().getString("subreddit");
        }
        setStyle(STYLE_NO_TITLE, getTheme());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_reply, null);

        mCaptchaImage = (ImageView) v.findViewById(R.id.captcha);
        mCaptchaResponse = (EditText) v.findViewById(R.id.captcha_response);
        mSubmitText = (EditText) v.findViewById(R.id.submit_body);
        mTitle = (EditText) v.findViewById(R.id.title);
        mConfirm = (Button) v.findViewById(R.id.confirm_reply);
        mCancel = (Button) v.findViewById(R.id.cancel_reply);
        mCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dismiss();
            }
        });
        mConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SubmitAsyncTask().execute();
            }
        });
        return v;
    }

    public static SubmitDialogFragment newInstance(User user, String subreddit) {
        Bundle b = new Bundle();
        b.putParcelable("user", user);
        b.putString("subreddit", subreddit);
        SubmitDialogFragment rf = new SubmitDialogFragment();
        rf.setArguments(b);
        return rf;
    }

    private class SubmitAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            if (mUser != null && mSubmitText != null && mSubmitText.getText() != null
                    && mSubmitText.getText().toString().length() != 0) {
                List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                apiParams.add(new BasicNameValuePair("api-type", "json"));
                apiParams.add(new BasicNameValuePair("captcha", ""));
                apiParams.add(new BasicNameValuePair("extension", ""));
                apiParams.add(new BasicNameValuePair("iden", mCaptcha.getIden()));
                apiParams.add(new BasicNameValuePair("kind", "self"));
                apiParams.add(new BasicNameValuePair("resubmit", "false"));
                apiParams.add(new BasicNameValuePair("save", "false"));
                apiParams.add(new BasicNameValuePair("sendreplies", "false"));
                apiParams.add(new BasicNameValuePair("sr", mSubreddit));
                apiParams.add(new BasicNameValuePair("text", mSubmitText.getText().toString()));
                apiParams.add(new BasicNameValuePair("then", "comments"));
                apiParams.add(new BasicNameValuePair("title", mTitle.getText().toString()));
                apiParams.add(new BasicNameValuePair("url", ""));
                Log.i("SubmitDialogFragment", "Response = " + Utilities.post(apiParams,
                        "http://www.reddit.com/api/submit", mUser.getCookie(),
                        mUser.getModhash()));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void v) {
            dismiss();
        }
    }

    private class RetrieveCaptchaAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.i("RetrieveCaptcha", Utilities.get("",
                        "http://www.reddit.com/api/needs_captcha.json",
                        mUser.getCookie(), mUser.getModhash()));
            } catch (IOException e) {
                e.printStackTrace();
            }

            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("api-type", "json"));
            Log.i("RetrieveCaptcha", Utilities.post(apiParams,
                    "http://www.reddit.com/api/new_captcha", mUser.getCookie(),
                    mUser.getModhash()));
            return null;
        }
    }

}
