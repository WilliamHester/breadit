package me.williamhester.ui.fragments;

import android.os.Bundle;

import me.williamhester.models.reddit.RedditAccount;
import me.williamhester.models.AccountManager;

/**
 * Created by william on 9/5/14.
 */
public abstract class AccountFragment extends BaseFragment {

    protected RedditAccount mRedditAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRedditAccount = AccountManager.getAccount();
    }

    @Override
    public void onResume() {
        super.onResume();

        RedditAccount redditAccount = AccountManager.getAccount();
        if (!(redditAccount == null && mRedditAccount == null)
                && (mRedditAccount == null
                || redditAccount == null
                || !redditAccount.equals(mRedditAccount))) {
            mRedditAccount = AccountManager.getAccount();
            onAccountChanged();
        }
    }

    /**
     * This method is called when the account is changed.
     */
    public abstract void onAccountChanged();

}
