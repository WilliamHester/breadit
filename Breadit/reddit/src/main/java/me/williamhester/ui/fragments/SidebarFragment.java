package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.koushikdutta.async.future.FutureCallback;

import java.text.DecimalFormat;

import me.williamhester.models.AccountManager;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;

/**
 * Created by william on 11/13/14.
 */
public class SidebarFragment extends Fragment {

    private String mSubredditName;
    private Subreddit mSubreddit;

    public static SidebarFragment newInstance(String subredditName) {
        Bundle b = new Bundle();
        b.putString("subredditName", subredditName);
        SidebarFragment fragment = new SidebarFragment();
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSubredditName = getArguments().getString("subredditName");

        if (savedInstanceState != null) {
            mSubreddit = savedInstanceState.getParcelable("subreddit");
        }

        if (mSubreddit == null) {
            RedditApi.getSubredditDetails(getActivity(), mSubredditName, new FutureCallback<Subreddit>() {
                @Override
                public void onCompleted(Exception e, Subreddit result) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }
                    mSubreddit = result;
                    setUpSideBar(getView()); // Refresh the data in the view
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.view_sidebar, container, false);

        setUpSideBar(v);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelable("subreddit", mSubreddit);
    }

    /**
     * This method is called in onCreate (and later after the Subreddit is fetched) to set up the
     * sidebar that contains the description HTML and other parts.
     *
     * @param view the currently displayed parent view of the fragment.
     */
    private void setUpSideBar(View view) {
        // Only do anything at all if the view is nonnull
        if (view != null) {
            View subscribe = view.findViewById(R.id.subscribe);
            View submit = view.findViewById(R.id.submit);
            TextView title = (TextView) view.findViewById(R.id.subreddit_title);
            TextView subscribers = (TextView) view.findViewById(R.id.subscribers);
            TextView description = (TextView) view.findViewById(R.id.description);

            title.setText("/r/" + mSubredditName);
            if (mSubreddit != null) {
                if (AccountManager.isLoggedIn()) {
                    submit.setVisibility(View.VISIBLE);
                    subscribe.setVisibility(View.VISIBLE);
                    // Set subscribe's background to be dependent upon the user's subscription
                    // status
                    // subscribe.setBackground();
                } else {
                    submit.setVisibility(View.GONE);
                    subscribe.setVisibility(View.GONE);
                }
                DecimalFormat format = new DecimalFormat("###,###,##0");
                subscribers.setText(format.format(mSubreddit.getSubscriberCount()) + " "
                        + getResources().getQuantityString(R.plurals.subscribers,
                        mSubreddit.getSubscriberCount()));
                if (!TextUtils.isEmpty(mSubreddit.getDescriptionHtml())) {
                    HtmlParser parser = new HtmlParser(
                            Html.fromHtml(mSubreddit.getDescriptionHtml())
                            .toString());
                    description.setText(parser.getSpannableString());
                    description.setMovementMethod(new LinkMovementMethod());
                }
            } else if (AccountManager.isLoggedIn()) {
                // Grey out subscribe as it's loading in
            } else {
                // just go ahead and remove the submit and subscribe buttons
                submit.setVisibility(View.GONE);
                subscribe.setVisibility(View.GONE);
            }
        }
    }
}
