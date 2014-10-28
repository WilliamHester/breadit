package me.williamhester.ui.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import me.williamhester.models.AbsComment;
import me.williamhester.models.AccountManager;
import me.williamhester.models.Comment;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;

/**
 * Created by william on 8/1/14.
 */
public class CommentViewHolder extends VotableViewHolder {

    private Comment mComment;
    private CommentClickCallbacks mCallback;
    private String mSubmissionAuthor;
    private TextView mAuthor;
    private TextView mFlairText;
    private View mContent;
    private View mLevelIndicator;
    private View mGoldIndicator;
    private View mOptionsRow;

    public CommentViewHolder(View itemView, CommentClickCallbacks callbacks, String submissionAuthor) {
        super(itemView);
        mCallback = callbacks;
        mBody.setMovementMethod(new CommentLinkMovementMethod());
        mSubmissionAuthor = submissionAuthor;
        mContent = itemView.findViewById(R.id.comment_content);
        mAuthor = (TextView) itemView.findViewById(R.id.author);
        mFlairText = (TextView) itemView.findViewById(R.id.flair);
        mLevelIndicator = itemView.findViewById(R.id.level_indicator);
        mGoldIndicator = itemView.findViewById(R.id.gold_indicator);
        mOptionsRow = itemView.findViewById(R.id.options_row);

        View optionShare = itemView.findViewById(R.id.option_share);
        final View optionLinks = itemView.findViewById(R.id.option_links);
        final View optionReply = itemView.findViewById(R.id.option_reply);
        final View optionEdit = itemView.findViewById(R.id.option_edit);
        final View optionSave = itemView.findViewById(R.id.option_save);
        final View optionOverflow = itemView.findViewById(R.id.option_overflow);

        View.OnClickListener optionsRowClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onOptionsRowItemSelected(v, mComment);
            }
        };

        optionShare.setOnClickListener(optionsRowClickListener);
        optionLinks.setOnClickListener(optionsRowClickListener);
        optionEdit.setOnClickListener(optionsRowClickListener);
        optionReply.setOnClickListener(optionsRowClickListener);
        optionSave.setOnClickListener(optionsRowClickListener);
        optionOverflow.setOnClickListener(optionsRowClickListener);

        mFlairText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage(Html.fromHtml((mComment).getFlairText()).toString());
                builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.show();
            }
        });
        mContent.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOptionsRow.getVisibility() == View.VISIBLE) {
                    mCallback.onCommentLongPressed(null);
                } else if (!isHidden()) {
                    optionLinks.setVisibility(
                            mComment.getLinks().size() > 0 ? View.VISIBLE : View.GONE);
                    optionReply.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
                    optionSave.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
                    optionOverflow.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
                    optionEdit.setVisibility(AccountManager.isLoggedIn()
                            && AccountManager.getAccount().getUsername()
                            .equalsIgnoreCase(mComment.getAuthor()) ? View.VISIBLE : View.GONE);
                    mCallback.onCommentLongPressed(CommentViewHolder.this);
                    expand(mOptionsRow);
                }
                return true;
            }
        });
    }

    public void setContent(Comment comment) {
        super.setContent(comment);
        mComment = comment;
        float dp = itemView.getResources().getDisplayMetrics().density;
        itemView.setPadding(Math.round(4 * dp * mComment.getLevel()), 0, 0, 0);
        mOptionsRow.setVisibility(View.GONE);
        if (mComment.getLevel() > 0) {
            mLevelIndicator.setVisibility(View.VISIBLE);
            switch (mComment.getLevel() % 4) {
                case 1:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.green));
                    break;
                case 2:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.cyan));
                    break;
                case 3:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.blue));
                    break;
                case 0:
                    mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.pink));
                    break;
            }
        } else {
            mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.card_view_gray));
        }
        mContent.setOnClickListener(mHideCommentsClickListener);
        if (mComment.getSpannableBody() == null && mComment.getBodyHtml() != null) {
            HtmlParser parser = new HtmlParser(Html.fromHtml(mComment.getBodyHtml()).toString());
            mComment.setSpannableBody(parser.getSpannableString());
            mComment.setLinks(parser.getLinks());
        }
        mBody.setText(mComment.getSpannableBody());
        if (mComment.isHidden()) {
            mBody.setVisibility(View.GONE);
        } else {
            mBody.setVisibility(View.VISIBLE);
        }

        mGoldIndicator.setVisibility(mComment.isGilded() ? View.VISIBLE : View.INVISIBLE);

        if (mComment.getAuthor().equals(mSubmissionAuthor)) {
            mAuthor.setBackgroundResource(R.drawable.author_background);
            mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
        } else {
            mAuthor.setBackground(null);
            mAuthor.setTextColor(itemView.getResources().getColor(R.color.comment_metadata_gray));
        }
        mAuthor.setText(mComment.getAuthor());
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(mComment.getScore()))
                .append(" ")
                .append(itemView.getResources().getQuantityString(R.plurals.points, mComment.getScore()));
        mMetadata.setText(sb);
        if (!TextUtils.isEmpty(mComment.getFlairText())) {
            mFlairText.setVisibility(View.VISIBLE);
            mFlairText.setBackgroundResource(R.drawable.flair_background);
            mFlairText.setText(Html.fromHtml(mComment.getFlairText()).toString());
        } else {
            mFlairText.setVisibility(View.GONE);
        }
    }

    public boolean isHidden() {
        return mComment.isHidden();
    }

    public void collapseOptions() {
        collapse(mOptionsRow);
    }

    private View.OnClickListener mHideCommentsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallback.onBodyClick(CommentViewHolder.this, (Comment) mComment);
        }
    };

    private static class CommentLinkMovementMethod extends LinkMovementMethod {
        @Override
        public boolean onTouchEvent(@NonNull TextView widget, @NonNull Spannable buffer,
                                    @NonNull MotionEvent event) {
            int action = event.getAction();
            if (action == MotionEvent.ACTION_UP ||
                    action == MotionEvent.ACTION_DOWN) {
                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= widget.getTotalPaddingLeft();
                y -= widget.getTotalPaddingTop();
                x += widget.getScrollX();
                y += widget.getScrollY();
                Layout layout = widget.getLayout();
                int line = layout.getLineForVertical(y);
                int off = layout.getOffsetForHorizontal(line, x);
                ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);
                if (link.length != 0) {
                    if (action == MotionEvent.ACTION_UP) {
                        link[0].onClick(widget);
                    } else {
                        Selection.setSelection(buffer,
                                buffer.getSpanStart(link[0]),
                                buffer.getSpanEnd(link[0]));
                    }
                    return true;
                } else {
                    widget.getParent().requestDisallowInterceptTouchEvent(false);
                    ((View) widget.getParent()).onTouchEvent(event);
                }
            } else {
                ((View) widget.getParent()).onTouchEvent(event);
                Selection.removeSelection(buffer);
            }
            return false;
        }
    }

    public interface CommentClickCallbacks {
        public void onBodyClick(CommentViewHolder viewHolder, Comment comment);
        public void onCommentLongPressed(CommentViewHolder holder);
        public void onOptionsRowItemSelected(View view, AbsComment submission);
    }
}
