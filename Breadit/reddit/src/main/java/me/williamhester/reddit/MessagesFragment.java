package me.williamhester.reddit;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.commons.lang3.StringEscapeUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.areddit.Account;
import me.williamhester.areddit.Comment;
import me.williamhester.areddit.Message;
import me.williamhester.areddit.Votable;
import me.williamhester.areddit.utils.Utilities;

/**
 * Created by William on 4/12/14.
 */
public class MessagesFragment extends Fragment {

    private Account mAccount;
    private Context mContext;

    private ListView mMessagesListView;
    private List<Message> mMessageList;
    private MessageArrayAdapter mMessageAdapter;
    private TextView mCommentKarma;
    private TextView mLinkKarma;
    private TextView mCakeDay;
    private View mHeaderView;
    private int mFilterType = Message.ALL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessageList = new ArrayList<Message>();
        mContext = getActivity();
        if (getArguments() != null) {
            mAccount = getArguments().getParcelable("account");
            mFilterType = getArguments().getInt("filter_by", Message.ALL);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, null);
        mMessagesListView = (ListView) v.findViewById(R.id.messages);
        mMessageAdapter = new MessageArrayAdapter(mContext);
        mHeaderView = createHeaderView(inflater);
        mMessagesListView.addHeaderView(mHeaderView);
        mMessagesListView.setAdapter(mMessageAdapter);
        new LoadMessagesTask().execute();
        return v;
    }

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.header_messages, null);
        TextView username = (TextView) v.findViewById(R.id.username);
        mCommentKarma = (TextView) v.findViewById(R.id.comment_karma);
        mLinkKarma = (TextView) v.findViewById(R.id.link_karma);
        mCakeDay = (TextView) v.findViewById(R.id.cakeday);

        username.setText(mAccount.getUsername());

        new LoadAccountDataTask().execute();

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
                        mFilterType = Message.SENT;
                        break;
                    case 6:
                        mFilterType = Message.MOD_MAIL;
                        break;
                }
                mMessageList.clear();
                new LoadMessagesTask().execute();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        return v;
    }

    private class MessageArrayAdapter extends ArrayAdapter<Message> {
        Context mContext;

        public MessageArrayAdapter(Context context) {
            super(context, R.layout.list_item_post, mMessageList);
            mContext = context;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            Message m = getItem(position);

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater)
                        mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.list_item_message, parent, false);
            } else {
                convertView.invalidate();
            }

            TextView subject = (TextView) convertView.findViewById(R.id.subject);
            TextView toFrom = (TextView) convertView.findViewById(R.id.to_from);
            TextView author = (TextView) convertView.findViewById(R.id.author);
            TextView metadata = (TextView) convertView.findViewById(R.id.metadata);
            TextView body = (TextView) convertView.findViewById(R.id.body);

            LinearLayout editedText = (LinearLayout) convertView.findViewById(R.id.edited_text);
//            EditText replyBody = (EditText) convertView.findViewById(R.id.reply_body);
//            Button cancelReply = (Button) convertView.findViewById(R.id.cancel_reply);
//            Button confirmReply = (Button) convertView.findViewById(R.id.confirm_reply);

            final View voteStatus = convertView.findViewById(R.id.vote_status);
            switch (m.getVoteStatus()) {
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

            if (m.getAuthor().equals(mAccount.getUsername())) {
                toFrom.setText(getResources().getString(R.string.to) + " ");
            } else {
                toFrom.setText(getResources().getString(R.string.from) + " ");
            }

            author.setText(m.getAuthor());
            metadata.setText(" " + calculateTimeShort(m.getCreatedUtc()));
            String unescaped = StringEscapeUtils.unescapeHtml4(m.getBodyHtml());
            String formatted = unescaped.substring(31, unescaped.length() - 20);
            body.setText(Html.fromHtml(formatted));

            // Todo: allow replies, but for now, this will be a stub.
            editedText.setVisibility(View.GONE);

            subject.setText(m.getSubject());

            return convertView;
        }
    }

    private String calculateTimeShort(long postTime) {
        long currentTime = System.currentTimeMillis() / 1000;
        long difference = currentTime - postTime;
        String time;
        if (difference / 31536000 > 0) {
            time = difference / 3156000 + "y";
        } else if (difference / 2592000 > 0) {
            time = difference / 2592000 + "m";
        } else if (difference / 604800 > 0) {
            time = difference / 604800 + "w";
        } else if (difference / 86400 > 0) {
            time = difference / 86400 + "d";
        } else if (difference / 3600 > 0) {
            time = difference / 3600 + "h";
        } else if (difference / 60 > 0) {
            time = difference / 60 + "m";
        } else {
            time = difference + "s";
        }
        return time;
    }

    private class LoadMessagesTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            try {
                List<Message> messages = Message.getMessages(mFilterType, null, null, mAccount);
                for (Message m : messages) {
                    mMessageList.add(m);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mMessageAdapter.notifyDataSetChanged();
        }
    }

    private class LoadAccountDataTask extends AsyncTask<Void, Void, JsonObject> {
        @Override
        protected JsonObject doInBackground(Void... voids) {
            try {
                String s = Utilities.get("", "http://www.reddit.com/api/me.json",
                        mAccount.getCookie(), mAccount.getModhash());
                JsonObject jsonObject = new JsonParser().parse(s).getAsJsonObject();
                return jsonObject;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(JsonObject result) {
            if (result != null && !result.isJsonNull()) {
                DecimalFormat format = new DecimalFormat("###,###,###,##0");
                mLinkKarma.setText(format.format(result.get("data").getAsJsonObject()
                        .get("link_karma").getAsLong()) + " Link karma");
                mCommentKarma.setText(format.format(result.get("data").getAsJsonObject()
                        .get("comment_karma").getAsLong()) + " Comment karma");

            }
            mMessageAdapter.notifyDataSetChanged();
        }

        private String formatDate(long epochTime) {
            return null;
        }
    }
}
