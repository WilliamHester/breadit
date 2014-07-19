package me.williamhester.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 6/27/14.
 */
public class SubmissionsRecyclerAdapter extends RecyclerView.Adapter<SubmissionsRecyclerAdapter.ViewHolder> {

    private List<Submission> mSubmissions;

    public SubmissionsRecyclerAdapter(List<Submission> submissions) {
        mSubmissions = submissions;
    }

    @Override
    public SubmissionsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SubmissionsRecyclerAdapter.ViewHolder viewHolder, int position) {
        viewHolder.setContent(mSubmissions.get(position));
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
            }
        });
    }

    @Override
    public int getItemCount() {
        return mSubmissions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        public ViewHolder(View itemView) {
            super(itemView);
        }
        
        private void setContent(Submission s) {
            final View voteStatus = itemView.findViewById(R.id.vote_status);
            TextView author = (TextView) itemView.findViewById(R.id.author);
            TextView title = (TextView) itemView.findViewById(R.id.title);
            TextView domain = (TextView) itemView.findViewById(R.id.domain);
            final TextView points = (TextView) itemView.findViewById(R.id.points);

            switch (s.getVoteStatus()) {
                case Votable.DOWNVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(itemView.getResources().getColor(R.color.periwinkle));
                    break;
                case Votable.UPVOTED:
                    voteStatus.setVisibility(View.VISIBLE);
                    voteStatus.setBackgroundColor(itemView.getResources().getColor(R.color.orangered));
                    break;
                default:
                    voteStatus.setVisibility(View.GONE);
                    break;
            }

            title.setText(StringEscapeUtils.unescapeHtml4(s.getTitle()));
            author.setText(s.getAuthor());
            domain.setText("(" + s.getDomain() + ")");
            points.setText(s.getScore() + " points by ");

            if (s.getDomain().contains("imgur")) {
                ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
                imageView.setVisibility(View.VISIBLE);
                String id = ImgurApi.getImageIdFromUrl(s.getUrl());
                ImgurApi.getImageDetails(id, imageView.getContext(), new FutureCallback<ResponseImgurWrapper<ImgurImage>>() {
                    @Override
                    public void onCompleted(Exception e, ResponseImgurWrapper<ImgurImage> result) {
                        if (result != null && result.isSuccess()) {

                        } else {
                            Log.d("SubmissionsRecyclerAdapter", "failed, status = " + result.getStatus());
                        }
                    }
                });
            }
        }
    }
}
