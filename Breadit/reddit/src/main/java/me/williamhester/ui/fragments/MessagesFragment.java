package me.williamhester.ui.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
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
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.tools.HtmlParser;
import me.williamhester.ui.activities.BrowseActivity;
import me.williamhester.ui.text.ClickableLinkMovementMethod;
import me.williamhester.ui.views.DividerItemDecoration;
import me.williamhester.ui.views.VotableViewHolder;
import me.williamhester.ui.widget.InfiniteLoadToolbarHideScrollListener;

public class MessagesFragment extends AccountFragment implements Toolbar.OnMenuItemClickListener,
        InfiniteLoadToolbarHideScrollListener.OnLoadMoreListener {

    private boolean mRefreshing = false;

    private ArrayList<Message> mMessages;
    private InfiniteLoadToolbarHideScrollListener mScrollListener;
    private MessageArrayAdapter mMessageAdapter;
    private MessageFragmentCallbacks mCallback;
    private MessageViewHolder mFocusedViewHolder;
    private ProgressBar mProgressBar;
    private RecyclerView mMessagesRecyclerView;
    private String mFilterType;
    private SwipeRefreshLayout mRefreshLayout;
    private Toolbar mToolbar;

    public static MessagesFragment newInstance() {
        return new MessagesFragment();
    }

    public static MessagesFragment newInstance(String filterType) {
        Bundle args = new Bundle();
        args.putString("filter_by", filterType);
        MessagesFragment fragment = new MessagesFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof MessageFragmentCallbacks) {
            mCallback = (MessageFragmentCallbacks) activity;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mMessages = new ArrayList<>();
        if (savedInstanceState != null) {
            mFilterType = savedInstanceState.getString("filter_by");
            mRefreshing = savedInstanceState.getBoolean("refreshing");
            mMessages = savedInstanceState.getParcelableArrayList("messages");
        } else if (getArguments() != null) {
            mFilterType = getArguments().getString("filter_by", Message.ALL);
        } else {
            mFilterType = Message.ALL;
        }
    }

    @Override
    public void onAccountChanged() {

    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_messages, root, false);

        mToolbar = (Toolbar) v.findViewById(R.id.toolbar_actionbar);
        if (mCallback == null) {
            mToolbar.setNavigationIcon(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getActivity().onBackPressed();
                }
            });
        } else {
            mToolbar.setNavigationIcon(R.drawable.ic_drawer);
            mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mCallback.onHomeClicked();
                }
            });
        }
        mToolbar.setOnMenuItemClickListener(this);
        onCreateOptionsMenu(mToolbar.getMenu(), getActivity().getMenuInflater());

        mProgressBar = (ProgressBar) v.findViewById(R.id.progress_bar);
        mRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.swipe_refresh);
        mRefreshLayout.setProgressBackgroundColor(R.color.primary);
        mRefreshLayout.setColorSchemeResources(R.color.orangered);
        mRefreshLayout.setRefreshing(mRefreshing);
        mRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mRefreshing = true;
                RedditApi.getMessages(getActivity(), mFilterType, null, mMessageCallback);
            }
        });
        mToolbar.post(new Runnable() {
            @Override
            public void run() {
                float density = getResources().getDisplayMetrics().density;
                int startAt = mToolbar.getHeight() - (int) (40 * density);
                int endAt = startAt + (int) (64 * density);
                mRefreshLayout.setProgressViewOffset(false, startAt, endAt);
            }
        });
        mMessagesRecyclerView = (RecyclerView) v.findViewById(R.id.inbox);
        mMessageAdapter = new MessageArrayAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mScrollListener = new InfiniteLoadToolbarHideScrollListener(mMessageAdapter, mToolbar,
                mMessagesRecyclerView, mMessages, layoutManager, this);
        mMessagesRecyclerView.setLayoutManager(layoutManager);
        mMessagesRecyclerView.setAdapter(mMessageAdapter);
        mMessagesRecyclerView.setOnScrollListener(mScrollListener);
        mMessagesRecyclerView.addItemDecoration(new DividerItemDecoration(getResources()
                .getDrawable(R.drawable.card_divider)));

        Spinner messagesType = (Spinner) v.findViewById(R.id.messages_type);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
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

        if (mMessages.size() == 0) {
            mProgressBar.setVisibility(View.VISIBLE);
            RedditApi.getMessages(getActivity(), mFilterType, null, mMessageCallback);
        } else {
            mProgressBar.setVisibility(View.GONE);
        }

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString("filter_by", mFilterType);
        outState.putBoolean("refreshing", mRefreshing);
        outState.putParcelableArrayList("messages", mMessages);
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
            if (listing == null) {
                return;
            }
            ArrayList<Message> messages = new ArrayList<>();
            for (GenericResponseRedditWrapper<Message> message : listing.getChildren()) {
                messages.add(message.getData());
            }
            mMessages.addAll(messages);
            if (mMessages.size() == messages.size()) {
                mMessageAdapter.notifyDataSetChanged();
            } else {
                mMessageAdapter.notifyItemRangeInserted(mMessages.size() - messages.size() + 1,
                        messages.size() + 1);
            }
        }
    };

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.messages, menu);
    }

    @Override
    public boolean onMenuItemClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.action_compose_message: {
                ComposeMessageFragment fragment = ComposeMessageFragment.newInstance();
                getFragmentManager().beginTransaction()
                        .replace(R.id.main_container, fragment, "composeMessage")
                        .addToBackStack("composeMessage")
                        .commit();
            }
        }
        return false;
    }

    private void countUnreadAndNotify() {
        if (mCallback == null) {
            return;
        }
        int count = 0;
        for (Message m : mMessages) {
            if (m.isUnread()) {
                count++;
            }
        }
        mCallback.onMessageReadCountChanged(count);
    }

    @Override
    public void onLoadMore() {
        mProgressBar.setVisibility(View.VISIBLE);
        String after;
        if (mMessages == null || mMessages.size() == 0) {
            after = null;
        } else {
            after = mMessages.get(mMessages.size() - 1).getName();
        }
        RedditApi.getMessages(getActivity(), mFilterType, after, mMessageCallback);
    }

    private class MessageArrayAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        
        private static final int MESSAGE = 1;
        private static final int HEADER = 2;
        private static final int FOOTER = 3;
        
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            if (viewType == HEADER) {
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT, mToolbar.getHeight());
                View header = new View(getActivity());
                header.setLayoutParams(params);
                return new RecyclerView.ViewHolder(header) { };
            } else if (viewType == FOOTER) {
                return new RecyclerView.ViewHolder(inflater.inflate(R.layout.footer_spacer, parent,
                        false)) {};
            }
            return new MessageViewHolder(inflater.inflate(R.layout.list_item_message,
                    mMessagesRecyclerView, false));
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {
            if (getItemViewType(position) == MESSAGE) {
                ((MessageViewHolder) viewHolder).setContent(mMessages.get(position - 1));
            }
        }

        @Override
        public int getItemCount() {
            return mMessages.size() + 2;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == getItemCount() - 1) {
                return FOOTER;
            } else if (position == 0) {
                return HEADER;
            }
            return MESSAGE;
        }
    }

    private class MessageViewHolder extends VotableViewHolder {

        private Message mMessage;
        private TextView mSubject;
        private TextView mToFrom;
        private TextView mBody;
        private View mOptionsRow;
        private View mReadStatus;

        public MessageViewHolder(View itemView) {
            super(itemView);

            mSubject = (TextView) itemView.findViewById(R.id.subject);
            mToFrom = (TextView) itemView.findViewById(R.id.to_from);
            mBody = (TextView) itemView.findViewById(R.id.body);
            mBody.setMovementMethod(new ClickableLinkMovementMethod());

            mOptionsRow = itemView.findViewById(R.id.options_row);
            View reply = itemView.findViewById(R.id.option_reply);
            View unread = itemView.findViewById(R.id.option_mark_unread);
            View user = itemView.findViewById(R.id.option_view_user);
            View overflow = itemView.findViewById(R.id.option_overflow);
            final View subreddit = itemView.findViewById(R.id.option_go_to_subreddit);

            reply.setOnClickListener(mOptionsClickListener);
            unread.setOnClickListener(mOptionsClickListener);
            user.setOnClickListener(mOptionsClickListener);
            overflow.setOnClickListener(mOptionsClickListener);
            subreddit.setOnClickListener(mOptionsClickListener);

            mReadStatus = itemView.findViewById(R.id.read_status);
            mSwipeView.findViewById(R.id.message_content).setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!mMessage.isUnread()) {
                        expand(mOptionsRow);
                        if (mFocusedViewHolder != null) {
                            collapse(mFocusedViewHolder.mOptionsRow);
                        }
                        if (mFocusedViewHolder == MessageViewHolder.this) {
                            mFocusedViewHolder = null;
                        } else {
                            mFocusedViewHolder = MessageViewHolder.this;
                        }
                        subreddit.setVisibility(mMessage.isComment() ? View.VISIBLE : View.GONE);
                        return true;
                    }
                    return false;
                }
            });
            mSwipeView.findViewById(R.id.message_content).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mMessage.isUnread()) {
                        mMessage.setUnread(false);
                        mReadStatus.setVisibility(View.GONE);
                        countUnreadAndNotify();
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
                        Intent i = new Intent(getActivity(), BrowseActivity.class);
                        extras.putString("type", "comments");
                        extras.putBoolean("isSingleThread", true);
                        extras.putString("permalink", mMessage.getContext());
                        i.putExtras(extras);
                        startActivity(i);
//                    } else {
                        // Todo: make a new fragment that shows the conversation like an instant messenger
                    }
                }
            });
        }

        @Override
        protected void onVoted() {

        }

        @Override
        public void expandOptions() {

        }

        @Override
        public void collapseOptions() {

        }

        public void setContent(Object object) {
            super.setContent(object);
            mMessage = (Message) object;
            
            collapse(mOptionsRow);
            mSwipeView.setEnabled(mMessage.isComment());
            mReadStatus.setVisibility(mMessage.isUnread() ? View.VISIBLE : View.GONE);

            SpannableStringBuilder toFrom = new SpannableStringBuilder();
            if (mMessage.getAuthor().equalsIgnoreCase(mAccount.getUsername())) {
                toFrom.append(getResources().getString(R.string.to))
                        .append(' ')
                        .append(mMessage.getDestination());
            } else {
                toFrom.append(getResources().getString(R.string.from))
                        .append(' ')
                        .append(mMessage.getAuthor());
            }
            if (mMessage.isComment()) {
                toFrom.append(' ')
                        .append(getResources().getString(R.string.via))
                        .append(" /r/")
                        .append(mMessage.getSubreddit());
            }
            toFrom.append(' ')
                    .append(calculateTimeShort(mMessage.getCreatedUtc()));
            mToFrom.setText(toFrom);

            String unescaped = Html.fromHtml(mMessage.getBodyHtml()).toString();
            HtmlParser parser = new HtmlParser(unescaped);
            mBody.setText(parser.getSpannableString());

            SpannableStringBuilder ssb = new SpannableStringBuilder();
            if (mMessage.isComment()) {
                ssb.append(getResources().getString(R.string.comment_reply))
                        .setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                ssb.append(' ')
                        .append(getResources().getString(R.string.on));
                String linkTitle = Html.fromHtml(mMessage.getLinkTitle()).toString();
                ssb.append(' ')
                        .append(linkTitle)
                        .setSpan(new StyleSpan(Typeface.ITALIC), ssb.length() - linkTitle.length(),
                                ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            } else {
                ssb.append(Html.fromHtml(mMessage.getSubject()).toString())
                        .setSpan(new StyleSpan(Typeface.BOLD), 0, ssb.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            mSubject.setText(ssb);
        }

        private View.OnClickListener mOptionsClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.option_reply: {
                        Fragment reply = ReplyFragment.newInstance(mMessage);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.main_container, reply, "ReplyFragment")
                                .addToBackStack("ReplyFragment")
                                .commit();
                        break;
                    }
                    case R.id.option_mark_unread: {
                        mMessage.setUnread(true);
                        mReadStatus.setVisibility(View.VISIBLE);
                        countUnreadAndNotify();
                        RedditApi.markMessageRead(getActivity(), false, mMessage.getName(),
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
                        break;
                    }
                    case R.id.option_view_user: {
                        Bundle b = new Bundle();
                        b.putString("type", "user");
                        b.putString("username", mMessage.getAuthor());
                        Intent i = new Intent(getActivity(), BrowseActivity.class);
                        i.putExtras(b);
                        getActivity().startActivity(i);
                        break;
                    }
                    case R.id.option_go_to_subreddit: {
                        Intent i = new Intent(getActivity(), BrowseActivity.class);
                        Bundle args = new Bundle();
                        i.setAction(Intent.ACTION_VIEW);
                        args.putString("type", "subreddit");
                        args.putString("subreddit", mMessage.getSubreddit());
                        i.putExtras(args);
                        startActivity(i);
                        break;
                    }
                    case R.id.option_overflow: {
                        break;
                    }
                }
            }
        };
    }

    public static interface MessageFragmentCallbacks extends TopLevelFragmentCallbacks {
        public void onMessageReadCountChanged(int newCount);
    }
}
