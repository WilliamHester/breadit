package me.williamhester.ui.views;

import android.annotation.TargetApi;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.DecimalFormat;

import me.williamhester.reddit.R;

/**
 * Created by william on 11/14/14.
 */
public class MarkdownBodyView extends LinearLayout {

    private static final int MODE_NONE = 0;
    private static final int MODE_BULLETS = 1;
    private static final int MODE_NUMBERED = 2;
    
    private Context mContext;
    private EditText mBody;

    private int mListMode = MODE_NONE;

    public MarkdownBodyView(Context context) {
        this(context, null, 0);
    }

    public MarkdownBodyView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MarkdownBodyView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @TargetApi(21)
    public MarkdownBodyView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mContext = context;
        
        LayoutInflater layoutInflater =
                (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_markdown_body, this);
        
        init();
    }

    private void init() {
        final DecimalFormat format = new DecimalFormat("##,##0");
        final TextView charCount = (TextView) findViewById(R.id.char_count);
        mBody = (EditText) findViewById(R.id.reply_body);
        View actionLink = findViewById(R.id.action_link);
        View actionBold = findViewById(R.id.action_bold);
        View actionItalics = findViewById(R.id.action_italics);
        View actionStrikeThrough = findViewById(R.id.action_strikethrough);
        View actionSuperscript = findViewById(R.id.action_superscript);
        View actionBullets = findViewById(R.id.action_bullets);
        View actionNumberedList = findViewById(R.id.action_numbered_list);

        actionLink.setOnClickListener(mTextFormattingClickListener);
        actionBold.setOnClickListener(mTextFormattingClickListener);
        actionItalics.setOnClickListener(mTextFormattingClickListener);
        actionStrikeThrough.setOnClickListener(mTextFormattingClickListener);
        actionSuperscript.setOnClickListener(mTextFormattingClickListener);
        actionBullets.setOnClickListener(mTextFormattingClickListener);
        actionNumberedList.setOnClickListener(mTextFormattingClickListener);

        charCount.setText(format.format(mBody.length()) + "/10,000");
        mBody.addTextChangedListener(new TextWatcher() {
            private DecimalFormat format = new DecimalFormat("##,##0");

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Do nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Do nothing
            }

            @Override
            public void afterTextChanged(Editable s) {
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
    }

    public void setHint(String hint) {
        mBody.setHint(hint);
    }

    public String getBody() {
        return mBody.getText().toString();
    }

    /**
     * Sets the body text of the portion
     *
     * @param body the string that should go in the body.
     */
    public void setBody(String body) {
        mBody.setText(body);
    }

    /**
     * This method should be called by the parent Fragment during its onResume method to show the
     * keyboard with this View's EditText focused.
     */
    public void showKeyboard() {
        mBody.requestFocus();
        InputMethodManager imm =  (InputMethodManager) mContext.getSystemService(
                Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(mBody, InputMethodManager.SHOW_IMPLICIT);
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
                        findViewById(R.id.numbered_list_selector).setVisibility(View.GONE);
                        findViewById(R.id.bullet_selector).setVisibility(View.VISIBLE);
                    } else if (mListMode == MODE_BULLETS) {
                        findViewById(R.id.bullet_selector).setVisibility(View.GONE);
                        mListMode = MODE_NONE;
                    }
                    mListMode = MODE_BULLETS;
                    break;
                case R.id.action_numbered_list:
                    insertInitialNumber();
                    if (mListMode == MODE_BULLETS) {
                        findViewById(R.id.bullet_selector).setVisibility(View.GONE);
                        findViewById(R.id.numbered_list_selector).setVisibility(View.VISIBLE);
                    } else if (mListMode == MODE_NUMBERED) {
                        findViewById(R.id.numbered_list_selector).setVisibility(View.GONE);
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
