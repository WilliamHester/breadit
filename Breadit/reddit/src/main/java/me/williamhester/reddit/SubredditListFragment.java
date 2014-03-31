package me.williamhester.reddit;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by William on 3/31/14.
 */
public class SubredditListFragment extends Fragment {

    private SubbredditListFragmentListener mCallback;

    @Override
    public void onAttach(Activity activity) {
        try {
            mCallback = (SubbredditListFragmentListener) activity;
        } catch (ClassCastException e) {
            Log.e("SubbredditListFragment", "Hosting activity must implement" +
                    " SubredditListFragmentListener.");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    public interface SubbredditListFragmentListener {
        /**
         * The method that is called when the subreddit is selected
         *
         * @param subreddit the title of the subreddit without the "/r/" part
         */
        public void onSubredditSelected(String subreddit);
    }


}
