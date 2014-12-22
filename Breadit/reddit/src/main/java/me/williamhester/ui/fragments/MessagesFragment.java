package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.models.GenericListing;
import me.williamhester.models.GenericResponseRedditWrapper;
import me.williamhester.models.Message;
import me.williamhester.models.utils.Utilities;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.ui.activities.SubmissionActivity;
import me.williamhester.ui.activities.UserActivity;
import me.williamhester.ui.text.ClickableLinkMovementMethod;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.VotableViewHolder;

public class MessagesFragment extends AccountFragment implements Toolbar.OnMenuItemClickListener {

    private boolean mRefreshing = false;

    private Context mContext;

    private ArrayList<Message> mMessages;
    private InfiniteLoadingScrollListener mScrollListener;
    private MessageArrayAdapter mMessageAdapter;
    private LinearLayoutManager mLayoutManager;
    private ProgressBar mProgressBar;
    private RecyclerView mMessagesRecyclerView;
    private String mFilterType;
    private SwipeRefreshLayout mRefreshLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessages = new ArrayList<>();
        mContext = getActivity();
        if (savedInstanceState != null) {
            mFilterType = savedInstanceState.getString("filter_by");
            mRefreshing = savedInstanceState.getBoolean("refreshing");
        } else if (getArguments() != null) {
            mFilterType = getArguments().getString("filter_by", Message.ALL);
        } else {
            mFilterType = Message.ALL;
        }
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, root, false);

        Toolbar toolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);
        toolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        toolbar.setOnMenuItemClickListener(this);
        new MenuInflater(getActivity()).inflate(R.menu.messages, toolbar.getMenu());
        toolbar.getMenu().findItem(R.id.action_compose_message)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setProgressBackgroundColor(R.color.darkest_gray);
        mRefreshLayout.setColorSchemeResources(R.color.orangered);
        mRefreshLayout.setRefreshing(mRefreshing);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshing = true;
                RedditApi.getMessages(getActivity(), mFilterType, null, mMessageCallback);
            }
        });
        mMessagesRecyclerView = (RecyclerView) v.findViewById(R.id.messages);
        mMessageAdapter = new MessageArrayAdapter();
        mLayoutManager = new LinearLayoutManager(getActivity());
        mScrollListener = new InfiniteLoadingScrollListener();
        mMessagesRecyclerView.setLayoutManager(mLayoutManager);
        mMessagesRecyclerView.setAdapter(mMessageAdapter);
        mMessagesRecyclerView.setOnScrollListener(mScrollListener);
        mMessagesRecyclerView.addItemDecoration(new DividerItemDecoration(getResources()
                .getDrawable(R.drawable.card_divider)));

        Spinner messagesType = (Spinner) v.findViewById(R.id.messages_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(mContext,
                R.layout.spinner_item, R.id.spinner_text,
                getResources().getStringArray(R.array.filter_types));
        messagesType.setAdapter(adapter);
        messagesType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String filterType = null;
                switch (i) {
                    case 0:
                        filterType = Message.ALL;
                        break;
                    case 1:
                        filterType = Message.UNREAD;
                        break;
                    case 2:
                        filterType = Message.MESSAGES;
                        break;
                    case 3:
                        filterType = Message.COMMENT_REPLIES;
                        break;
                    case 4:
                        filterType = Message.POST_REPLIES;
                        break;
                    case 5:
                        filterType = Message.SENT;
                        break;
                    case 6:
                        filterType = Message.MOD_MAIL;
                        break;
                }
                if (filterType != null && !filterType.equals(mFilterType)) {
                    mFilterType = filterType;
                    mRefreshing = true;
                    mProgressBar.setVisibility(View.VISIBLE);
                    RedditApi.getMessages(getActivity(), mFilterType, null, mMessageCallback);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });

        RedditApi.getMessages(getActivity(), mFilterType, null, mMessageCallback);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("filter_by", mFilterType);
        outState.putBoolean("refreshing", mRefreshing);
    }

    private FutureCallback<JsonObject> mMessageCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            mProgressBar.setVisibility(View.GONE);
            mRefreshLayout.setRefreshing(false);
            if (e != null) {
                // Failed to load
                return;
            }
            if (mRefreshing) {
                mRefreshing = false;
                mMessages.clear();
                mScrollListener.resetState();
            }
            Gson gson = new Gson();

            // Generics are just beautiful.
            TypeToken<GenericResponseRedditWrapper<GenericListing<Message>>> token =
                    new TypeToken<GenericResponseRedditWrapper<GenericListing<Message>>>() {};

            GenericResponseRedditWrapper<GenericListing<Message>> wrapper =
                    gson.fromJson(result, token.getType());
            GenericListing<Message> listing = wrapper.getData();
            ArrayList<Message> messages = new ArrayList<>();
            for (GenericResponseRedditWrapper<Message> message : listing.getChildren()) {
                messages.add(message.getData());
            }
            mMessages.addAll(messages);
            if (mMessages.size() == messages.size()) {
                mMessageAdapter.notifyDataSetChanged();
            } else {
                mMessageAdapter.notifyItemRangeInserted(mMessages.size() - messages.size(),
                        messages.size());
            }
        }
    };

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_compose_message: {
                ComposeMessageFragment fragment = ComposeMessageFragment.newInstance();
                getFragmentManager().beginTransaction()
                        .replace(R.id.container, fragment, "composeMessage")
                        .addToBackStack("composeMessage")
                        .commit();
            }
        }
        return false;
    }

    public class InfiniteLoadingScrollListener extends RecyclerView.OnScrollListener {

        private final int VISIBLE_THRESHOLD = 5;
        private int mPreviousTotal = 0;
        private boolean mLoading = true;

        @Override
        public void onScrolled(RecyclerView absListView, int dx, int dy) {
            if (mLoading) {
                if (mMessageAdapter.getItemCount() > mPreviousTotal) {
                    mPreviousTotal = mMessageAdapter.getItemCount();
                    mLoading = false;
                    mProgressBar.setVisibility(View.GONE);
                }
            } else if (mMessages.size() > 0
                    && (mMessageAdapter.getItemCount() - mMessagesRecyclerView.getChildCount()) // 25 - 4 = 21
                    <= (mLayoutManager.findFirstVisibleItemPosition() + VISIBLE_THRESHOLD)) { // 20 + 5 = 25
                loadMoreMessages();
                mLoading = true;
                mProgressBar.setVisibility(View.VISIBLE);
            }
        }

        public void resetState() {
            mPreviousTotal = mMessageAdapter.getItemCount();
            mLoading = false;
            mProgressBar.setVisibility(View.GONE);
        }

        private void loadMoreMessages() {
            String after;
            if (mMessages == null || mMessages.size() == 0) {
                after = null;
            } else {
                after = mMessages.get(mMessages.size() - 1).getName();
            }
            RedditApi.getMessages(getActivity(), mFilterType, after, mMessageCallback);
        }
    }

    private class MessageArrayAdapter extends RecyclerView.Adapter<VotableViewHolder> {
        @Override
        public VotableViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            return new MessageViewHolder(LayoutInflater.from(getActivity())
                    .inflate(R.layout.list_item_message, mMessagesRecyclerView, false));
        }

        @Override
        public void onBindViewHolder(VotableViewHolder votableViewHolder, int i) {
            votableViewHolder.setContent(mMessages.get(i));
        }

        @Override
        public int getItemCount() {
            return mMessages.size();
        }
    }

    private class MessageViewHolder extends VotableViewHolder {

        private Message mMessage;
        private TextView mSubject;
        private TextView mToFrom;
        private TextView mAuthor;
        private TextView mMetadata;
        private TextView mBody;
        private View mReadStatus;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mSubject = (TextView) itemView.findViewById(R.id.subject);
            mToFrom = (TextView) itemView.findViewById(R.id.to_from);
            mAuthor = (TextView) itemView.findViewById(R.id.author);
            mMetadata = (TextView) itemView.findViewById(R.id.metadata);
            mBody = (TextView) itemView.findViewById(R.id.body);
            mBody.setMovementMethod(new ClickableLinkMovementMethod());
            mReadStatus = itemView.findViewById(R.id.read_status);
            mSwipeView.findViewById(R.id.message_content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessage.isUnread()) {
                        mMessage.setUnread(false);
                        mReadStatus.setVisibility(View.GONE);
                        RedditApi.markMessageRead(getActivity(), true, mMessage.getName(),
                                new FutureCallback<String>() {
                                    @Override
                                    public void onCompleted(Exception e, String result) {
                                        if (e != null) {
                                            // do something about the exception
                                        }
                                        // The result here is not needed, because as long as it
                                        // doesn't contain an error, we're good.
                                    }
                                });
                    } else if (mMessage.isComment()) {
                        Bundle extras = new Bundle();
                        Intent i = new Intent(getActivity(), SubmissionActivity.class);
                        extras.putBoolean("isSingleThread", true);
                        extras.putString("permalink", mMessage.getContext());
                        i.putExtras(extras);
                        startActivity(i);
                    } else {
                        // Start a new fragment that shows the conversation like an instant messenger
                    }
                    // Do something specific to the message type. If it was a comment, then prompt
                    //     the user to either view context or that comment's single thread
                    //     either case will require a modification to the CommentsFragment that will
                    //     allow the user to switch to view the ENTIRE list of comments instead of
                    //     just that comment's thread. This should be done through a flag to the
                    //     activity, or maybe just simply containing the specific comment to get the
                    //     context of.
                }
            });
            mSwipeView.findViewById(R.id.message_content).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!mMessage.isUnread()) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    ArrayList<String> options = new ArrayList<>();
                    options.add(getResources().getString(R.string.reply));
                    options.add(getResources().getString(R.string.mark_unread));
                    options.add(getResources().getString(R.string.view_profile));
                    if (mMessage.isComment()) {
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
                                    mMessage.setUnread(true);
                                    RedditApi.markMessageRead(getActivity(), false, mMessage.getName(), new FutureCallback<String>() {
                                        @Override
                                        public void onCompleted(Exception e, String result) {
                                            if (e != null) {
                                                // do something about the exception
                                            }
                                            // The result here is not needed, because as long as it doesn't contain
                                            // an error, we're good.
                                        }
                                    });
                                    break;
                                case 2:
                                    // View user's profile
                                    Bundle b = new Bundle();
                                    b.putString("username", mMessage.getAuthor());
                                    Intent i = new Intent(mContext, UserActivity.class);
                                    i.putExtras(b);
                                    mContext.startActivity(i);
                                    break;
                                case 3:
                                    if (mMessage.isComment()) {
                                        // report
//                                        new ReportCommentTask(m).execute();
                                    } else {
                                        // Block user
//                                        new BlockUserTask(m).execute();
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
                    builder.show();
                }
                    return false;
                }
            });
        }

        @Override
        protected void onVoted() {

        }

        public void setContent(Object object) {
            super.setContent(object);
            mMessage = (Message) object;
            if (mMessage.getAuthor().equalsIgnoreCase(mAccount.getUsername())) {
                mToFrom.setText(getResources().getString(R.string.to) + " ");
            } else {
                mToFrom.setText(getResources().getString(R.string.from) + " ");
            }

            if (mMessage.isComment()) {
                mSwipeView.setEnabled(true);
            } else {
                mSwipeView.setEnabled(false);
            }

            mAuthor.setText(mMessage.getAuthor());
            mMetadata.setText(" " + Utilities.calculateTimeShort(mMessage.getCreatedUtc()));
            String unescaped = Html.fromHtml(mMessage.getBodyHtml()).toString();
            HtmlParser parser = new HtmlParser(unescaped);
            mBody.setText(parser.getSpannableString());

            if (mMessage.isUnread()) {
                mReadStatus.setVisibility(View.VISIBLE);
            } else {
                mReadStatus.setVisibility(View.GONE);
            }

            mSubject.setText(mMessage.getSubject());
        }
    }
}
