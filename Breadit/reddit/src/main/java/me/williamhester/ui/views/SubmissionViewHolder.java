package me.williamhester.ui.views;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.List;

import me.williamhester.models.AccountManager;
import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.Url;

/**
 * This class is intended to be used with the RecyclerView class; however, it can be used nearly
 * just as easily with ListView to provide the View Holder pattern for optimization.
 *
 * TODO: break this up into a few different ViewHolders and use different item types.
 *
 * Created by William on 10/19/14.
 */
public class SubmissionViewHolder extends VotableViewHolder {

    private TextView mDomain;
    private TextView mCommentData;
    private TextView mSubreddit;
    private View mContainer;
    private View mNsfwWarning;
    private View mNsfwBlocker;
    private View mOptionsRow;

    protected Submission mSubmission;
    protected SubmissionCallbacks mCallback;

    public SubmissionViewHolder(final View itemView, SubmissionCallbacks callbacks) {
        super(itemView);
        mCallback = callbacks;

        mContainer = itemView.findViewById(R.id.content_preview);
        mDomain = (TextView) itemView.findViewById(R.id.domain);
        mCommentData = (TextView) itemView.findViewById(R.id.num_comments);
        mSubreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
        mNsfwWarning = itemView.findViewById(R.id.nsfw_warning);
        mNsfwBlocker = itemView.findViewById(R.id.nsfw_blocker);
        mOptionsRow = itemView.findViewById(R.id.options_row);
        final View submissionData = itemView.findViewById(R.id.submission_data);
        View optionReply = itemView.findViewById(R.id.option_reply);
        View optionUser = itemView.findViewById(R.id.option_view_user);
        View optionShare = itemView.findViewById(R.id.option_share);
        final View optionEdit = itemView.findViewById(R.id.option_edit);
        final View optionSubreddit = itemView.findViewById(R.id.option_go_to_subreddit);
        final View optionSave = itemView.findViewById(R.id.option_save);
        final View optionOverflow = itemView.findViewById(R.id.option_overflow);

        View.OnClickListener mOptionsOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onOptionsRowItemSelected(v, mSubmission);
            }
        };
        optionShare.setOnClickListener(mOptionsOnClickListener);
        optionReply.setOnClickListener(mOptionsOnClickListener);
        optionEdit.setOnClickListener(mOptionsOnClickListener);
        optionSubreddit.setOnClickListener(mOptionsOnClickListener);
        optionUser.setOnClickListener(mOptionsOnClickListener);
        optionSave.setOnClickListener(mOptionsOnClickListener);
        optionOverflow.setOnClickListener(mOptionsOnClickListener);
        submissionData.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                v.onTouchEvent(event);
                return false;
            }
        });
        submissionData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onCardClicked(mSubmission);
            }
        });
        submissionData.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (mOptionsRow.getVisibility() == View.VISIBLE) {
                    mCallback.onCardLongPressed(null);
                } else {
                    expandOptions(optionSubreddit, optionSave, optionOverflow);
                    mCallback.onCardLongPressed(SubmissionViewHolder.this);
                    expand(mOptionsRow);
                }
                return true;
            }
        });
//        mNsfwBlocker.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                view.setVisibility(View.GONE);
//                mSubmission.setShowNsfwContent();
//            }
//        });

    }

    @Override
    protected void onVoted() {
        mCallback.onVoted(mSubmission);
    }

    @Override
    public void setContent(Object object) {
        super.setContent(object);
        mSubmission = (Submission) object;

        mOptionsRow.setVisibility(View.GONE);
        mBody.setText(Html.fromHtml(mSubmission.getTitle()).toString());
        mDomain.setText(mSubmission.getDomain());
        mCommentData.setText(mSubmission.getNumberOfComments() + " "
                + itemView.getResources().getQuantityString(R.plurals.comments,
                mSubmission.getNumberOfComments()));
        mSubreddit.setText(mSubmission.getSubredditName().toLowerCase());
        mMetadata.setText(mSubmission.getAuthor() + " " + mSubmission.getScore() + " "
                + itemView.getResources().getQuantityString(R.plurals.points,
                mSubmission.getScore()));

        if (!mSubmission.isNsfw()) {
            mNsfwWarning.setVisibility(View.GONE);
            if (!mSubmission.isSelf()) {
//                mNsfwBlocker.setVisibility(View.GONE);
            }
        } else if (!mSubmission.isSelf() && mSubmission.isShowingNsfw()) {
            mNsfwWarning.setVisibility(View.VISIBLE);
//            mNsfwBlocker.setVisibility(View.GONE);
        } else {
            mNsfwWarning.setVisibility(View.VISIBLE);
            if (!mSubmission.isSelf()) {
//                mNsfwBlocker.setVisibility(View.VISIBLE);
            } else {
//                mNsfwBlocker.setVisibility(View.GONE);
            }
        }
    }

    public void disableClicks() {
        View subData = itemView.findViewById(R.id.submission_data);
        subData.setOnClickListener(null);
        subData.setOnLongClickListener(null);
        subData.setClickable(false);
    }

    public void collapseOptions() {
        collapse(mOptionsRow);
    }

    public void expandOptions() {
        mOptionsRow.setVisibility(View.VISIBLE);
        expandOptions(itemView.findViewById(R.id.option_go_to_subreddit),
                itemView.findViewById(R.id.option_save),
                itemView.findViewById(R.id.option_overflow));
        itemView.findViewById(R.id.option_reply).setVisibility(AccountManager.isLoggedIn()
                ? View.VISIBLE : View.GONE);
        itemView.findViewById(R.id.option_edit).setVisibility(AccountManager.isLoggedIn()
                && AccountManager.getAccount().getUsername()
                .equalsIgnoreCase(mSubmission.getAuthor()) ? View.VISIBLE : View.GONE);
    }

    protected void expandOptions(View optionSubreddit, View optionSave, View optionOverflow) {
        optionSubreddit.setVisibility(mCallback.isFrontPage() ? View.VISIBLE : View.GONE);
        optionOverflow.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
        optionSave.setVisibility(View.GONE); // TODO: actually allow the user to save.
//        optionSave.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
    }

    public static interface SubmissionCallbacks {
        public void onImageViewClicked(Object imgurData);
        public void onImageViewClicked(String imageUrl);
        public void onLinkClicked(Submission submission);
        public void onYouTubeVideoClicked(String videoId);
        public void onCardClicked(Submission submission);
        public void onCardLongPressed(SubmissionViewHolder holder);
        public void onOptionsRowItemSelected(View view, Submission submission);
        public void onVoted(Submission submission);
        public boolean isFrontPage();
    }
}
