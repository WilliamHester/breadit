package me.williamhester.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;

import me.williamhester.reddit.R;

/**
 * This Fragment provides an abstraction between the ReplyFragment and ComposeMessageFragment. It
 * combines most of the logic between the two and keeps a variable, mKillOnStart, that will kill
 * the Fragment when it is started if the asynchronous task completes when the Fragment's state
 * is saved. It manages the saving of the state of the Fragment inside it.
 *
 * Created by William on 10/31/14.
 */
public abstract class AsyncSendFragment extends Fragment {

    protected MarkdownBodyFragment mBodyFragment;
    protected boolean mKillOnStart;

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
    protected abstract int getContainerId();

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
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBodyFragment = MarkdownBodyFragment.newInstance(getBodyHint());
        if (savedInstanceState != null) {
            SavedState state = savedInstanceState.getParcelable("savedState");
            mBodyFragment.setInitialSavedState(state);
        }
        getChildFragmentManager().beginTransaction()
                .replace(getContainerId(), mBodyFragment, "body")
                .commit();
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
        SavedState state = getChildFragmentManager().saveFragmentInstanceState(mBodyFragment);
        outState.putParcelable("savedState", state);
    }
}
