package me.williamhester.ui.fragments;

import android.content.Context;
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

import java.text.DecimalFormat;

import me.williamhester.reddit.R;

/**
 * Created by william on 10/21/14.
 */
public abstract class MarkdownBodyFragment extends Fragment {

    private static final int MODE_NONE = 0;
    private static final int MODE_BULLETS = 1;
    private static final int MODE_NUMBERED = 2;

    protected EditText mBody;
    protected boolean mKillOnStart; // Fragments don't like to be killed asynchronously
    private int mListMode;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mKillOnStart = savedInstanceState.getBoolean("killOnStart");
            mListMode = savedInstanceState.getInt("listMode");
        }
    }

    protected abstract CharSequence getBodyHint();
    protected abstract CharSequence getButtonText();
    protected abstract int getLayoutId();
    protected abstract void onSaveClick();

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(getLayoutId(), container, false);

        final DecimalFormat format = new DecimalFormat("##,##0");
        final TextView charCount = (TextView) v.findViewById(R.id.char_count);
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

        charCount.setText(format.format(mBody.length()) + "/10,000");
        mBody.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 10000) {
                    s.delete(10000, s.length()); // Remove the 10,000th character.
                }
                charCount.setText(format.format(s.length()) + "/10,000");
            }
        });
        mBody.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == KeyEvent.KEYCODE_ENTER
                        || actionId == KeyEvent.KEYCODE_NUMPAD_ENTER) {
                    switch (mListMode) {
                        case MODE_BULLETS:
                            insertBullet();
                            break;
                        case MODE_NUMBERED:
                            insertNumber();
                            break;
                    }
                }
                return false;
            }
        });
        mBody.setHint(getBodyHint());
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_reply, menu);

        Button button = (Button) menu.findItem(R.id.action_reply).getActionView();
        button.setText(getButtonText());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

        mBody.requestFocus();
        InputMethodManager imm =  (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mBody, InputMethodManager.SHOW_IMPLICIT);

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
                    insertInitialBullet();
                    if (mListMode == MODE_NUMBERED || mListMode == MODE_NONE) {
                        getView().findViewById(R.id.numbered_list_selector).setVisibility(View.GONE);
                        getView().findViewById(R.id.bullet_selector).setVisibility(View.VISIBLE);
                    } else if (mListMode == MODE_BULLETS) {
                        getView().findViewById(R.id.bullet_selector).setVisibility(View.GONE);
                        mListMode = MODE_NONE;
                    }
                    mListMode = MODE_BULLETS;
                    break;
                case R.id.action_numbered_list:
                    insertInitialNumber();
                    if (mListMode == MODE_BULLETS) {
                        getView().findViewById(R.id.bullet_selector).setVisibility(View.GONE);
                        getView().findViewById(R.id.numbered_list_selector).setVisibility(View.VISIBLE);
                    } else if (mListMode == MODE_NUMBERED) {
                        getView().findViewById(R.id.numbered_list_selector).setVisibility(View.GONE);
                        mListMode = MODE_NONE;
                    }
                    mListMode = MODE_NUMBERED;
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

    private void insertInitialBullet() {
        Editable editable = mBody.getEditableText();
        int start = mBody.getSelectionStart();
        int end = mBody.getSelectionEnd();
        if (start == end) {
            String s = editable.toString();
            end--;
            while (end < s.length() && end > 0 && s.charAt(end) != '\n') {
                end--;
            }
            if (end <= 0) {
                editable.insert(0, "* ");
            } else if (s.charAt(end - 1) == '\n') {
                editable.insert(end, "* ");
            } else {
                editable.insert(end, "\n* ");
            }
        } else {
            editable.append("\n* ");
        }
    }

    private void insertInitialNumber() {
        Editable editable = mBody.getEditableText();
        int start = mBody.getSelectionStart();
        int end = mBody.getSelectionEnd();
        if (start == end) {
            String s = editable.toString();
            end--;
            while (end < s.length() && end > 0 && s.charAt(end) != '\n') {
                end--;
            }
            if (end <= 0) {
                editable.insert(0, "1. ");
            } else if (s.charAt(end - 1) == '\n') {
                editable.insert(end, "1. ");
            } else {
                editable.insert(end, "\n1. ");
            }
        } else {
            editable.append("\n1. ");
        }
    }

    private void insertBullet() {
        Editable editable = mBody.getEditableText();
        editable.append("\n* ");
    }

    private void insertNumber() {
        Editable editable = mBody.getEditableText();
        editable.append("\n1. ");
    }
}
