package me.williamhester.reddit.ui.views;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;

import java.util.List;
import java.util.Map;

import me.williamhester.reddit.SettingsManager;
import me.williamhester.reddit.models.reddit.Account;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.imgur.ImgurAlbum;
import me.williamhester.reddit.models.imgur.ImgurImage;
import me.williamhester.reddit.models.reddit.Submission;
import me.williamhester.reddit.models.reddit.Subreddit;
import me.williamhester.reddit.network.ImgurApi;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.reddit.tools.HtmlParser;
import me.williamhester.reddit.tools.Url;
import me.williamhester.reddit.ui.activities.BrowseActivity;
import me.williamhester.reddit.ui.activities.OverlayContentActivity;

/**
 * This class is intended to be used with the RecyclerView class; however, it can be used nearly
 * just as easily with ListView to provide the View Holder pattern for optimization.
 *
 * Created by William on 10/19/14.
 */
public class SubmissionViewHolder extends VotableViewHolder {

  private ImageButton mImageButton;
  private ImageView mImageView;
  private ImageView mThumbnail;
  private TextView mCommentData;
  private TextView mDomain;
  private TextView mSelfText;
  private TextView mSubreddit;
  private TextView mUrl;
  private View mBasicLinkView;
  private View mExpandButton;
  private View mNsfwWarning;
  private View mOptionsRow;
  private View mImagePreviewView;
  private View mSelfTextView;
  private View mShowSelfText;

  private OnVotedListener mOnVotedListener;
  protected Submission mSubmission;
  protected SubmissionCallbacks mCallback;

