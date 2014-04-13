package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Comment;
import me.williamhester.areddit.Message;
import me.williamhester.areddit.Submission;
import me.williamhester.areddit.Thing;
import me.williamhester.areddit.User;
import me.williamhester.areddit.Votable;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/13/14.
 */
public class UserFragment extends Fragment {

    private Account mAccount;
    private String mUsername;
    private User mUser;

    private Context mContext;
    private ListView mSubmittedListView;
    private List<Thing> mSubmittedThings;
    private SubmittedArrayAdapter mSubmittedAdapter;
    private TextView mCommentKarma;
    private TextView mLinkKarma;
    private TextView mCakeDay;
    private View mHeaderView;
    private int mFilterType = Message.ALL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        if (getArguments() != null) {
            mUsername = getArguments().getString("username");
            mAccount = getArguments().getParcelable("account");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_user, root, false);
        mSubmittedThings = new ArrayList<Thing>();
        mSubmittedListView = (ListView) v.findViewById(R.id.submitted);
        mSubmittedAdapter = new SubmittedArrayAdapter(mContext);
        mSubmittedListView.addHeaderView(createHeaderView(inflater));
        mSubmittedListView.setAdapter(mSubmittedAdapter);
        new LoadUserDataTask().execute();
        new SubmittedLoaderTask().execute();
        return v;
    }

    public static UserFragment newInstance(String username, Account account) {
        Bundle b = new Bundle();
        b.putString("username", username);
        b.putParcelable("account", account);
        UserFragment fragment = new UserFragment();
        fragment.setArguments(b);
        return fragment;
    }

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.header_user, null);
        TextView username = (TextView) v.findViewById(R.id.username);
        mCommentKarma = (TextView) v.findViewById(R.id.comment_karma);
        mLinkKarma = (TextView) v.findViewById(R.id.link_karma);
        mCakeDay = (TextView) v.findViewById(R.id.cakeday);

        username.setText("/u/" + mUsername);

        new LoadUserDataTask().execute();

        Spinner filter = (Spinner) v.findViewById(R.id.filter);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
                R.layout.spinner_item, R.id.orange_spinner_text,
                getResources().getStringArray(R.array.filter_types));
        filter.setAdapter(adapter);
        filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        mFilterType = Message.ALL;
                        break;
                    case 1:
                        mFilterType = Message.UNREAD;
                        break;
                    case 2:
                        mFilterType = Message.MESSAGES;
                        break;
                    case 3:
                        mFilterType = Message.COMMENT_REPLIES;
                        break;
                    case 4:
                        mFilterType = Message.POST_REPLIES;
                        break;
                    case 5:
                        mFilterType = Comment.OLD;
                        break;
                }
                mSubmittedThings.clear();
                new SubmittedLoaderTask().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        return v;
    }

    private class SubmittedArrayAdapter extends ArrayAdapter<Thing> {
        Context mContext;

        public SubmittedArrayAdapter(Context context) {
            super(context, R.layout.list_item_post, mSubmittedThings);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Thing t = getItem(position);
            String tag = null;
            if (convertView != null)
                tag = convertView.getTag() == null ? null : (String) convertView.getTag();

            if (t instanceof Comment) {
                if (tag == null || !tag.equals("t1")) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item_comment, null);
                    convertView.setTag("t1");
                }
                LinearLayout root = (LinearLayout) convertView.findViewById(R.id.root);
                TextView author = (TextView) convertView.findViewById(R.id.author);
                TextView score = (TextView) convertView.findViewById(R.id.points);
                TextView time = (TextView) convertView.findViewById(R.id.time);
                TextView body = (TextView) convertView.findViewById(R.id.comment_text);
                View voteStatus = convertView.findViewById(R.id.vote_status);
                LinearLayout replyLayout = (LinearLayout) convertView.findViewById(R.id.edited_text);
//                final EditText replyBody = (EditText) convertView.findViewById(R.id.reply_body);
//                final Button confirm = (Button) convertView.findViewById(R.id.confirm_reply);
//                final Button cancel = (Button) convertView.findViewById(R.id.cancel_reply);

//                if (((Comment) getItem(position)).isBeingEdited()) {
//                    confirm.setEnabled(true);
//                    cancel.setEnabled(true);
//                    replyBody.setEnabled(true);
//                    body.setVisibility(View.GONE);
//                    replyLayout.setVisibility(View.VISIBLE);
//                    confirm.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            if (position == 1)
//                                new ReplyAsyncTask(replyBody.getText().toString(),
//                                        mSubmission.getName()).execute();
//                            else if (position > 1)
//                                new ReplyAsyncTask(replyBody.getText().toString(),
//                                        getItem(position - 1).getName()).execute();
//                            confirm.setEnabled(false);
//                            cancel.setEnabled(false);
//                            replyBody.setEnabled(false);
//                        }
//                    });
//                    cancel.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View view) {
//                            mCommentsList.remove(position);
//                            mCommentAdapter.notifyDataSetChanged();
//                        }
//                    });
//                } else {
                    replyLayout.setVisibility(View.GONE);
                    body.setVisibility(View.VISIBLE);
//                }

                author.setText(Utilities.removeEndQuotes(((Comment) getItem(position))
                        .getAuthor()));
                score.setText(((Comment)getItem(position)).getScore() + " points by ");
                time.setText(" " + Utilities.calculateTimeShort(((Comment)getItem(position))
                        .getCreatedUtc()));
                body.setText(Html.fromHtml(StringEscapeUtils
                        .unescapeHtml4(((Comment)getItem(position)).getBodyHtml())));
//                body.setMovementMethod(new CommentLinkMovementMethod(position + HEADER_VIEW_COUNT));

                switch (((Comment)getItem(position)).getVoteStatus()) {
                    case Votable.DOWNVOTED:
                        voteStatus.setVisibility(View.VISIBLE);
                        voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                        break;
                    case Votable.UPVOTED:
                        voteStatus.setVisibility(View.VISIBLE);
                        voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                        break;
                    default:
                        voteStatus.setVisibility(View.GONE);
                        break;
                }
                if (((Comment)getItem(position)).isHidden())
                    body.setVisibility(View.GONE);
                else
                    body.setVisibility(View.VISIBLE);

            } else if (t instanceof Submission) {
                if (tag == null || !tag.equals("t3")) {
                    LayoutInflater inflater = (LayoutInflater) mContext
                            .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                    convertView = inflater.inflate(R.layout.list_item_post, null);
                    convertView.setTag("t3");
                }
                Submission s = (Submission) getItem(position);

                convertView.invalidate();

                final View voteStatus = convertView.findViewById(R.id.vote_status);
                TextView nameAndTime
                        = (TextView) convertView.findViewById(R.id.subreddit_name_and_time);
                TextView author = (TextView) convertView.findViewById(R.id.author);
                ImageView thumbnail = (ImageView) convertView.findViewById(R.id.thumbnail);
                TextView title = (TextView) convertView.findViewById(R.id.title);
                TextView domain = (TextView) convertView.findViewById(R.id.domain);
                final TextView points = (TextView) convertView.findViewById(R.id.points);
                View spacer = convertView.findViewById(R.id.spacer);

                // if the submission is a self post, we need to hide the thumbnail
                if (s.isSelf()) {
                    thumbnail.setVisibility(View.GONE);
                    spacer.setVisibility(View.GONE);
                } else {
                    thumbnail.setVisibility(View.VISIBLE);
                    spacer.setVisibility(View.VISIBLE);
                    UrlImageViewHelper.setUrlDrawable(thumbnail, s.getThumbnailUrl());
                }

                nameAndTime.setText(" in " + s.getSubredditName() + " "
                        + Utilities.calculateTimeShort(s.getCreatedUtc()));
                switch (s.getVoteStatus()) {
                    case Votable.DOWNVOTED:
                        voteStatus.setVisibility(View.VISIBLE);
                        voteStatus.setBackgroundColor(getResources().getColor(R.color.periwinkle));
                        break;
                    case Votable.UPVOTED:
                        voteStatus.setVisibility(View.VISIBLE);
                        voteStatus.setBackgroundColor(getResources().getColor(R.color.orangered));
                        break;
                    default:
                        voteStatus.setVisibility(View.GONE);
                        break;
                }

                title.setText(StringEscapeUtils.unescapeHtml4(s.getTitle()));
                author.setText(s.getAuthor());
                domain.setText("(" + s.getDomain() + ")");
                points.setText(s.getScore() + " points by ");

                if (mAccount != null && mAccount.hasVisited(getItem(position).getName())) {
                    title.setTypeface(title.getTypeface(), Typeface.ITALIC);
                } else {
                    title.setTypeface(Typeface.create("sans-serif-light", Typeface.NORMAL));
                }
            } else {
                Log.i("UserFragment", "Something bad happened");
            }
            return convertView;
        }
    }


    private class LoadUserDataTask extends AsyncTask<Void, Void, User> {
        @Override
        protected User doInBackground(Void... voids) {
            try {
                return User.getUser(mUsername, mAccount);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (User.UserNotFoundException e) {
                // Tell the user that the user could not be found.
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(User result) {
            if (result != null) {
                DecimalFormat format = new DecimalFormat("###,###,###,##0");
                mLinkKarma.setText(format.format(result.getLinkKarma()) + " Link karma");
                mCommentKarma.setText(format.format(result.getCommentKarma()) + " Comment karma");

            }
            mSubmittedAdapter.notifyDataSetChanged();
        }

        private String formatDate(long epochTime) {
            return null;
        }
    }

    private class SubmittedLoaderTask extends AsyncTask<Void, Void, List<Thing>> {

        @Override
        protected List<Thing> doInBackground(Void... params) {
            try {
                List<Thing> things = new ArrayList<Thing>();
                String dataString = Utilities.get("", "http://www.reddit.com/user/" + mUsername
                        + "/.json", mAccount);
                JsonParser jsonParser = new JsonParser();
                JsonElement element = jsonParser.parse(dataString);
                if (!element.isJsonNull()) {
                    JsonArray children = element.getAsJsonObject()
                            .getAsJsonObject("data").getAsJsonArray("children");
                    for (JsonElement e : children) {
                        JsonObject data = e.getAsJsonObject();
                        if (data.get("kind").getAsString().contains("t1")) {
                            things.add(Comment.fromJsonString(data));
                        } else if (data.get("kind").getAsString().contains("t3")) {
                            things.add(Submission.fromJsonString(data));
                        }
                    }
                } else {
                    return null;
                }
                return things;
            } catch (MalformedURLException e) {
                e.printStackTrace();
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<Thing> result) {
            if (result != null) {
                for (Thing thing : result) {
                    if (thing != null) {
                        mSubmittedThings.add(thing);
                    }
                }
                mSubmittedAdapter.notifyDataSetChanged();
            }
        }
    }

}
