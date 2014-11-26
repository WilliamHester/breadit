package me.williamhester.ui.views;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import me.williamhester.SettingsManager;
import me.williamhester.models.Submission;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 11/25/14.
 */
public class LinkSubmissionViewHolder extends SubmissionViewHolder {

    private ImageView mThumbnail;
    private TextView mUrl;

    public LinkSubmissionViewHolder(View itemView, final SubmissionCallbacks callbacks) {
        super(itemView, callbacks);

        mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
        mUrl = (TextView) itemView.findViewById(R.id.url);
        View linkContainer = itemView.findViewById(R.id.link);
        linkContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                callbacks.onLinkClicked(mSubmission);
            }
        });
    }

    @Override
    public void setContent(Object object) {
        super.setContent(object);

        mSubmission = (Submission) object;
        if (SettingsManager.isShowingThumbnails()
                && !TextUtils.isEmpty(mSubmission.getThumbnailUrl())) {
                ImgurApi.loadImage(mSubmission.getThumbnailUrl(), mThumbnail, null);
        } else {
            mThumbnail.setImageDrawable(mThumbnail.getResources().getDrawable(
                    R.drawable.ic_action_web_site));
        }

        mUrl.setText(mSubmission.getUrl());
    }
}
