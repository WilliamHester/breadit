package me.williamhester.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.network.ImgurApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.UrlParser;

/**
 * Created by william on 6/27/14.
 */
public class SubmissionsRecyclerAdapter extends RecyclerView.Adapter<SubmissionsRecyclerAdapter.ViewHolder> {

    private List<Submission> mSubmissions;
    private AdapterCallbacks mCallback;

    public SubmissionsRecyclerAdapter(List<Submission> submissions, AdapterCallbacks callbacks) {
        mSubmissions = submissions;
        mCallback = callbacks;
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
        
        private void setContent(final Submission s) {
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

            TextView subreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
            subreddit.setText("/r/" + s.getSubredditName());

            View container = itemView.findViewById(R.id.content_preview);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);

            final UrlParser linkDetails = new UrlParser(s.getUrl());
            if (linkDetails.getType() != UrlParser.NOT_SPECIAL) {
                container.setVisibility(View.VISIBLE);
                String id = linkDetails.getLinkId();
                if (linkDetails.getType() == UrlParser.IMGUR_IMAGE) {
                    ImgurApi.getImageDetails(id, imageView.getContext(), new ImgurImageFuture());
                } else if (linkDetails.getType() == UrlParser.IMGUR_ALBUM) {
                    ImgurApi.getAlbumDetails(id, imageView.getContext(), new ImgurAlbumFuture());
                } else if (linkDetails.getType() == UrlParser.YOUTUBE) {
                    String url = "http://img.youtube.com/vi/" + linkDetails.getLinkId() + "/maxresdefault.jpg";
                    ImgurApi.loadImage(url, imageView, null);
                    ImageButton button = (ImageButton) itemView.findViewById(R.id.preview_button);
                    button.setImageResource(android.R.drawable.ic_media_play);
                    button.setVisibility(View.VISIBLE);
                    imageView.setAlpha(0.8f);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(s.getUrl()));
                            view.getContext().startActivity(browserIntent);
                        }
                    });
                }
            } else {
                container.setVisibility(View.GONE);
            }
            itemView.invalidate();
        }

        private class ImgurAlbumFuture implements FutureCallback<ResponseImgurWrapper<ImgurAlbum>> {
            @Override
            public void onCompleted(Exception e, final ResponseImgurWrapper<ImgurAlbum> result) {
                if (result != null && result.isSuccess()) {
                    ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
                    ImgurApi.loadImage(result.getData().getImages().get(0).getUrl(), imageView, null);
                    ImageButton button = (ImageButton) itemView.findViewById(R.id.preview_button);
                    button.setVisibility(View.INVISIBLE);
                    imageView.setAlpha(1f);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallback.onImageViewClicked(result.getData());
                        }
                    });
                } else if (result != null) {
                    Log.e("SubmissionsRecyclerAdapter", "failed, status = " + result.getStatus());
                } else {
                    e.printStackTrace();
                }
            }
        }

        private class ImgurImageFuture implements FutureCallback<ResponseImgurWrapper<ImgurImage>> {
            @Override
            public void onCompleted(Exception e, final ResponseImgurWrapper<ImgurImage> result) {
                if (result != null && result.isSuccess()) {
                    ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
                    ImgurApi.loadImage(result.getData().getUrl(), imageView, null);
                    ImageButton button = (ImageButton) itemView.findViewById(R.id.preview_button);
                    button.setVisibility(View.INVISIBLE);
                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mCallback.onImageViewClicked(result.getData());
                        }
                    });
                } else if (result != null) {
                    Log.e("SubmissionsRecyclerAdapter", "failed, status = " + result.getStatus());
                } else {
                    e.printStackTrace();
                }
            }
        }
    }

    public interface AdapterCallbacks {
        public void onImageViewClicked(ImgurImage image);
        public void onImageViewClicked(ImgurAlbum album);
    }
}
