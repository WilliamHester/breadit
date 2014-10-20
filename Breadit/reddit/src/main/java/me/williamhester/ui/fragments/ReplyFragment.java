package me.williamhester.ui.fragments;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.models.AbsComment;
import me.williamhester.models.Comment;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/19/14.
 */
public class ReplyFragment extends Fragment {

    private static final int MODE_NONE = 0;
    private static final int MODE_BULLETS = 1;
    private static final int MODE_NUMBERED = 2;

    private EditText mBody;
    private boolean mKillOnStart; // Fragments don't like to be killed asynchronously
    private int mListMode;

    public static ReplyFragment newInstance(Comment comment) {
        Bundle b = new Bundle();
        b.putParcelable("comment", comment);
        ReplyFragment fragment = new ReplyFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        ((ActionBarActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (savedInstanceState != null) {
            mKillOnStart = savedInstanceState.getBoolean("killOnStart");
            mListMode = savedInstanceState.getInt("listMode");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reply, container, false);
        mBody = (EditText) v.findViewById(R.id.reply_body);
        View actionLink = v.findViewById(R.id.action_link);
        View actionBold = v.findViewById(R.id.action_bold);
        View actionItalics = v.findViewById(R.id.action_italics);
        View actionStrikethrough = v.findViewById(R.id.action_strikethrough);
        View actionSuperscript = v.findViewById(R.id.action_superscript);
        View actionBullets = v.findViewById(R.id.action_bullets);
        View actionNumberedList = v.findViewById(R.id.action_numbered_list);

        actionLink.setOnClickListener(mTextFormattingClickListener);
        actionBold.setOnClickListener(mTextFormattingClickListener);
        actionItalics.setOnClickListener(mTextFormattingClickListener);
        actionStrikethrough.setOnClickListener(mTextFormattingClickListener);
        actionSuperscript.setOnClickListener(mTextFormattingClickListener);
        actionBullets.setOnClickListener(mTextFormattingClickListener);
        actionNumberedList.setOnClickListener(mTextFormattingClickListener);

        Comment parent = getArguments().getParcelable("comment");
        mBody.setHint(getResources().getString(R.string.reply_hint) + " /u/" + parent.getAuthor());
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_reply, menu);

        Button button = (Button) menu.findItem(R.id.action_reply).getActionView();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ProgressDialog dialog = new ProgressDialog(getActivity());
                dialog.setMessage(getResources().getString(R.string.sending_reply));
                dialog.setCancelable(false);
                dialog.show();
                final Comment parent = getArguments().getParcelable("comment");
                RedditApi.replyToComment(getActivity(), parent,
                        mBody.getText().toString(), new FutureCallback<ArrayList<AbsComment>>() {
                            @Override
                            public void onCompleted(final Exception e,
                                                    final ArrayList<AbsComment> result) {
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
                                        resultBundle.putParcelable("newComment", result.get(0));
                                        resultBundle.putParcelable("parentComment", parent);
                                        i.putExtras(resultBundle);
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
                        });
            }
        });
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
        outState.putInt("listMode", mListMode);
    }

    private View.OnClickListener mTextFormattingClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.action_link:
                    wrapSelection("[", "]()");
                    break;
                case R.id.action_bold:
                    wrapSelection("**");
                    break;
                case R.id.action_italics:
                    wrapSelection("*");
                    break;
                case R.id.action_strikethrough:
                    wrapSelection("~~");
                    break;
                case R.id.action_superscript:
                    wrapSelection("^", "");
                    break;
                case R.id.action_bullets:
                    break;
                case R.id.action_numbered_list:
                    break;
            }
        }
    };

    private void wrapSelection(String wrapper) {
        wrapSelection(wrapper, wrapper);
    }

    private void wrapSelection(String beginning, String endWrap) {
        Editable editable = mBody.getEditableText();
        int start = mBody.getSelectionStart();
        int end = mBody.getSelectionEnd() + beginning.length();
        editable.insert(start, beginning);
        editable.insert(end, endWrap);
        if (start == end - beginning.length()) { // There was no selection
            mBody.setSelection(end);
        }
    }
}
