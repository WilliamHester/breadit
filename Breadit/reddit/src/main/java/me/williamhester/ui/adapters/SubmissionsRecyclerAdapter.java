package me.williamhester.ui.adapters;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.ScaleAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import org.apache.commons.lang3.StringEscapeUtils;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.Account;
import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;
import me.williamhester.models.Submission;
import me.williamhester.models.Votable;
import me.williamhester.network.ImgurApi;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.views.SwipeView;

/**
 * Created by william on 6/27/14.
 */
public class SubmissionsRecyclerAdapter extends RecyclerView.Adapter<SubmissionsRecyclerAdapter.ViewHolder> {

    private List<Submission> mSubmissions;
    private final List<String> mExpandedSubmissions = new ArrayList<>();
    private AdapterCallbacks mCallback;

    public SubmissionsRecyclerAdapter(List<Submission> submissions, AdapterCallbacks callbacks) {
        mSubmissions = submissions;
        mCallback = callbacks;
    }

    @Override
    public SubmissionsRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int position) {
        final View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_post, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(SubmissionsRecyclerAdapter.ViewHolder viewHolder, int position) {
        viewHolder.setContent(mSubmissions.get(position));
    }

    @Override
    public int getItemCount() {
        return mSubmissions.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private Submission mSubmission;

        public ViewHolder(View itemView) {
            super(itemView);
        }
        
        private void setContent(final Submission s) {
            mSubmission = s;
            final View upVoteView = itemView.findViewById(R.id.vote_background);
            final View downVoteView = itemView.findViewById(R.id.vote_foreground);
            final SwipeView swipeView = (SwipeView) itemView.findViewById(R.id.swipe_view);
            swipeView.setVoteViews(upVoteView, downVoteView);
            swipeView.setVotable(mSubmission);
            swipeView.setAccount(mCallback.getAccount());
            swipeView.setSwipeListener(new SwipeView.SwipeListener() {
                @Override
                public void onRightToLeftSwipe() {
                    if (mSubmission.getVoteStatus() == Votable.DOWNVOTED) {
                        RedditApi.vote(swipeView.getContext(), mSubmission, mCallback.getAccount(), Votable.NEUTRAL);
                    } else {
                        RedditApi.vote(swipeView.getContext(), mSubmission, mCallback.getAccount(), Votable.DOWNVOTED);
                    }
                }

                @Override
                public void onLeftToRightSwipe() {
                    if (mSubmission.getVoteStatus() == Votable.DOWNVOTED) {
                        RedditApi.vote(swipeView.getContext(), mSubmission, mCallback.getAccount(), Votable.NEUTRAL);
                    } else {
                        RedditApi.vote(swipeView.getContext(), mSubmission, mCallback.getAccount(), Votable.UPVOTED);
                    }
                }
            });
            TextView title = (TextView) itemView.findViewById(R.id.title);
            TextView domain = (TextView) itemView.findViewById(R.id.domain);
            final TextView metaData = (TextView) itemView.findViewById(R.id.metadata);
            TextView commentData = (TextView) itemView.findViewById(R.id.num_comments);
            View nsfwWarning = itemView.findViewById(R.id.nsfw_warning);
            View expandButton = itemView.findViewById(R.id.expand_self_text);
            expandButton.setOnClickListener(mExpandListener);

            if (mSubmission.isNsfw()) {
                nsfwWarning.setVisibility(View.VISIBLE);
            } else {
                nsfwWarning.setVisibility(View.GONE);
            }

            switch (s.getVoteStatus()) {
                case Votable.DOWNVOTED:
                    downVoteView.setVisibility(View.VISIBLE);
                    upVoteView.setVisibility(View.GONE);
                    break;
                case Votable.UPVOTED:
                    upVoteView.setVisibility(View.VISIBLE);
                    downVoteView.setVisibility(View.GONE);
                    break;
                default:
                    upVoteView.setVisibility(View.GONE);
                    downVoteView.setVisibility(View.GONE);
                    break;
            }

            title.setText(StringEscapeUtils.unescapeHtml4(s.getTitle()));
            domain.setText(s.getDomain());
            metaData.setText(s.getScore() + " points by " + s.getAuthor());
            commentData.setText(s.getNumberOfComments() + " comments");
            View submissionData = itemView.findViewById(R.id.submission_data);

            submissionData.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onCardClicked(mSubmission);
                }
            });

            TextView subreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
            subreddit.setText("/r/" + s.getSubredditName());

            View container = itemView.findViewById(R.id.content_preview);
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            final ImageButton button = (ImageButton) itemView.findViewById(R.id.preview_button);

            if (mSubmission.isSelf()) {
                showSelfText(expandButton, container, imageView, button);
            } else {
                itemView.findViewById(R.id.show_self_text).setVisibility(View.GONE);
                itemView.findViewById(R.id.self_text).setVisibility(View.GONE);
                final UrlParser linkDetails = new UrlParser(s.getUrl());
                if (linkDetails.getType() != UrlParser.NOT_SPECIAL) {
                    container.setVisibility(View.VISIBLE);
                    String id = linkDetails.getLinkId();
                    if (linkDetails.getType() == UrlParser.IMGUR_IMAGE) {
                        if (s.isNsfw()) {
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
                        if (!setImagePreview()) {
                            imageView.setImageDrawable(null);
                            ImgurApi.getImageDetails(id, imageView.getContext(), new ImgurImageFuture());
                        }
                    } else if (linkDetails.getType() == UrlParser.IMGUR_ALBUM) {
                        if (s.isNsfw()) {
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
                        if (!setImagePreview()) {
                            imageView.setImageDrawable(null);
                            ImgurApi.getAlbumDetails(id, imageView.getContext(), new ImgurAlbumFuture());
                        }
                    } else if (linkDetails.getType() == UrlParser.YOUTUBE) {
                        ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
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
                    } else if (linkDetails.getType() == UrlParser.NORMAL_IMAGE) {
                        ImgurApi.loadImage(linkDetails.getUrl(), imageView, null);
                        if (s.isNsfw()) {
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
                        imageView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                mCallback.onImageViewClicked(mSubmission.getUrl());
                            }
                        });
                    }
                } else {
                    container.setVisibility(View.GONE);
                }
            }
            itemView.invalidate();
        }

        private void showSelfText(View expandButton, View container, ImageView imageView, ImageButton button) {
            if (mSubmission.getBodyHtml() != null) {
                itemView.findViewById(R.id.show_self_text).setVisibility(View.VISIBLE);
                container.setVisibility(View.VISIBLE);
                TextView content = (TextView) itemView.findViewById(R.id.self_text);
                if (mExpandedSubmissions.contains(mSubmission.getName())) {
                    expandButton.setRotation(-180f);
                    content.setVisibility(View.VISIBLE);
                } else {
                    expandButton.setRotation(0f);
                    content.setVisibility(View.GONE);
                }
                imageView.setVisibility(View.GONE);
                button.setVisibility(View.GONE);
                content.setText(HtmlParser.parseHtml(StringEscapeUtils.unescapeHtml4(mSubmission.getBodyHtml())));
            } else {
                itemView.findViewById(R.id.show_self_text).setVisibility(View.GONE);
                container.setVisibility(View.GONE);
            }
        }

        private class ImgurAlbumFuture implements FutureCallback<ResponseImgurWrapper<ImgurAlbum>> {
            @Override
            public void onCompleted(Exception e, final ResponseImgurWrapper<ImgurAlbum> result) {
                if (result != null && result.isSuccess()) {
                    mSubmission.setImgurData(result.getData());
                    setImagePreview();
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
                    mSubmission.setImgurData(result.getData());
                    setImagePreview();
                } else if (result != null) {
                    Log.e("SubmissionsRecyclerAdapter", "failed, status = " + result.getStatus());
                } else {
                    e.printStackTrace();
                }
            }
        }

        private View.OnClickListener mExpandListener = new View.OnClickListener() {

            private View mTextView = itemView.findViewById(R.id.self_text);

            @Override
            public void onClick(final View view) {
                Animation anim;
                Animation textAnim;
                final boolean expanded = mExpandedSubmissions.contains(mSubmission.getName());
                if (expanded) {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_left);
                    mTextView.setVisibility(View.GONE);
                    mExpandedSubmissions.remove(mSubmission.getName());
                } else {
                    anim = AnimationUtils.loadAnimation(view.getContext(), R.anim.rotate_right);
                    textAnim = new ScaleAnimation(1, 1, 0, 1);
                    textAnim.setFillAfter(true);
                    textAnim.setDuration(300l);
                    mTextView.setVisibility(View.VISIBLE);
                    mTextView.startAnimation(textAnim);
                    mExpandedSubmissions.add(mSubmission.getName());
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
        private boolean setImagePreview() {
            if (mSubmission.getImgurData() == null) {
                return false;
            }
            ImgurImage image = null;
            if (mSubmission.getImgurData() instanceof ImgurAlbum) {
                image = ((ImgurAlbum) mSubmission.getImgurData()).getImages().get(0);
            } else if (mSubmission.getImgurData() instanceof ImgurImage) {
                image = (ImgurImage) mSubmission.getImgurData();
            }
            ImageView imageView = (ImageView) itemView.findViewById(R.id.image);
            if (image != null && !image.isAnimated()) {
                ImgurApi.loadImage(image.getUrl(), imageView, null);
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
            return true;
        }
    }

    public interface AdapterCallbacks {
        public void onImageViewClicked(ImgurImage image);
        public void onImageViewClicked(ImgurAlbum album);
        public void onImageViewClicked(String imageUrl);
        public void onCardClicked(Submission submission);
        public Account getAccount();
    }
}
