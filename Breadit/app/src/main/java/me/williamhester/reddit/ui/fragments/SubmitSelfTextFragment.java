package me.williamhester.reddit.ui.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import butterknife.Bind;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.views.MarkdownBodyView;

/**
 * Created by william on 10/31/14.
 */
public class SubmitSelfTextFragment extends SubmitFragment {

  @Bind(R.id.markdown_body) MarkdownBodyView mBody;
  @Bind(R.id.title) EditText mTitle;

  public static SubmitSelfTextFragment newInstance() {
    return new SubmitSelfTextFragment();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_submit_self_text, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    mBody.setHint(getResources().getString(R.string.self_text));
  }

  public boolean isValid() {
    return mTitle.getText().length() > 0;
  }

  public Map<String, List<String>> getSubmitBody() {
    Map<String, List<String>> body = new HashMap<>();
    body.put("kind", new ArrayList<String>(1));
    body.get("kind").add("self");
    body.put("text", new ArrayList<String>(1));
    body.get("text").add(mBody.getBody());
    body.put("title", new ArrayList<String>(1));
    body.get("title").add(mTitle.getText().toString());
    return body;
  }
}