  public SubmissionViewHolder(final View itemView, SubmissionCallbacks callbacks) {
    super(itemView);
    mCallback = callbacks;

    mDomain = (TextView) itemView.findViewById(R.id.domain);
    mCommentData = (TextView) itemView.findViewById(R.id.num_comments);
    mSubreddit = (TextView) itemView.findViewById(R.id.subreddit_title);
    mNsfwWarning = itemView.findViewById(R.id.nsfw_warning);
    mOptionsRow = itemView.findViewById(R.id.options_row);
    final View submissionData = itemView.findViewById(R.id.submission_data);
    View optionReply = itemView.findViewById(R.id.option_reply);
    View optionUser = itemView.findViewById(R.id.option_view_user);
    View optionShare = itemView.findViewById(R.id.option_share);
    final View optionEdit = itemView.findViewById(R.id.option_edit);
    final View optionSubreddit = itemView.findViewById(R.id.option_go_to_subreddit);
    final View optionSave = itemView.findViewById(R.id.option_save);
    final View optionOverflow = itemView.findViewById(R.id.option_overflow);

    View.OnClickListener mOptionsOnClickListener = new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (mCallback.onOptionsRowItemSelected(v.getId(), mSubmission)) {
          return;
        }
        onOptionsRowItemSelected(v, mSubmission);
      }
    };
    optionShare.setOnClickListener(mOptionsOnClickListener);
    optionReply.setOnClickListener(mOptionsOnClickListener);
    optionEdit.setOnClickListener(mOptionsOnClickListener);
    optionSubreddit.setOnClickListener(mOptionsOnClickListener);
    optionUser.setOnClickListener(mOptionsOnClickListener);
    optionSave.setOnClickListener(mOptionsOnClickListener);
    optionOverflow.setOnClickListener(mOptionsOnClickListener);
    submissionData.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        v.onTouchEvent(event);
        return false;
      }
    });
    submissionData.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        mCallback.onCardClicked(mSubmission);
      }
    });
    submissionData.setOnLongClickListener(new View.OnLongClickListener() {
      long time = -1L;

      @Override
      public boolean onLongClick(View v) {
        if (System.currentTimeMillis() - time > 50) { // Terrible hack to prevent this
          // from being called twice
          mCallback.onCardLongPressed(SubmissionViewHolder.this);
        }
        time = System.currentTimeMillis();
        return true;
      }
    });

    mBasicLinkView = itemView.findViewById(R.id.submission_link);
    mThumbnail = (ImageView) itemView.findViewById(R.id.thumbnail);
    mUrl = (TextView) itemView.findViewById(R.id.url);
    mBasicLinkView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        onLinkClicked();
      }
    });

    mImagePreviewView = itemView.findViewById(R.id.submission_image_preview);
    mImageView = (ImageView) itemView.findViewById(R.id.image);
    mImageButton = (ImageButton) itemView.findViewById(R.id.preview_button);

    mSelfTextView = itemView.findViewById(R.id.submission_self_text);
    mSelfText = (TextView) itemView.findViewById(R.id.self_text);
    mShowSelfText = itemView.findViewById(R.id.show_self_text);
    mExpandButton = itemView.findViewById(R.id.expand_self_text);

    View.OnClickListener expandListener = new View.OnClickListener() {
      @Override
      public void onClick(final View view) {
        ObjectAnimator objectAnimator;
        if (mSubmission.isSelftextOpen()) {
          objectAnimator =
              ObjectAnimator.ofFloat(view, "rotation", view.getRotation(), 0F);
          collapse(mSelfText);
        } else {
          objectAnimator =
              ObjectAnimator.ofFloat(view, "rotation", view.getRotation(), 180F);
          expand(mSelfText);
        }
        objectAnimator.setDuration(300);
        objectAnimator.start();
        mSubmission.setSelftextOpen(!mSubmission.isSelftextOpen());
      }
    };
    mExpandButton.setOnClickListener(expandListener);
  }

  @Override
  public void setContent(Object object) {
    super.setContent(object);
    mSubmission = (Submission) object;

    mOptionsRow.setVisibility(View.GONE);
    mBody.setText(Html.fromHtml(mSubmission.getTitle()).toString());
    mDomain.setText(mSubmission.getDomain());
    mCommentData.setText(mSubmission.getNumberOfComments() + " "
        + itemView.getResources().getQuantityString(R.plurals.comments,
        mSubmission.getNumberOfComments()));
    mSubreddit.setText(mSubmission.getSubreddit().toLowerCase());
    mMetadata.setText(mSubmission.getAuthor() + " " + mSubmission.getScore() + " "
        + itemView.getResources().getQuantityString(R.plurals.points,
        mSubmission.getScore()));

    setUpNsfw();

    if (mSubmission.isSelf()) {
      if (TextUtils.isEmpty(mSubmission.getBodyMarkdown())) {
        setUpBasic();
      } else {
        setUpSelfText();
      }
    } else if (SettingsManager.isLowBandwidth()) {
      setUpLink();
    } else {
      if (mSubmission.getLinkDetails() == null) {
        mSubmission.setLinkDetails(new Url(mSubmission.getUrl()));
      }
      switch (mSubmission.getLinkDetails().getType()) {
        case Url.IMGUR_IMAGE:
        case Url.IMGUR_ALBUM:
        case Url.NORMAL_IMAGE:
        case Url.YOUTUBE:
          setUpImage();
          break;
        case Url.GFYCAT_LINK:
        case Url.GIF:
        case Url.DIRECT_GFY:
        case Url.IMGUR_GALLERY:
        case Url.SUBMISSION:
        case Url.SUBREDDIT:
        case Url.USER:
        case Url.REDDIT_LIVE:
          setUpLink();
          break;
        default:
          setUpLink();
          break;
      }
    }
  }

  public void setUpNsfw() {
    mNsfwWarning.setVisibility(mSubmission.isNsfw() ? View.VISIBLE : View.GONE);
  }

  @Override
  protected void onVoted() {
    if (mOnVotedListener != null) {
      mOnVotedListener.onVoted(mSubmission);
    }
    mMetadata.setText(mSubmission.getAuthor() + " " + mSubmission.getScore() + " "
        + itemView.getResources().getQuantityString(R.plurals.points,
        mSubmission.getScore()));
  }

  public void setOnVotedListener(OnVotedListener listener) {
    mOnVotedListener = listener;
  }

  private void setUpBasic() {
    mBasicLinkView.setVisibility(View.GONE);
    mImagePreviewView.setVisibility(View.GONE);
    mSelfTextView.setVisibility(View.GONE);
  }

  private void setUpLink() {
    mBasicLinkView.setVisibility(View.VISIBLE);
    mImagePreviewView.setVisibility(View.GONE);
    mSelfTextView.setVisibility(View.GONE);

    if ((!SettingsManager.isLowBandwidth() || SettingsManager.isShowingThumbnails())
        && !TextUtils.isEmpty(mSubmission.getThumbnailUrl())) {
      Ion.with(mThumbnail)
          .animateIn(android.R.anim.fade_in)
          .placeholder(R.drawable.ic_action_web_site)
          .load(mSubmission.getThumbnailUrl());
    } else {
      mThumbnail.setImageDrawable(mThumbnail.getResources().getDrawable(
          R.drawable.ic_action_web_site));
    }

    mUrl.setText(mSubmission.getUrl());
  }

  private void setUpSelfText() {
    mBasicLinkView.setVisibility(View.GONE);
    mImagePreviewView.setVisibility(View.GONE);
    mSelfTextView.setVisibility(View.VISIBLE);

    if (mSubmission.getBodyHtml() != null) {
      mShowSelfText.setVisibility(View.VISIBLE);
      if (mSubmission.isSelftextOpen()) {
        mExpandButton.setRotation(180f);
        mSelfText.setVisibility(View.VISIBLE);
      } else {
        mExpandButton.setRotation(0f);
        mSelfText.setVisibility(View.GONE);
      }
      HtmlParser parser = new HtmlParser(Html.fromHtml(mSubmission.getBodyHtml()).toString());
      mSelfText.setText(parser.getSpannableString());
      mSelfText.setMovementMethod(new LinkMovementMethod());
    }
  }

  private void setUpImage() {
    mBasicLinkView.setVisibility(View.GONE);
    mImagePreviewView.setVisibility(View.VISIBLE);
    mSelfTextView.setVisibility(View.GONE);

    final Url linkDetails = mSubmission.getLinkDetails();
    String id = linkDetails.getLinkId();
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
            onLinkClicked();
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
            onImageViewClicked();
          }
        });
        break;
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
          onImageViewClicked();
        }
      });
    }
  }

  @SuppressLint("InlinedApi")
  @SuppressWarnings("unchecked")
  private void onImageViewClicked() {
    Bundle args = new Bundle();
    args.putInt("type", OverlayContentActivity.TYPE_SUBMISSION);
    args.putParcelable("submission", mSubmission);
    Intent i = new Intent(mCallback.getActivity(), OverlayContentActivity.class);
    i.putExtras(args);
    Bundle anim;
    anim = ActivityOptions.makeCustomAnimation(mCallback.getActivity(), R.anim.fade_in,
        R.anim.fade_out).toBundle();
    mCallback.getActivity().startActivity(i, anim);
  }

  private void onLinkClicked() {
    Url link = mSubmission.getLinkDetails();
    Bundle args = new Bundle();
    args.putString("permalink", link.getUrl());
    Intent i;
    switch (link.getType()) {
      case Url.SUBMISSION:
        args.putString("type", "comments");
        i = new Intent(mCallback.getActivity(), BrowseActivity.class);
        break;
      case Url.SUBREDDIT:
        args.putString("type", "subreddit");
        i = new Intent(mCallback.getActivity(), BrowseActivity.class);
        i.setAction(Intent.ACTION_VIEW);
        args.putString("subreddit", link.getLinkId());
        break;
      case Url.USER:
        args.putString("type", "user");
        i = new Intent(mCallback.getActivity(), BrowseActivity.class);
        break;
      case Url.IMGUR_GALLERY: // For now, we're going to go to a WebView because weird things happen with galleries
      case Url.IMGUR_ALBUM:
      case Url.IMGUR_IMAGE:
        // TODO: add content transition using VersionUtils.isAtLeastL()
      default:
        args.putInt("type", OverlayContentActivity.TYPE_SUBMISSION);
        args.putParcelable("submission", mSubmission);
        i = new Intent(mCallback.getActivity(), OverlayContentActivity.class);
        break;
    }
    Bundle anim = ActivityOptions.makeCustomAnimation(mCallback.getActivity(), R.anim.fade_in,
        R.anim.fade_out).toBundle();
    i.putExtras(args);
    mCallback.getActivity().startActivity(i, anim);
  }

  private void onOptionsRowItemSelected(final View view, final Submission submission) {
    switch (view.getId()) {
      case R.id.option_go_to_subreddit: {
        Bundle b = new Bundle();
        b.putString("type", "subreddit");
        b.putString("subreddit", submission.getSubreddit());
        Intent i = new Intent(mCallback.getActivity(), BrowseActivity.class);
        i.putExtras(b);
        mCallback.getActivity().startActivity(i);
        break;
      }
      case R.id.option_view_user: {
        Bundle b = new Bundle();
        b.putString("type", "user");
        b.putString("username", submission.getAuthor());
        Intent i = new Intent(mCallback.getActivity(), BrowseActivity.class);
        i.putExtras(b);
        mCallback.getActivity().startActivity(i);
        break;
      }
      case R.id.option_share: {
        PopupMenu menu = new PopupMenu(mCallback.getActivity(), view);
        menu.inflate(R.menu.share);
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
          @Override
          public boolean onMenuItemClick(MenuItem item) {
            Intent sendIntent = new Intent();
            sendIntent.setAction(Intent.ACTION_SEND);
            if (item.getItemId() == R.id.share_link) {
              sendIntent.putExtra(Intent.EXTRA_TEXT, submission.getUrl());
            } else {
              String link = RedditApi.PUBLIC_REDDIT_URL + submission.getPermalink();
              sendIntent.putExtra(Intent.EXTRA_TEXT, link);
            }
            sendIntent.setType("text/plain");
            mCallback.getActivity().startActivity(Intent.createChooser(sendIntent,
                view.getResources().getText(R.string.share_with)));
            return false;
          }
        });
        menu.show();
        break;
      }
      case R.id.option_save: {
        // TODO: actually save it
        break;
      }
      case R.id.option_overflow: {
        inflateOverflowPopupMenu(view, submission);
        break;
      }
    }
  }

  private void inflateOverflowPopupMenu(View view, final Submission submission) {
    PopupMenu popupMenu = new PopupMenu(mCallback.getActivity(), view);

    Account account = AccountManager.getAccount();
    Map<String, Subreddit> subscriptions = account.getSubscriptions();
    boolean isMod = subscriptions.containsKey(submission.getSubreddit().toLowerCase())
        && subscriptions.get(submission.getSubreddit().toLowerCase()).userIsModerator();
    boolean isOp = submission.getAuthor().equalsIgnoreCase(account.getUsername());

    popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (mCallback.onOptionsRowItemSelected(item.getItemId(), mSubmission)) {
          return true;
        }
        switch (item.getItemId()) {
          case R.id.overflow_mark_nsfw: {
            FutureCallback<String> callback = new FutureCallback<String>() {
              @Override
              public void onCompleted(Exception e, String result) {
                if (e != null) {
                  e.printStackTrace();
                }
                submission.setIsNsfw(!submission.isNsfw());
                setUpNsfw();
              }
            };
            if (submission.isNsfw()) {
              RedditApi.unmarkNsfw(mCallback.getActivity(), submission, callback);
            } else {
              RedditApi.markNsfw(mCallback.getActivity(), submission, callback);
            }
            break;
          }
          case R.id.overflow_report:
            break; // TODO: Make a form for this
          case R.id.overflow_approve: {
            FutureCallback<String> callback = new FutureCallback<String>() {
              @Override
              public void onCompleted(Exception e, String result) {
                if (e != null) {
                  e.printStackTrace();
                }
              }
            };
            RedditApi.approve(mCallback.getActivity(), submission, callback);
            break;
          }
        }
        return true;
      }
    });
    popupMenu.inflate(R.menu.submission_overflow);

    Menu menu = popupMenu.getMenu();
    if ((isOp || isMod) && submission.isNsfw()) {
      menu.findItem(R.id.overflow_mark_nsfw).setTitle(R.string.unmark_nsfw);
    }
    if (submission.isHidden()) {
      menu.findItem(R.id.overflow_hide).setTitle(R.string.unhide);
    }
    menu.findItem(R.id.overflow_report).setVisible(!isMod);
    menu.findItem(R.id.overflow_mark_nsfw).setVisible(isOp || isMod);
    menu.findItem(R.id.overflow_delete).setVisible(isOp);
    menu.findItem(R.id.overflow_approve).setVisible(isMod);
    menu.findItem(R.id.overflow_remove).setVisible(isMod);
    menu.findItem(R.id.overflow_spam).setVisible(isMod);

    popupMenu.show();
  }

  public void disableClicks() {
    View subData = itemView.findViewById(R.id.submission_data);
    subData.setOnClickListener(null);
    subData.setOnLongClickListener(null);
    subData.setClickable(false);
    subData.setBackground(null);
  }

  public void collapseOptions() {
    collapse(mOptionsRow);
  }

  public void expandOptions() {
    expand(mOptionsRow);
    expandOptions(itemView.findViewById(R.id.option_go_to_subreddit),
        itemView.findViewById(R.id.option_save),
        itemView.findViewById(R.id.option_overflow));
    itemView.findViewById(R.id.option_edit).setVisibility(AccountManager.isLoggedIn()
        && AccountManager.getAccount().getUsername()
        .equalsIgnoreCase(mSubmission.getAuthor()) ? View.VISIBLE : View.GONE);
  }

  public void expandOptionsForComments() {
    mOptionsRow.setVisibility(View.VISIBLE);
    expandOptions(itemView.findViewById(R.id.option_go_to_subreddit),
        itemView.findViewById(R.id.option_save),
        itemView.findViewById(R.id.option_overflow));
    itemView.findViewById(R.id.option_reply).setVisibility(AccountManager.isLoggedIn()
        ? View.VISIBLE : View.GONE);
    itemView.findViewById(R.id.option_edit).setVisibility(AccountManager.isLoggedIn()
        && AccountManager.getAccount().getUsername()
        .equalsIgnoreCase(mSubmission.getAuthor()) ? View.VISIBLE : View.GONE);
  }

  protected void expandOptions(View optionSubreddit, View optionSave, View optionOverflow) {
    optionSubreddit.setVisibility(mCallback.isFrontPage() ? View.VISIBLE : View.GONE);
    optionOverflow.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
    optionSave.setVisibility(View.GONE); // TODO: actually allow the user to save.
//        optionSave.setVisibility(AccountManager.isLoggedIn() ? View.VISIBLE : View.GONE);
  }

  public interface OnVotedListener {
    void onVoted(Submission submission);
  }

  public interface SubmissionCallbacks {
    FragmentManager getFragmentManager();

    Activity getActivity();

    void onCardClicked(Submission submission);

    void onCardLongPressed(SubmissionViewHolder holder);

    boolean onOptionsRowItemSelected(int itemId, Submission submission);

    boolean isFrontPage();
  }
}
