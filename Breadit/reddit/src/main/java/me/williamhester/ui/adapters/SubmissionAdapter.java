package me.williamhester.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import java.util.List;

import me.williamhester.models.Submission;
import me.williamhester.reddit.R;
import me.williamhester.ui.views.SubmissionViewHolder;

/**
 * Created by William on 6/27/14.
 */
public class SubmissionAdapter extends RecyclerView.Adapter<SubmissionViewHolder> {

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
        return new SubmissionViewHolder(
                inflater.inflate(R.layout.list_item_submission, parent, false),
                mCallback);
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
