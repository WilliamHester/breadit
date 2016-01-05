package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.koushikdutta.async.ByteBufferList;
import com.koushikdutta.async.DataEmitter;
import com.koushikdutta.async.callback.DataCallback;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;

import java.util.ArrayList;

import me.williamhester.models.reddit.RedditLiveResponse;
import me.williamhester.models.reddit.RedditLive;
import me.williamhester.models.reddit.Submission;
import me.williamhester.models.reddit.ResponseWrapper;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 7/29/14.
 */
public class RedditLiveFragment extends BaseFragment {

    private static final String SUBMISSION = "submission";

    private Submission mSubmission;
    private LiveAdapter mAdapter;
    private final ArrayList<RedditLiveResponse> mRedditLiveResponses = new ArrayList<>();

    public static RedditLiveFragment newInstance(Submission submission) {
        Bundle args = new Bundle();
        args.putParcelable(SUBMISSION, submission);
        RedditLiveFragment fragment = new RedditLiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mSubmission = getArguments().getParcelable(SUBMISSION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reddit_live, root, false);

        mAdapter = new LiveAdapter();
        ListView liveComments = (ListView) v.findViewById(R.id.live_comments);
        liveComments.setAdapter(mAdapter);

        RedditApi.getRedditLiveData(getActivity(), mSubmission, mLiveCallback);
        return v;
    }

    private void setUpLiveListener(String url) {
        AsyncHttpClient.getDefaultInstance().websocket(url, "wss", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    ex.printStackTrace();
                    return;
                }
                Log.d("RedditLiveFragment", "Connected!");
                webSocket.setDataCallback(new DataCallback() {
                    @Override
                    public void onDataAvailable(DataEmitter emitter, ByteBufferList bb) {
                        Log.d("RedditLiveFragment", "Got some data");
                    }
                });
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        if (s.contains("update")) {
                            Gson gson = new Gson();
                            RedditLiveResponse redditLiveResponse = gson.fromJson(s, RedditLiveResponse.class);
                            mRedditLiveResponses.add(redditLiveResponse);
                            if (getView() != null) {
                                getView().post(new Runnable() {
                                    @Override
                                    public void run() {
                                        mAdapter.notifyDataSetChanged();
                                    }
                                });
                            }
                        }
                    }
                });
            }
        });
    }

    private FutureCallback<ResponseWrapper> mLiveCallback =
            new FutureCallback<ResponseWrapper>() {
        @Override
        public void onCompleted(Exception e, ResponseWrapper result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            if (result.getData() instanceof RedditLive) {
                setUpLiveListener(((RedditLive) result.getData()).getWebsocketUrl());
            }
        }
    };

    private class LiveAdapter extends ArrayAdapter<RedditLiveResponse> {

        public LiveAdapter() {
            super(getActivity(), R.layout.list_item_live, mRedditLiveResponses);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(getActivity(), R.layout.list_item_live, null);
            }

            return convertView;
        }

    }

}
