package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 10/31/14.
 */
public class SubmitLinkFragment extends SubmitFragment {

    private EditText mUrl;
    private EditText mTitle;

    public static SubmitLinkFragment newInstance() {
        return new SubmitLinkFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_submit_link, container, false);
        mTitle = (EditText) v.findViewById(R.id.title);
        mUrl = (EditText) v.findViewById(R.id.url);
        Button suggest = (Button) v.findViewById(R.id.suggest_title);

        suggest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RedditApi.getSuggestedTitle(getActivity(), mUrl.getText().toString(),
                        new FutureCallback<JsonObject>() {
                            @Override
                            public void onCompleted(Exception e, JsonObject result) {
                                if (e != null) {
                                    e.printStackTrace();
                                    return;
                                }
                                JsonArray array = result.get("jquery").getAsJsonArray();
                                JsonArray suggested = array.get(12).getAsJsonArray();
                                JsonArray nameArray = suggested.get(3).getAsJsonArray();
                                mTitle.setText(nameArray.get(0).getAsString());
                            }
                        });
            }
        });

        return v;
    }

    public Map<String, List<String>> getSubmitBody() {
        Map<String, List<String>> body = new HashMap<>();
        body.put("kind", new ArrayList<String>(1));
        body.get("kind").add("link");
        body.put("url", new ArrayList<String>(1));
        body.get("url").add(mUrl.getText().toString());
        body.put("title", new ArrayList<String>(1));
        body.get("title").add(mTitle.getText().toString());
        return body;
    }
}
