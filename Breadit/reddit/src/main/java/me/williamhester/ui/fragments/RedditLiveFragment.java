package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.Gson;
import com.koushikdutta.async.http.AsyncHttpClient;
import com.koushikdutta.async.http.WebSocket;
import com.koushikdutta.async.http.socketio.Acknowledge;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.LiveResponse;
import me.williamhester.models.Submission;
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
        args.putSerializable(SUBMISSION, submission);
        RedditLiveFragment fragment = new RedditLiveFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mSubmission = (Submission) getArguments().getSerializable(SUBMISSION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_reddit_live, root, false);

        ListView liveComments = (ListView) v.findViewById(R.id.live_comments);
        liveComments.setAdapter(mAdapter);
        return v;
    }

    private void setUpLiveListener() {
        AsyncHttpClient.getDefaultInstance().websocket(
                "wss://wss.redditmedia.com/live/tayzhy1oj2g5?h=902e90a7fcb1d6b4be0a1b161cce607886ce12d3&amp;e=1406784103",
                "my-protocol", new AsyncHttpClient.WebSocketConnectCallback() {
            @Override
            public void onCompleted(Exception ex, WebSocket webSocket) {
                if (ex != null) {
                    Log.e("RedditLiveFragment", ex.toString());
                    return;
                }
                webSocket.send(new byte[10]);
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

    private class LiveAdapter extends ArrayAdapter<LiveResponse> {

        public LiveAdapter(Context context) {
            super(context, R.layout.list_item_live, mLiveResponses);
        }

    }

}
