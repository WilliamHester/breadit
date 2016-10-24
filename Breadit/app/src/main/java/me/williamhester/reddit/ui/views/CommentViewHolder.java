package me.williamhester.reddit.ui.views;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.Html;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.Comment;
import me.williamhester.reddit.R;
import me.williamhester.reddit.tools.HtmlParser;
import me.williamhester.reddit.ui.text.ClickableLinkMovementMethod;

/**
 * CommentViewHolder is an extension of the VotableViewHolder and contains all of the necessary
 * information to display a Comment and call back to its parent upon an event.
 *
 * Created by william on 8/1/14.
 */
public class CommentViewHolder extends VotableViewHolder {

  protected Comment mRedditComment;
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
        mCallback.onOptionsRowItemSelected(v, mRedditComment);
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
        builder.setMessage(Html.fromHtml((mRedditComment).getFlairText()).toString());
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
              mRedditComment.getLinks().size() > 0 ? View.VISIBLE : View.GONE);
          optionReply.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
//                    optionSave.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
          // TODO: Fix this so that it actually saves
          optionSave.setVisibility(View.GONE);
          optionOverflow.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
          optionEdit.setVisibility(AccountManager.isLoggedIn()
              && AccountManager.getAccount().getUsername()
              .equalsIgnoreCase(mRedditComment.getAuthor()) ? View.VISIBLE : View.GONE);
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
    mRedditComment = (Comment) comment;
    float dp = itemView.getResources().getDisplayMetrics().density;
    itemView.setPadding(Math.round(4 * dp * mRedditComment.getLevel()), 0, 0, 0);
    mOptionsRow.setVisibility(View.GONE);
    if (mRedditComment.getLevel() > 0) {
      mLevelIndicator.setVisibility(View.VISIBLE);
      switch (mRedditComment.getLevel() % 4) {
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
      mLevelIndicator.setBackgroundColor(mLevelIndicator.getResources().getColor(R.color.primary_dark));
    }
    mContent.setOnClickListener(mHideCommentsClickListener);
    if (mRedditComment.getSpannableBody() == null && mRedditComment.getBodyHtml() != null) {
      HtmlParser parser = new HtmlParser(Html.fromHtml(mRedditComment.getBodyHtml()).toString());
      mRedditComment.setSpannableBody(parser.getSpannableString());
      mRedditComment.setLinks(parser.getLinks());
    }
    mBody.setText(mRedditComment.getSpannableBody());
    if (mRedditComment.isHidden()) {
      mBody.setVisibility(View.GONE);
    } else {
      mBody.setVisibility(View.VISIBLE);
    }

    mGoldIndicator.setVisibility(mRedditComment.isGilded() ? View.VISIBLE : View.INVISIBLE);

    if (mRedditComment.getAuthor().equals(mCallback.getSubmissionAuthor())
        || mRedditComment.getAuthor().equals(mRedditComment.getLinkAuthor())) {
      mAuthor.setBackgroundResource(R.drawable.author_background);
      mAuthor.setTextColor(itemView.getResources().getColor(R.color.ghostwhite));
    } else if (mRedditComment.getDistinguished() != null) {
      switch (mRedditComment.getDistinguished()) {
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
    mAuthor.setText(mRedditComment.getAuthor());
    StringBuilder sb = new StringBuilder();
    sb.append(String.valueOf(mRedditComment.getScore()))
        .append(' ')
        .append(itemView.getResources().getQuantityString(R.plurals.points, mRedditComment.getScore()))
        .append(' ')
        .append(calculateTimeShort(mRedditComment.getCreatedUtc()));
    mMetadata.setText(sb);
    if (!TextUtils.isEmpty(mRedditComment.getFlairText())) {
      mFlairText.setVisibility(View.VISIBLE);
      mFlairText.setBackgroundResource(R.drawable.flair_background);
      mFlairText.setText(Html.fromHtml(mRedditComment.getFlairText()).toString());
    } else {
      mFlairText.setVisibility(View.GONE);
    }
  }

  public boolean isHidden() {
    return mRedditComment.isHidden();
  }

  private View.OnClickListener mHideCommentsClickListener = new View.OnClickListener() {
    @Override
    public void onClick(View view) {
      mCallback.onBodyClick(CommentViewHolder.this, mRedditComment);
    }
  };

  public interface CommentCallbacks {
    public void onBodyClick(CommentViewHolder viewHolder, Comment redditComment);

    public void onCommentLongPressed(CommentViewHolder holder);

    public void onOptionsRowItemSelected(View view, Comment redditComment);

    public String getSubmissionAuthor();
  }
}
