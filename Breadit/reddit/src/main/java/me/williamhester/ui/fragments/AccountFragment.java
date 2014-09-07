package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;

/**
 * Created by william on 9/5/14.
 */
public class AccountFragment extends Fragment {

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
            mAccount = account;
            onAccountChanged();
        }
    }

    /**
     * This method is called when the account is changed.
     */
    protected void onAccountChanged() { }

}