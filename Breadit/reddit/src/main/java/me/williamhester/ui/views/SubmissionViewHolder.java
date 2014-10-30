package me.williamhester.ui.views;

import android.text.Html;
import android.text.method.LinkMovementMethod;
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
    private TextView mSelfText;
    private ImageView mImageView;
    private ImageButton mImageButton;
    private View mContainer;
    private View mNsfwWarning;
    private View mNsfwBlocker;
    private View mOptionsRow;
    private View mExpandButton;
    private View mShowSelfText;

    private Submission mSubmission;
    private SubmissionCallbacks mCallback;

    public SubmissionViewHolder(final View itemView, SubmissionCallbacks callbacks) {
        super(itemView);
        mCallback = callbacks;

        mContainer = itemView.findViewById(R.id.content_preview);
        mDomain = (TextView) itemView.findViewById(R.id.domain);
        mCommentData = (TextView) itemView.findViewById(R.id.num_comments);
        mSubreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
        mNsfwWarning = itemView.findViewById(R.id.nsfw_warning);
        mNsfwBlocker = itemView.findViewById(R.id.nsfw_blocker);
        mExpandButton = itemView.findViewById(R.id.expand_self_text);
        mSelfText = (TextView) itemView.findViewById(R.id.self_text);
        mShowSelfText = itemView.findViewById(R.id.show_self_text);
        mImageView = (ImageView) itemView.findViewById(R.id.image);
        mImageButton = (ImageButton) itemView.findViewById(R.id.preview_button);
        mOptionsRow = itemView.findViewById(R.id.options_row);
        View submissionData = itemView.findViewById(R.id.submission_data);
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
        mNsfwBlocker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                view.setVisibility(View.GONE);
                mSubmission.setShowNsfwContent();
            }
        });

        View.OnClickListener mExpandListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Animation anim;
                if (mSubmission.isSelftextOpen()) {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_left);
                    collapse(mSelfText);
                } else {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_right);
                    expand(mSelfText);
                }
                mSubmission.setSelftextOpen(!mSubmission.isSelftextOpen());
                anim.setFillBefore(true);
                anim.setFillAfter(true);
                view.startAnimation(anim);
            }
        };
        mExpandButton.setOnClickListener(mExpandListener);
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
        mCommentData.setText(mSubmission.getNumberOfComments() + " comments");
        mSubreddit.setText(mSubmission.getSubredditName().toLowerCase());
        mMetadata.setText(mSubmission.getAuthor() + " " + mSubmission.getScore() + " "
                + itemView.getResources().getQuantityString(R.plurals.points,
                mSubmission.getScore()));
        setUpNsfw();

        if (mSubmission.isSelf()) {
            showSelfText(mExpandButton, mContainer, mImageView, mImageButton);
        } else {
            mSelfText.setText("");
            mSelfText.setVisibility(View.GONE);
            mShowSelfText.setVisibility(View.GONE);
            final Url linkDetails = new Url(mSubmission.getUrl());
            mContainer.setVisibility(View.VISIBLE);
            String id = linkDetails.getLinkId();
            switch (linkDetails.getType()) {
                case Url.IMGUR_IMAGE: {
                    mContainer.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    mImageButton.setVisibility(View.GONE);
                    if (mSubmission.getImgurData() == null) {
                        mImageView.setImageDrawable(null);
                        ImgurApi.getImageDetails(id, itemView.getContext(), mSubmission, mImgurCallback);
                    } else {
                        setImagePreview();
                    }
                    break;
                }
                case Url.IMGUR_ALBUM: {
                    mContainer.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    mImageButton.setVisibility(View.GONE);
                    if (mSubmission.getImgurData() == null) {
                        mImageView.setImageDrawable(null);
                        ImgurApi.getAlbumDetails(id, itemView.getContext(), mSubmission, mImgurCallback);
                    } else {
                        setImagePreview();
                    }
                    break;
                }
                case Url.YOUTUBE: {
                    mContainer.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    ImgurApi.loadImage(linkDetails.getUrl(), mImageView, null);
                    mImageButton.setImageResource(R.drawable.ic_youtube);
                    mImageButton.setVisibility(View.VISIBLE);
                    mImageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallback.onYouTubeVideoClicked(linkDetails.getLinkId());
                        }
                    });
                    break;
                }
                case Url.NORMAL_IMAGE: {
                    mContainer.setVisibility(View.VISIBLE);
                    mImageView.setVisibility(View.VISIBLE);
                    mImageButton.setVisibility(View.GONE);
                    ImgurApi.loadImage(linkDetails.getUrl(), mImageView, null);
                    mImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallback.onImageViewClicked(mSubmission.getUrl());
                        }
                    });
                    break;
                }
                default: { // It's special, but not special enough
                    mContainer.setVisibility(View.GONE);
                }
            }
        }
    }

    public void disableClicks() {
        View subData = itemView.findViewById(R.id.submission_data);
        subData.setOnClickListener(null);
        subData.setOnLongClickListener(null);
        subData.setClickable(false);
    }

    private void setUpNsfw() {
        if (!mSubmission.isNsfw()) {
            mNsfwWarning.setVisibility(View.GONE);
            if (!mSubmission.isSelf()) {
                mNsfwBlocker.setVisibility(View.GONE);
            }
        } else if (!mSubmission.isSelf() && mSubmission.isShowingNsfw()) {
            mNsfwWarning.setVisibility(View.VISIBLE);
            mNsfwBlocker.setVisibility(View.GONE);
        } else {
            mNsfwWarning.setVisibility(View.VISIBLE);
            if (!mSubmission.isSelf()) {
                mNsfwBlocker.setVisibility(View.VISIBLE);
            } else {
                mNsfwBlocker.setVisibility(View.GONE);
            }
        }
    }

    private void showSelfText(View expandButton, View container, ImageView imageView, ImageButton button) {
        if (mSubmission.getBodyHtml() != null) {
            mShowSelfText.setVisibility(View.VISIBLE);
            container.setVisibility(View.VISIBLE);
            if (mSubmission.isSelftextOpen()) {
                expandButton.setRotation(-180f);
                mSelfText.setVisibility(View.VISIBLE);
            } else {
                expandButton.setRotation(0f);
                mSelfText.setVisibility(View.GONE);
            }
            imageView.setVisibility(View.GONE);
            button.setVisibility(View.GONE);
            HtmlParser parser = new HtmlParser(Html.fromHtml(mSubmission.getBodyHtml()).toString());
            mSelfText.setText(parser.getSpannableString());
            mSelfText.setMovementMethod(new LinkMovementMethod());
        } else {
            mShowSelfText.setVisibility(View.GONE);
            mSelfText.setVisibility(View.GONE);
            container.setVisibility(View.GONE);
        }
    }

    private FutureCallback<Submission> mImgurCallback = new FutureCallback<Submission>() {
        @Override
        public void onCompleted(Exception e, Submission result) {
            if (e != null) {
                e.printStackTrace();
            } else if (mSubmission == result) {
                setImagePreview();
            }
        }
    };

    public void collapseOptions() {
        collapse(mOptionsRow);
    }

    public void expandOptions() {
        mOptionsRow.setVisibility(View.VISIBLE);
        expandOptions(itemView.findViewById(R.id.option_go_to_subreddit),
                itemView.findViewById(R.id.option_save),
                itemView.findViewById(R.id.option_overflow));
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

    /**
     * Attempts to set the image preview
     */
    private void setImagePreview() {
        final ImgurImage image;
        if (mSubmission.getImgurData() instanceof ImgurAlbum) {
            List<ImgurImage> images = ((ImgurAlbum) mSubmission.getImgurData()).getImages();
            if (images != null) {
                image = images.get(0);
            } else {
                image = null;
            }
        } else if (mSubmission.getImgurData() instanceof ImgurImage) {
            image = (ImgurImage) mSubmission.getImgurData();
        } else {
            image = null;
        }
        if (image != null) {
            ImgurApi.loadImage(image.getHugeThumbnail(), mImageView, null);
            mImageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onImageViewClicked(mSubmission.getImgurData());
                }
            });
        } else {
            mContainer.setVisibility(View.GONE);
        }
    }

    public static interface SubmissionCallbacks {
        public void onImageViewClicked(Object imgurData);
        public void onImageViewClicked(String imageUrl);
        public void onYouTubeVideoClicked(String videoId);
        public void onCardClicked(Submission submission);
        public void onCardLongPressed(SubmissionViewHolder holder);
        public void onOptionsRowItemSelected(View view, Submission submission);
        public void onVoted(Submission submission);
        public boolean isFrontPage();
    }
}
