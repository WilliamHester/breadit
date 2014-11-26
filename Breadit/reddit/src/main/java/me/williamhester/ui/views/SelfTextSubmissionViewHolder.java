package me.williamhester.ui.views;

import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;

/**
 * Created by william on 11/25/14.
 */
public class SelfTextSubmissionViewHolder extends SubmissionViewHolder {

    private TextView mSelfText;
    private View mContentPreview;
    private View mExpandButton;
    private View mShowSelfText;

    public SelfTextSubmissionViewHolder(View itemView, SubmissionCallbacks callbacks) {
        super(itemView, callbacks);

        mContentPreview = itemView.findViewById(R.id.content_preview);
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
    public void setContent(Object object) {
        super.setContent(object);

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
}
