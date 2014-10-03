package me.williamhester.ui.adapters;

import android.content.Context;
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
import me.williamhester.models.Votable;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.views.VotableViewHolder;

/**
 * Created by william on 6/27/14.
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
        private View mNsfwWarning;
        private View mExpandButton;

        private Submission mSubmission;

        public SubmissionViewHolder(View itemView) {
            super(itemView);

            mDomain = (TextView) itemView.findViewById(R.id.domain);
            mCommentData = (TextView) itemView.findViewById(R.id.num_comments);
            mSubreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
            mNsfwWarning = itemView.findViewById(R.id.nsfw_warning);
            mExpandButton = itemView.findViewById(R.id.expand_self_text);
            mSelfText = (TextView) itemView.findViewById(R.id.self_text);
            View submissionData = itemView.findViewById(R.id.submission_data);

            submissionData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onCardClicked(mSubmission);
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

            if (mSubmission.isNsfw()) {
                mNsfwWarning.setVisibility(View.VISIBLE);
            } else {
                mNsfwWarning.setVisibility(View.GONE);
            }

            View container = itemView.findViewById(R.id.content_preview);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            final ImageButton button = (ImageButton) itemView.findViewById(R.id.preview_button);
            setUpNsfw(button);

            if (mSubmission.isSelf()) {
                showSelfText(mExpandButton, container, imageView, button);
            } else {
                itemView.findViewById(R.id.show_self_text).setVisibility(View.GONE);
                itemView.findViewById(R.id.self_text).setVisibility(View.GONE);
                final UrlParser linkDetails = new UrlParser(mSubmission.getUrl());
                if (linkDetails.getType() != UrlParser.NOT_SPECIAL) {
                    mSelfText.setVisibility(View.GONE);
                    container.setVisibility(View.VISIBLE);
                    String id = linkDetails.getLinkId();
                    if (linkDetails.getType() == UrlParser.IMGUR_IMAGE) {
                        if (mSubmission.getImgurData() == null) {
                            imageView.setImageDrawable(null);
                            ImgurApi.getImageDetails(id, itemView.getContext(), mSubmission, mImgurCallback);
                        } else {
                            setImagePreview();
                        }
                    } else if (linkDetails.getType() == UrlParser.IMGUR_ALBUM) {
                        if (mSubmission.getImgurData() == null) {
                            imageView.setImageDrawable(null);
                            ImgurApi.getAlbumDetails(id, itemView.getContext(), mSubmission, mImgurCallback);
                        } else {
                            setImagePreview();
                        }
                    } else if (linkDetails.getType() == UrlParser.YOUTUBE) {
                        imageView.setVisibility(View.VISIBLE);
                        ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                        button.setImageResource(R.drawable.ic_youtube);
                        button.setVisibility(View.VISIBLE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mCallback.onYouTubeVideoClicked(linkDetails.getLinkId());
                            }
                        });
                    } else if (linkDetails.getType() == UrlParser.NORMAL_IMAGE) {
                        imageView.setVisibility(View.VISIBLE);
                        ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mCallback.onImageViewClicked(mSubmission.getUrl());
                            }
                        });
                    } else {
                        container.setVisibility(View.GONE);
                    }
                } else {
                    container.setVisibility(View.GONE);
                }
            }
            itemView.invalidate();
        }

        private void setUpNsfw(final ImageButton button) {
            if (mSubmission.isNsfw()) {
                button.setVisibility(View.VISIBLE);
                button.setBackgroundColor(button.getContext().getResources()
                        .getColor(android.R.color.black));
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        button.setVisibility(View.GONE);
                    }
                });
            } else {
                button.setVisibility(View.GONE);
            }
        }

        private void showSelfText(View expandButton, View container, ImageView imageView, ImageButton button) {
            if (mSubmission.getBodyHtml() != null) {
                itemView.findViewById(R.id.show_self_text).setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
                TextView content = (TextView) itemView.findViewById(R.id.self_text);
                if (mSubmission.isSelftextOpen()) {
                    expandButton.setRotation(-180f);
                    content.setVisibility(View.VISIBLE);
                } else {
                    expandButton.setRotation(0f);
                    content.setVisibility(View.GONE);
                }
                imageView.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                HtmlParser parser = new HtmlParser(Html.fromHtml(mSubmission.getBodyHtml()).toString());
                content.setText(parser.getSpannableString());
                content.setMovementMethod(new LinkMovementMethod());
            } else {
                itemView.findViewById(R.id.show_self_text).setVisibility(View.GONE);
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

            private View mTextView = itemView.findViewById(R.id.self_text);

            @Override
            public void onClick(final View view) {
                Animation anim;
                Animation textAnim;
                if (mSubmission.isSelftextOpen()) {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_left);
                    mTextView.setVisibility(View.GONE);
                    mSubmission.setSelftextOpen(false);
                } else {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_right);
                    textAnim = new ScaleAnimation(1, 1, 0, 1);
                    textAnim.setFillAfter(true);
                    textAnim.setDuration(300l);
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.startAnimation(textAnim);
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
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            if (image != null) {
                imageView.setVisibility(View.VISIBLE);
                ImgurApi.loadImage(image.getHugeThumbnail(), imageView, null);
                ImageButton button = (ImageButton) itemView.findViewById(R.id.preview_button);
                button.setVisibility(View.INVISIBLE);
                imageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSubmission.getImgurData() instanceof ImgurImage) {
                            mCallback.onImageViewClicked((ImgurImage) mSubmission.getImgurData());
                        } else if (mSubmission.getImgurData() instanceof ImgurAlbum) {
                            mCallback.onImageViewClicked((ImgurAlbum) mSubmission.getImgurData());
                        }
                    }
                });
            } else {
                itemView.findViewById(R.id.content_preview).setVisibility(View.GONE);
            }
        }
    }

    public static interface AdapterCallbacks {
        public void onImageViewClicked(ImgurImage image);
        public void onImageViewClicked(ImgurAlbum album);
        public void onImageViewClicked(String imageUrl);
        public void onYouTubeVideoClicked(String videoId);
        public void onCardClicked(Submission submission);
    }
}
