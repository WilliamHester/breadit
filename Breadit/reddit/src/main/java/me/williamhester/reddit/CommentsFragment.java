package me.williamhester.reddit;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Comment;
import me.williamhester.areddit.User;

/**
 * Created by William on 2/11/14.
 */
public class CommentsFragment extends Fragment {

    private boolean mIsSelf;

    private List<Comment> mCommentsList;
    private LinearLayout mLinearLayout;
    private ScrollView mScrollView;
    private String mUrl;
    private String mPermalink;
    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommentsList = new ArrayList<Comment>();
        Bundle args = getArguments();
        if (args != null) {
            mUrl = args.getString("url", null);
            mPermalink = args.getString("permalink", null);
            mIsSelf = args.getBoolean("isSelf", false);
            mUser = args.getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_comments, null);
        mScrollView = (ScrollView) v.findViewById(R.id.scroll_view);
        mLinearLayout = (LinearLayout) v.findViewById(R.id.linear_container);
        new CommentLoaderTask().execute();

        return v;
    }

    private class CommentLoaderTask extends AsyncTask<Void, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(Void... params) {
            try {
                String lastComment = null;
                if (mCommentsList.size() > 0)
                    lastComment = mCommentsList.get(mCommentsList.size() - 1).getName();
                List<Comment> comments = Comment.getComments(mPermalink, mUser, lastComment);
                return comments;
            } catch (MalformedURLException e) {
                Log.e("me.williamhester.reddit", e.toString());
                return null;
            } catch (IOException e) {
                Log.e("me.williamhester.reddit", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> result) {
            if (result != null) {
                for (Comment comment : result) {
                    if (comment != null) {
                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                        ViewGroup.LayoutParams.WRAP_CONTENT);
                        if (mLinearLayout != null)
                            mLinearLayout.addView(new CommentView(getActivity(), comment), params);
                    }
                }
                mLinearLayout.requestLayout();
                Log.d("CommentsFragment", "mLinearLayout.height() == " + mLinearLayout.getMeasuredHeight());
                Log.d("CommentsFragment", "mLinearLayout child count " + mLinearLayout.getChildCount());
            }
        }
    }

}
