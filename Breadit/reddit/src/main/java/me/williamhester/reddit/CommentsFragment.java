package me.williamhester.reddit;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Comment;

/**
 * Created by William on 2/11/14.
 */
public class CommentsFragment extends Fragment {

    private boolean mIsSelf;

    private List<Comment> mCommentsList;
    private LinearLayout mLinearLayout;
    private String mUrl;
    private String mPermalink;
//    private User mUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCommentsList = new ArrayList<Comment>();
        Bundle args = getArguments();
        if (args != null) {
            mUrl = args.getString("url", null);
            mPermalink = args.getString("permalink", null);
            mIsSelf = args.getBoolean("isSelf", false);
//            mUser = args.getParcelable("user");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle bundle) {
        View v = inflater.inflate(R.layout.fragment_comments, null);
        mLinearLayout = (LinearLayout) v.findViewById(R.id.linear_container);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        new CommentLoaderTask().execute();
    }

    private class CommentLoaderTask extends AsyncTask<Void, Void, List<Comment>> {

        @Override
        protected List<Comment> doInBackground(Void... params) {
            try {
                String lastComment = null;
                if (mCommentsList.size() > 0)
                    lastComment = mCommentsList.get(mCommentsList.size() - 1).getName();
                List<Comment> comments = Comment.getComments(mPermalink, null, lastComment);
                return comments;
            } catch (MalformedURLException e) {
                Log.e("me.williamhester.reddit", e.toString());
                return null;
            } catch (IOException e) {
                Log.e("me.williamhester.reddit", e.toString());
                return null;
            } catch (org.json.simple.parser.ParseException e) {
                Log.e("me.williamhester.reddit", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Comment> result) {
            if (result != null) {
                for (Comment comment : result) {
                    LinearLayout.LayoutParams params =
                            new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                    mLinearLayout.addView(new CommentView(getActivity(), comment), params);
//                    Log.d("BreaditDebug", comment.getBody());
                    mCommentsList.add(comment);
                }
                Toast.makeText(getActivity(), "mLinearLayout child count " + mLinearLayout.getChildCount(), Toast.LENGTH_LONG).show();
            }
        }
    }

}
