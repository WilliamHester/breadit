package me.williamhester.ui.fragments;

import android.support.v4.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.socketio.Acknowledge;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.LiveResponse;
import me.williamhester.models.RedditLive;
import me.williamhester.models.ResponseRedditWrapper;
import me.williamhester.models.Submission;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 7/29/14.
 */
public class RedditLiveFragment extends Fragment {

    private static final String SUBMISSION = "submission";

    private Submission mSubmission;
    private LiveAdapter mAdapter;
    private final ArrayList<LiveResponse> mLiveResponses = new ArrayList<>();

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
                webSocket.setStringCallback(new WebSocket.StringCallback() {
                    @Override
                    public void onStringAvailable(String s) {
                        if (s.contains("update")) {
                            Gson gson = new Gson();
                            LiveResponse liveResponse = gson.fromJson(s, LiveResponse.class);
                            mLiveResponses.add(liveResponse);
                            mAdapter.notifyDataSetChanged();
                        }
                    }
                });
            }
        });
    }

    private FutureCallback<ResponseRedditWrapper> mLiveCallback =
            new FutureCallback<ResponseRedditWrapper>() {
        @Override
        public void onCompleted(Exception e, ResponseRedditWrapper result) {
            if (e != null) {
                e.printStackTrace();
                return;
            }
            if (result.getData() instanceof RedditLive) {
                setUpLiveListener(((RedditLive) result.getData()).getWebsocketUrl());
            }
        }
    };

    private class LiveAdapter extends ArrayAdapter<LiveResponse> {

        public LiveAdapter() {
            super(getActivity(), R.layout.list_item_live, mLiveResponses);
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
