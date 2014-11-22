package me.williamhester.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.Message;
import me.williamhester.models.Votable;
import me.williamhester.models.utils.Utilities;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.UserActivity;

public class MessagesFragment extends AccountFragment {

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
        mMessagesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                final Message m = mMessageList.get(position - 1);
                if (m.isUnread()) {
                    new MarkReadTask(m, true).execute();
                }
            }
        });
        mMessagesListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position,
                                           long l) {
                if (position > 0) {
                    final Message m = mMessageList.get(position - 1);
                    if (!m.isUnread()) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(mContext,
                                android.R.style.Theme_Holo_Dialog);
                        ArrayList<String> options = new ArrayList<String>();
                        options.add(getResources().getString(R.string.reply));
                        options.add(getResources().getString(R.string.mark_unread));
                        options.add(getResources().getString(R.string.view_profile));
                        if (m.isComment()) {
                            options.add(getResources().getString(R.string.view_context));
                            options.add(getResources().getString(R.string.report));
                            options.add(getResources().getString(R.string.full_comments));
                        } else {
                            options.add(getResources().getString(R.string.block_user));
                        }
                        String[] array = new String[options.size()];
                        options.toArray(array);
                        builder.setItems(array, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int position) {
                                switch (position) {
                                    case 0:
                                        // Reply
//                                        MessageDialogFragment mf = MessageDialogFragment
//                                                .newInstance(m.getName());
//                                        mf.show(getFragmentManager(), "reply_fragment");
                                        break;
                                    case 1:
                                        // Mark unread
                                        new MarkReadTask(m, false).execute();
                                        break;
                                    case 2:
                                        // View user's profile
                                        Bundle b = new Bundle();
                                        b.putString("username", m.getAuthor());
                                        Intent i = new Intent(mContext, UserActivity.class);
                                        i.putExtras(b);
                                        mContext.startActivity(i);
                                        break;
                                    case 3:
                                        if (m.isComment()) {
                                            // report
                                            new ReportCommentTask(m).execute();
                                        } else {
                                            // Block user
                                            new BlockUserTask(m).execute();
                                        }
                                        break;
                                    case 4:
                                        // view context
                                        break;
                                    case 5:
                                        // full comments
                                        break;
                                }
                            }
                        });
                        Dialog d = builder.create();
                        d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                        d.show();
                    }
                }
                return false;
            }
        });
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
                new LoadMessagesTask(true).execute();
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
            super(context, R.layout.list_item_submission, mMessageList);
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

            View readStatus = convertView.findViewById(R.id.read_status);

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
            metadata.setText(" " + Utilities.calculateTimeShort(m.getCreatedUtc()));
            String unescaped = Html.fromHtml(m.getBodyHtml()).toString();
            String formatted = unescaped.substring(31, unescaped.length() - 20);
            body.setText(Html.fromHtml(formatted));

            if (m.isUnread()) {
                readStatus.setVisibility(View.VISIBLE);
            } else {
                readStatus.setVisibility(View.GONE);
            }

            subject.setText(m.getSubject());

            return convertView;
        }
    }

    private class LoadMessagesTask extends AsyncTask<Void, Void, List<Message>> {
        private boolean mClear;

        public LoadMessagesTask() {
            mClear = false;
        }

        public LoadMessagesTask(boolean clear) {
            mClear = clear;
        }

        @Override
        protected List<Message> doInBackground(Void... voids) {
            try {
                return Message.getMessages(mFilterType, null, null, mAccount);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(List<Message> result) {
            if (mClear) {
                mMessageList.clear();
            }
            for (Message m : result) {
                mMessageList.add(m);
            }
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
    }

    private class MarkReadTask extends AsyncTask<Void, Void, Void> {

        private Message mMessage;
        private boolean mRead;

        public MarkReadTask(Message m, boolean read) {
            mMessage = m;
            mRead = read;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("id", mMessage.getName()));
            if (mRead) {
                Log.i("MessagesFragment", Utilities.post(apiParams, "http://www.reddit.com/api/read_message", mAccount));
            } else {
                Log.i("MessagesFragment", Utilities.post(apiParams, "http://www.reddit.com/api/unread_message", mAccount));
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            mMessage.setIsRead(!mRead);
            mMessageAdapter.notifyDataSetChanged();
        }
    }

    private class BlockUserTask extends AsyncTask<Void, Void, Void> {

        private Message mMessage;

        public BlockUserTask(Message m) {
            mMessage = m;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("id", mMessage.getName()));
            Log.i("MessagesFragment", Utilities.post(apiParams, "http://www.reddit.com/api/block",
                    mAccount));
            return null;
        }
    }

    private class ReportCommentTask extends AsyncTask<Void, Void, Void> {

        private Message mMessage;

        public ReportCommentTask(Message m) {
            mMessage = m;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<NameValuePair> apiParams = new ArrayList<NameValuePair>();
            apiParams.add(new BasicNameValuePair("id", mMessage.getName()));
            Log.i("MessagesFragment", Utilities.post(apiParams, "http://www.reddit.com/api/report",
                    mAccount));
            return null;
        }
    }
}
