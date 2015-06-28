package me.williamhester.ui.fragments;

import android.os.Bundle;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;

/**
 * Created by william on 9/5/14.
 */
public abstract class AccountFragment extends BaseFragment {

    protected Account mAccount;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAccount = AccountManager.getAccount();
    }

    @Override
    public void onResume() {
        super.onResume();

        Account account = AccountManager.getAccount();
        if (!(account == null && mAccount == null)
                && (mAccount == null
                || account == null
                || !account.equals(mAccount))) {
            mAccount = AccountManager.getAccount();
            onAccountChanged();
        }
    }

    /**
     * This method is called when the account is changed.
     */
    public abstract void onAccountChanged();

}
