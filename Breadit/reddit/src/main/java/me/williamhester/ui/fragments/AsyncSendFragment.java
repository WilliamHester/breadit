package me.williamhester.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

import me.williamhester.reddit.R;
import me.williamhester.ui.views.MarkdownBodyView;

/**
 * This Fragment provides an abstraction between the ReplyFragment and ComposeMessageFragment. It
 * combines most of the logic between the two and keeps a variable, mKillOnStart, that will kill
 * the Fragment when it is started if the asynchronous task completes when the Fragment's state
 * is saved. It manages the saving of the state of the Fragment inside it.
 *
 * Created by William on 10/31/14.
 */
public abstract class AsyncSendFragment extends Fragment {

    protected boolean mKillOnStart;
    protected MarkdownBodyView mMarkdownBody;

    /**
     * This is called to get the body hint for the MarkdownBodyFragment
     *
     * @return the string for the hint for the MarkdownBodyFragment
     */
    protected abstract String getBodyHint();

    /**
     * This is called to get the text for the button that appears in the ActionBar to send the
     * thing.
     *
     * @return the string that represents the text on the button
     */
    protected abstract String getButtonText();

    /**
     * This is called to get the container id to inflate the MarkdownBodyFragment into.
     *
     * @return the id of the container.
     */
    protected abstract int getMarkdownBodyId();

    /**
     * This method is called when the save button in the ActionBar is clicked.
     */
    protected abstract void onSaveClick();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        if (savedInstanceState != null) {
            mKillOnStart = savedInstanceState.getBoolean("killOnStart");
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_reply, menu);

        Button button = (Button) menu.findItem(R.id.action_reply).getActionView();
        button.setText(getButtonText());
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSaveClick();
            }
        });
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mMarkdownBody = (MarkdownBodyView) view.findViewById(getMarkdownBodyId());
        mMarkdownBody.setHint(getBodyHint());
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mKillOnStart) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putBoolean("killOnStart", mKillOnStart);
    }

    protected void kill() {
        mKillOnStart = true;
        if (isResumed()) {
            getFragmentManager().popBackStack();
            InputMethodManager imm =  (InputMethodManager) getActivity().getSystemService(
                    Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
