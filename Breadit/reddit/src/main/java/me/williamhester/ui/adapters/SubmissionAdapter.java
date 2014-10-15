package me.williamhester.ui.adapters;

import android.content.Context;
import android.graphics.Color;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.Url;
import me.williamhester.ui.views.VotableViewHolder;

/**
 * Created by William on 6/27/14.
 */
public class SubmissionAdapter extends ArrayAdapter<Submission> {

    private List<Submission> mSubmissions;
    private AdapterCallbacks mCallback;
    private Context mContext;

    public SubmissionAdapter(Context context, AdapterCallbacks callbacks, List<Submission> submissions) {
        super(context, R.layout.list_item_post, submissions);
        mContext = context;
        mSubmissions = submissions;
        mCallback = callbacks;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_post, parent, false);
            SubmissionViewHolder viewHolder = new SubmissionViewHolder(convertView);
            convertView.setTag(viewHolder);
        }
        SubmissionViewHolder submissionViewHolder = (SubmissionViewHolder) convertView.getTag();
        submissionViewHolder.setContent(mSubmissions.get(position));
        return convertView;
    }

    @Override
    public int getCount() {
        return mSubmissions.size();
    }

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
        private View mExpandButton;
        private View mShowSelfText;

        private Submission mSubmission;

        public SubmissionViewHolder(View itemView) {
            super(itemView);

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
            View submissionData = itemView.findViewById(R.id.submission_data);

            submissionData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onCardClicked(mSubmission);
                }
            });
            mNsfwBlocker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    view.setVisibility(View.GONE);
                    mSubmission.setShowNsfwContent();
                }
            });

            mExpandButton.setOnClickListener(mExpandListener);
        }

        @Override
        public void setContent(Object object) {
            super.setContent(object);
            mSubmission = (Submission) object;

            mBody.setText(Html.fromHtml(mSubmission.getTitle()).toString());
            mDomain.setText(mSubmission.getDomain());
            mCommentData.setText(mSubmission.getNumberOfComments() + " comments");
            mSubreddit.setText("/r/" + mSubmission.getSubredditName());
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

        private View.OnClickListener mExpandListener = new View.OnClickListener() {
            @Override
            public void onClick(final View view) {
                Animation anim;
                Animation textAnim;
                if (mSubmission.isSelftextOpen()) {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_left);
                    mSelfText.setVisibility(View.GONE);
                    mSubmission.setSelftextOpen(false);
                } else {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_right);
                    textAnim = new ScaleAnimation(1, 1, 0, 1);
                    textAnim.setFillAfter(true);
                    textAnim.setDuration(300l);
                    mSelfText.setVisibility(View.VISIBLE);
                    mSelfText.startAnimation(textAnim);
                    mSubmission.setSelftextOpen(true);
                }
                anim.setFillBefore(true);
                anim.setFillAfter(true);
                view.startAnimation(anim);
            }
        };

        /**
         * Attempts to set the image preview
         *
         * @return returns whether or not the data has been set.
         */
        private void setImagePreview() {
            final ImgurImage image;
            if (mSubmission.getImgurData() instanceof ImgurAlbum) {
                image = ((ImgurAlbum) mSubmission.getImgurData()).getImages().get(0);
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
    }

    public static interface AdapterCallbacks {
        public void onImageViewClicked(Object imgurData);
        public void onImageViewClicked(String imageUrl);
        public void onYouTubeVideoClicked(String videoId);
        public void onCardClicked(Submission submission);
    }
}
