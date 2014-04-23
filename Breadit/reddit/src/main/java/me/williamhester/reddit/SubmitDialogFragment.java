package me.williamhester.reddit;

import android.app.DialogFragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Captcha;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/6/14.
 */
public class SubmitDialogFragment extends DialogFragment {

    private Button mConfirm;
    private Button mCancel;
    private EditText mCaptchaResponse;
    private EditText mSubredditName;
    private EditText mSubmitText;
    private EditText mTitle;
    private ImageView mCaptchaImage;
    private Account mAccount;
    private String mSubreddit;

    private boolean mNsfw = false;
    private boolean mSelfPost = true;
    private boolean mSendReplies = true;

    private Captcha mCaptcha;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mAccount = getArguments().getParcelable("account");
            mSubreddit = getArguments().getString("subreddit");
        }
        setStyle(STYLE_NORMAL, android.R.style.Theme_Holo_Dialog);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_fragment_submit, null);
        mCaptchaImage = (ImageView) v.findViewById(R.id.captcha);
        mCaptchaResponse = (EditText) v.findViewById(R.id.captcha_response);
        mSubredditName = (EditText) v.findViewById(R.id.subreddit_name);
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
        CheckBox nsfwCheckbox = (CheckBox) v.findViewById(R.id.nsfw_checkbox);
        nsfwCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mNsfw = b;
            }
        });
        CheckBox selfCheckbox = (CheckBox) v.findViewById(R.id.self_checkbox);
        selfCheckbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mSelfPost = b;
            }
        });
        CheckBox sendReplies = (CheckBox) v.findViewById(R.id.send_replies);
        sendReplies.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mSendReplies = b;
            }
        });

        mCaptchaResponse.setVisibility(View.GONE);
        mCaptchaImage.setVisibility(View.GONE);
        if (mSubreddit != null) {
            mSubredditName.setVisibility(View.GONE);
        }

        getDialog().setTitle(R.string.new_text_post);
        return v;
    }

    public static SubmitDialogFragment newInstance(Account account, String subreddit) {
        Bundle b = new Bundle();
        b.putParcelable("account", account);
        b.putString("subreddit", subreddit);
        SubmitDialogFragment rf = new SubmitDialogFragment();
        rf.setArguments(b);
        return rf;
    }

    private class SubmitAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            Log.i("SDF", mAccount.getUsername());
            if (mAccount != null && mSubmitText != null && mSubmitText.getText() != null
                    && mSubmitText.getText().toString().length() != 0) {
                Log.i("SubmitDialogFragment", "Submitting...");
                List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                apiParams.add(new BasicNameValuePair("api-type", "json"));
                if (mSelfPost) {
                    Log.i("SDF", "self-post");
                    apiParams.add(new BasicNameValuePair("text", mSubmitText.getText().toString()));
                    apiParams.add(new BasicNameValuePair("kind", "self"));
                } else {
                    Log.i("SDF", "link");
                    apiParams.add(new BasicNameValuePair("kind", "link"));
                    apiParams.add(new BasicNameValuePair("url", mSubmitText.getText().toString()));
                }
                apiParams.add(new BasicNameValuePair("captcha", ""));
                apiParams.add(new BasicNameValuePair("extension", ""));
                if (mCaptcha != null)
                    apiParams.add(new BasicNameValuePair("iden", mCaptcha.getIden()));
                apiParams.add(new BasicNameValuePair("resubmit", "false"));
                apiParams.add(new BasicNameValuePair("save", "false"));
                apiParams.add(new BasicNameValuePair("sendreplies", Boolean.toString(mSendReplies)));
                if (mSubreddit != null)
                    apiParams.add(new BasicNameValuePair("sr", mSubreddit));
                else
                    apiParams.add(new BasicNameValuePair("sr", mSubredditName.getText().toString()));
                apiParams.add(new BasicNameValuePair("then", "comments"));
                apiParams.add(new BasicNameValuePair("title", mTitle.getText().toString()));
                Log.i("SubmitDialogFragment", "Response = " + Utilities.post(apiParams,
                        "http://www.reddit.com/api/submit", mAccount));
            } else if (mAccount == null) {
                Log.i("SubmitDialogFragment", "mAccount == null");
            } else if (mSubmitText == null) {
                Log.i("SubmitDialogFragment", "mSubmitText == null");
            } else if (mSubmitText.getText() == null) {
                Log.i("SubmitDialogFragment", "mSubmitText.getText() == null");
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
                boolean needsCaptcha = Boolean.parseBoolean(Utilities.get("",
                        "http://www.reddit.com/api/needs_captcha.json", mAccount));
                if (needsCaptcha) {
                    List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
                    apiParams.add(new BasicNameValuePair("api-type", "json"));
                    Log.i("SubmitDialogFragment", Utilities
                            .post(apiParams, "http://www.reddit.com/api/new_captcha", mAccount));
                    mCaptchaImage.setVisibility(View.VISIBLE);
                    mCaptchaResponse.setVisibility(View.VISIBLE);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

}
