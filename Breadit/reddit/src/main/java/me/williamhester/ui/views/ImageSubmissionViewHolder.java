package me.williamhester.ui.views;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.koushikdutta.async.future.FutureCallback;

import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.Submission;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;

/**
 * Created by william on 11/25/14.
 */
public class ImageSubmissionViewHolder extends SubmissionViewHolder {

    private ImageView mImageView;
    private ImageButton mImageButton;
    private View mNsfwBlocker;

    public ImageSubmissionViewHolder(View itemView, SubmissionCallbacks callbacks) {
        super(itemView, callbacks);

        mImageView = (ImageView) itemView.findViewById(R.id.image);
        mImageButton = (ImageButton) itemView.findViewById(R.id.preview_button);
        mNsfwBlocker = itemView.findViewById(R.id.nsfw_blocker);
    }

    @Override
    public void setContent(Object object) {
        super.setContent(object);

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
}
