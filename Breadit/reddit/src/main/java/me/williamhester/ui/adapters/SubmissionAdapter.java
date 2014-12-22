package me.williamhester.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.williamhester.SettingsManager;
import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.tools.Url;
import me.williamhester.ui.views.ImageSubmissionViewHolder;
import me.williamhester.ui.views.LinkSubmissionViewHolder;
import me.williamhester.ui.views.SelfTextSubmissionViewHolder;
import me.williamhester.ui.views.SubmissionViewHolder;

/**
 * Created by William on 6/27/14.
 */
public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder> {

    private static final int BASIC = 1;
    private static final int IMAGE = 2;
    private static final int LINK = 3;
    private static final int SELF = 4;

    private List<Submission> mSubmissions;
    private SubmissionViewHolder.SubmissionCallbacks mCallback;

    public SubmissionAdapter(SubmissionViewHolder.SubmissionCallbacks callbacks,
                             List<Submission> submissions) {
        mSubmissions = submissions;
        mCallback = callbacks;
    }

    @Override
    public SubmissionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        CardView cardView = (CardView) inflater.inflate(R.layout.view_content_card, parent, false);
        switch (viewType) {
            case BASIC: {
                inflater.inflate(R.layout.view_submission_basic, cardView, true);
                return new LinkSubmissionViewHolder(cardView, mCallback);
            }
            case IMAGE: {
                inflater.inflate(R.layout.view_submission_image, cardView, true);
                return new ImageSubmissionViewHolder(cardView, mCallback);
            }
            case LINK: {
                inflater.inflate(R.layout.view_submission_basic, cardView, true);
                return new LinkSubmissionViewHolder(cardView, mCallback);
            }
            case SELF: {
                inflater.inflate(R.layout.view_submission_self_text, cardView, true);
                return new SelfTextSubmissionViewHolder(cardView, mCallback);
            }
        }
        Log.wtf("SubmissionAdapter", "Something went horribly wrong");
        return new SubmissionViewHolder(
                inflater.inflate(R.layout.list_item_submission, parent, false),
                mCallback);
    }

    @Override
    public int getItemViewType(int position) {
        Submission s = mSubmissions.get(position);
        if (s.isSelf()) {
            if (TextUtils.isEmpty(s.getRawMarkdown())) {
                return BASIC;
            } else {
                return SELF;
            }
        }
        if (SettingsManager.isLowBandwidth()) {
            return BASIC;
        }
        switch (s.getLinkDetails().getType()) {
            case Url.IMGUR_IMAGE:
            case Url.IMGUR_ALBUM:
            case Url.NORMAL_IMAGE:
            case Url.YOUTUBE:
                return IMAGE;
            case Url.GFYCAT_LINK:
            case Url.GIF:
            case Url.DIRECT_GFY:
            case Url.IMGUR_GALLERY:
            case Url.SUBMISSION:
            case Url.SUBREDDIT:
            case Url.USER:
            case Url.REDDIT_LIVE:
                return LINK;
            default:
                return BASIC;
        }
    }

    @Override
    public void onBindViewHolder(SubmissionViewHolder submissionViewHolder, int i) {
        submissionViewHolder.setContent(mSubmissions.get(i));
    }

    @Override
    public int getItemCount() {
        return mSubmissions.size();
    }
}
