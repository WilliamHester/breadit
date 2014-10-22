package me.williamhester.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;

import java.text.DecimalFormat;
import java.util.ArrayList;

import me.williamhester.models.Thing;
import me.williamhester.models.Votable;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/19/14.
 */
public class ReplyFragment extends MarkdownBodyFragment {

    private boolean mIsEditing;

    public static ReplyFragment newInstance(Thing thing) {
        Bundle b = new Bundle();
        b.putParcelable("thing", thing);
        ReplyFragment fragment = new ReplyFragment();
        fragment.setArguments(b);
        return fragment;
    }

    public static ReplyFragment newInstance(Thing parent, Votable votable) {
        Bundle b = new Bundle();
        b.putParcelable("thing", parent);
        b.putParcelable("votable", votable);
        ReplyFragment fragment = new ReplyFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_reply;
    }

    @Override
    protected String getBodyHint() {
        Thing parent = getArguments().getParcelable("thing");
        if (parent != null) {
            return getResources().getString(R.string.reply_hint) + " /u/" + parent.getAuthor();
        } else {
            return getResources().getString(R.string.enter_self_text);
        }
    }

    @Override
    protected String getButtonText() {
        return getResources().getString(R.string.send_reply);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsEditing = getArguments().containsKey("votable");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        if (mIsEditing && savedInstanceState == null) {
            Votable votable = getArguments().getParcelable("votable");
            mBody.setText(votable.getRawMarkdown());
        }
        return view;
    }

    protected void onSaveClick() {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage(getResources().getString(R.string.sending_reply));
        dialog.setCancelable(false);
        dialog.show();
        final Thing parent = getArguments().getParcelable("thing");
        final Votable votable = getArguments().getParcelable("votable");
        FutureCallback<ArrayList<Thing>> callback = new FutureCallback<ArrayList<Thing>>() {
            @Override
            public void onCompleted(final Exception e,
                                    final ArrayList<Thing> result) {
                mBody.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (e != null) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(),
                                    R.string.failed_to_post_reply,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent i = new Intent();
                        Bundle resultBundle = new Bundle();
                        resultBundle.putParcelable("oldThing", votable);
                        resultBundle.putParcelable("newComment", result.get(0));
                        resultBundle.putParcelable("parentThing", parent);
                        i.putExtras(resultBundle);
                        InputMethodManager imm =  (InputMethodManager) getActivity()
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(mBody.getWindowToken(), 0);
                        getTargetFragment().onActivityResult(getTargetRequestCode(),
                                Activity.RESULT_OK, i);
                        if (isResumed()) {
                            getFragmentManager().popBackStack();
                        } else {
                            mKillOnStart = true;
                        }
                    }
                });
            }
        };
        if (mIsEditing) {
            votable.setRawMarkdown(mBody.getText().toString());
            RedditApi.editThing(getActivity(), votable, callback);
        } else {
            RedditApi.replyToComment(getActivity(), parent, mBody.getText().toString(),
                    callback);
        }
    }
}
