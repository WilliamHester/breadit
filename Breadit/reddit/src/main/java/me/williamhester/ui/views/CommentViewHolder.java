package me.williamhester.ui.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import me.williamhester.models.AccountManager;
import me.williamhester.models.Comment;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.ui.text.ClickableLinkMovementMethod;

/**
 * CommentViewHolder is an extension of the VotableViewHolder and contains all of the necessary
 * information to display a Comment and call back to its parent upon an event.
 *
 * Created by william on 8/1/14.
 */
public class CommentViewHolder extends VotableViewHolder {

    protected Comment mComment;
    private CommentCallbacks mCallback;
    private TextView mAuthor;
    private TextView mFlairText;
    private View mContent;
    private View mLevelIndicator;
    private View mGoldIndicator;
    private View mOptionsRow;

    public CommentViewHolder(View itemView, CommentCallbacks callbacks) {
        super(itemView);
        mCallback = callbacks;
        mBody.setMovementMethod(new ClickableLinkMovementMethod());
        mContent = itemView.findViewById(R.id.comment_content);
        mAuthor = (TextView) itemView.findViewById(R.id.author);
        mFlairText = (TextView) itemView.findViewById(R.id.flair);
        mLevelIndicator = itemView.findViewById(R.id.level_indicator);
        mGoldIndicator = itemView.findViewById(R.id.gold_indicator);
        mOptionsRow = itemView.findViewById(R.id.options_row);

        View optionShare = itemView.findViewById(R.id.option_share);
        View optionViewUser = itemView.findViewById(R.id.option_view_user);
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
        optionViewUser.setOnClickListener(optionsRowClickListener);
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
                        // Do nothing except close the dialog.
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
//                    optionSave.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
                    // TODO: Fix this so that it actually saves
                    optionSave.setVisibility(View.GONE);
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

    @Override
    protected void onVoted() {
        // Don't do anything
    }

    @Override
    public void expandOptions() {

    }

    @Override
    public void collapseOptions() {
        collapse(mOptionsRow);
    }

    public void setContent(Object comment) {
        super.setContent(comment);
        mComment = (Comment) comment;
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

        if (mComment.getAuthor().equals(mCallback.getSubmissionAuthor())
                || mComment.getAuthor().equals(mComment.getLinkAuthor())) {
            mAuthor.setBackgroundResource(R.drawable.author_background);
            mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
        } else if (mComment.getDistinguished() != null) {
            switch (mComment.getDistinguished()) {
                case "moderator":
                    mAuthor.setBackgroundResource(R.drawable.mod_background);
                    mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
                    break;
                case "admin":
                    mAuthor.setBackgroundResource(R.drawable.admin_background);
                    mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
                    break;
                default:
                    mAuthor.setBackground(null);
                    mAuthor.setTextColor(itemView.getResources().getColor(R.color.comment_metadata_gray));
            }
        } else {
            mAuthor.setBackground(null);
            mAuthor.setTextColor(itemView.getResources().getColor(R.color.comment_metadata_gray));
        }
        mAuthor.setText(mComment.getAuthor());
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf(mComment.getScore()))
                .append(' ')
                .append(itemView.getResources().getQuantityString(R.plurals.points, mComment.getScore()))
                .append(' ')
                .append(calculateTimeShort(mComment.getCreatedUtc()));
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

    private View.OnClickListener mHideCommentsClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mCallback.onBodyClick(CommentViewHolder.this, mComment);
        }
    };

    public interface CommentCallbacks {
        public void onBodyClick(CommentViewHolder viewHolder, Comment comment);
        public void onCommentLongPressed(CommentViewHolder holder);
        public void onOptionsRowItemSelected(View view, Comment comment);
        public String getSubmissionAuthor();
    }
}
