package me.williamhester.ui.views;

import android.text.Html;
import android.text.TextUtils;
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

import me.williamhester.SettingsManager;
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
 * Created by William on 10/19/14.
 */
public class SubmissionViewHolder extends VotableViewHolder {

    private ImageButton mImageButton;
    private ImageView mImageView;
    private ImageView mThumbnail;
    private TextView mCommentData;
    private TextView mDomain;
    private TextView mSelfText;
    private TextView mSubreddit;
    private TextView mUrl;
    private View mBasicLinkView;
    private View mExpandButton;
    private View mNsfwWarning;
    private View mOptionsRow;
    private View mImagePreviewView;
    private View mNsfwBlocker;
    private View mSelfTextView;
    private View mShowSelfText;

    protected Submission mSubmission;
    protected SubmissionCallbacks mCallback;

    public SubmissionViewHolder(final View itemView, SubmissionCallbacks callbacks) {
        super(itemView);
        mCallback = callbacks;

        mDomain = (TextView) itemView.findViewById(R.id.domain);
        mCommentData = (TextView) itemView.findViewById(R.id.num_comments);
        mSubreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
        mNsfwWarning = itemView.findViewById(R.id.nsfw_warning);
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
            long time = -1L;
            @Override
            public boolean onLongClick(View v) {
                if (System.currentTimeMillis() - time > 50) { // Terrible hack to prevent this
                                                              // from being called twice
                    mCallback.onCardLongPressed(SubmissionViewHolder.this);
                }
                time = System.currentTimeMillis();
                return true;
            }
        });

        mBasicLinkView = itemView.findViewById(R.id.submission_link);
        mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        mUrl = (TextView) itemView.findViewById(R.id.url);
        mBasicLinkView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCallback.onLinkClicked(mSubmission);
            }
        });

        mImagePreviewView = itemView.findViewById(R.id.submission_image_preview);
        mImageView = (ImageView) itemView.findViewById(R.id.image);
        mImageButton = (ImageButton) itemView.findViewById(R.id.preview_button);
        mNsfwBlocker = itemView.findViewById(R.id.nsfw_blocker);

        mSelfTextView = itemView.findViewById(R.id.submission_self_text);
        mSelfText = (TextView) itemView.findViewById(R.id.self_text);
        mShowSelfText = itemView.findViewById(R.id.show_self_text);
        mExpandButton = itemView.findViewById(R.id.expand_self_text);

        View.OnClickListener expandListener = new View.OnClickListener() {
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
        mExpandButton.setOnClickListener(expandListener);
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

        mNsfwWarning.setVisibility(mSubmission.isNsfw() ? View.VISIBLE : View.GONE);

        if (mSubmission.isSelf()) {
            if (TextUtils.isEmpty(mSubmission.getRawMarkdown())) {
                setUpBasic();
            } else {
                setUpSelfText();
            }
        } else if (SettingsManager.isLowBandwidth()) {
            setUpLink();
        } else {
            switch (mSubmission.getLinkDetails().getType()) {
                case Url.IMGUR_IMAGE:
                case Url.IMGUR_ALBUM:
                case Url.NORMAL_IMAGE:
                case Url.YOUTUBE:
                    setUpImage();
                    break;
                case Url.GFYCAT_LINK:
                case Url.GIF:
                case Url.DIRECT_GFY:
                case Url.IMGUR_GALLERY:
                case Url.SUBMISSION:
                case Url.SUBREDDIT:
                case Url.USER:
                case Url.REDDIT_LIVE:
                    setUpLink();
                    break;
                default:
                    setUpLink();
                    break;
            }
        }
    }

    private void setUpBasic() {
        mBasicLinkView.setVisibility(View.GONE);
        mImagePreviewView.setVisibility(View.GONE);
        mSelfTextView.setVisibility(View.GONE);
    }

    private void setUpLink() {
        mBasicLinkView.setVisibility(View.VISIBLE);
        mImagePreviewView.setVisibility(View.GONE);
        mSelfTextView.setVisibility(View.GONE);

        if (SettingsManager.isShowingThumbnails()
                && !TextUtils.isEmpty(mSubmission.getThumbnailUrl())) {
            ImgurApi.loadImage(mSubmission.getThumbnailUrl(), mThumbnail, null);
        } else {
            mThumbnail.setImageDrawable(mThumbnail.getResources().getDrawable(
                    R.drawable.ic_action_web_site));
        }

        mUrl.setText(mSubmission.getUrl());
    }

    private void setUpSelfText() {
        mBasicLinkView.setVisibility(View.GONE);
        mImagePreviewView.setVisibility(View.GONE);
        mSelfTextView.setVisibility(View.VISIBLE);

        if (mSubmission.getBodyHtml() != null) {
            mShowSelfText.setVisibility(View.VISIBLE);
//            mContentPreview.setVisibility(View.VISIBLE);
            if (mSubmission.isSelftextOpen()) {
                mExpandButton.setRotation(-180f);
                mSelfText.setVisibility(View.VISIBLE);
            } else {
                mExpandButton.setRotation(0f);
                mSelfText.setVisibility(View.GONE);
            }
            HtmlParser parser = new HtmlParser(Html.fromHtml(mSubmission.getBodyHtml()).toString());
            mSelfText.setText(parser.getSpannableString());
            mSelfText.setMovementMethod(new LinkMovementMethod());
        } else {
//            mContentPreview.setVisibility(View.GONE);
        }
    }

    private void setUpImage() {
        mBasicLinkView.setVisibility(View.GONE);
        mImagePreviewView.setVisibility(View.VISIBLE);
        mSelfTextView.setVisibility(View.GONE);

        final Url linkDetails = mSubmission.getLinkDetails();
        String id = linkDetails.getLinkId();
//        if (mSubmission.isNsfw()) {
//            mNsfwBlocker.setVisibility(View.VISIBLE);
//            return;
//        }
//        mNsfwBlocker.setVisibility(View.GONE);
        switch (linkDetails.getType()) {
            case Url.IMGUR_IMAGE: {
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
//                mContainer.setVisibility(View.GONE);
            }
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
//            mContainer.setVisibility(View.GONE);
        }
    }

    public void disableClicks() {
        View subData = itemView.findViewById(R.id.submission_data);
        subData.setOnClickListener(null);
        subData.setOnLongClickListener(null);
        subData.setClickable(false);
        subData.setBackground(null);
    }

    public void collapseOptions() {
        collapse(mOptionsRow);
    }

    public void expandOptions() {
        expand(mOptionsRow);
        expandOptions(itemView.findViewById(R.id.option_go_to_subreddit),
                itemView.findViewById(R.id.option_save),
                itemView.findViewById(R.id.option_overflow));
        itemView.findViewById(R.id.option_edit).setVisibility(AccountManager.isLoggedIn()
                && AccountManager.getAccount().getUsername()
                .equalsIgnoreCase(mSubmission.getAuthor()) ? View.VISIBLE : View.GONE);
    }

    public void expandOptionsForComments() {
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
