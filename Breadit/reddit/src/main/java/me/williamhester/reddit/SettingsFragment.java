package me.williamhester.reddit;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.williamhester.areddit.Account;

public class SettingsFragment extends PreferenceFragment {

    private Preference mClearHistory;
    private Preference mLogIn;
    private Preference mLogOut;
    private Preference mSwitchUsers;
    private Account mAccount;


    public static SettingsFragment newInstance(Account account) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("preferences");
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        mClearHistory = findPreference("pref_clear_history");
        mLogIn = findPreference("pref_login");
        mLogOut = findPreference("pref_logout");
        mSwitchUsers = findPreference("pref_switch_users");
        if(getArguments() != null) {
            mAccount = getArguments().getParcelable("account");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, null, false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mClearHistory) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Holo_Dialog);
            builder.setMessage(R.string.are_you_sure);
            builder.setTitle(R.string.clear_history);
            builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mAccount.setHistory("");
                    Log.i("SettingsFragment", mAccount.getUsername());
                    AccountDataSource dataSource = new AccountDataSource(getActivity());
                    dataSource.open();
                    dataSource.setHistory(mAccount);
                    dataSource.close();
                }
            });
            builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                }
            });
            Dialog d = builder.create();
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.show();
        }
        else if(preference == mLogIn) {
            LogInDialogFragment fragment = new LogInDialogFragment();
            fragment.show(getFragmentManager(), "login");
        }
        else if(preference == mLogOut) {
            Log.i("SettingsFragment", "logout");
            final List<Account> accounts;
            AccountDataSource dataSource = new AccountDataSource(getActivity());
            dataSource.open();
            accounts = dataSource.getAllAccounts();
            dataSource.close();
            final String[] accountNames = new String[accounts.size() + 1];
            for (int i = 0; i < accounts.size(); i++) {
                accountNames[i] = accounts.get(i).getUsername();
            }
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                    android.R.style.Theme_Holo_Dialog);
            int selection = 0;
            final SharedPreferences prefs = getActivity()
                    .getSharedPreferences("preferences", Context.MODE_PRIVATE);
            long currentId = prefs.getLong("accountId", -1);
            if (currentId != -1) {
                for (int i = 0; i < accounts.size(); i++) {
                    if (currentId == accounts.get(i).getId()) {
                        selection = i;
                    }
                }
            } else {
                selection = accountNames.length - 1;
            }
            builder.setTitle(R.string.select_to_remove)
                    .setSingleChoiceItems(accountNames, selection,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
//                            selection = i;
                                }
                            })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Nothing
                        }
                    })
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            long id = -1;
                            int selection = ((AlertDialog)dialogInterface)
                                    .getListView().getCheckedItemPosition();
                            if (selection < accounts.size()) {
                                id = accounts.get(selection).getId();
                            }
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong("accountId", id);
                            editor.commit();
                        }
                    });
            Dialog d = builder.create();
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.show();
        }
        else if(preference == mSwitchUsers) {
            final List<Account> accounts;
            AccountDataSource dataSource = new AccountDataSource(getActivity());
            dataSource.open();
            accounts = dataSource.getAllAccounts();
            dataSource.close();
            final String[] accountNames = new String[accounts.size() + 1];
            for (int i = 0; i < accounts.size(); i++) {
                accountNames[i] = accounts.get(i).getUsername();
            }
            accountNames[accounts.size()] = "Log out";
            final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                    android.R.style.Theme_Holo_Dialog);
            int selection = 0;
            final SharedPreferences prefs = getActivity()
                    .getSharedPreferences("preferences", Context.MODE_PRIVATE);
            long currentId = prefs.getLong("accountId", -1);
            if (currentId != -1) {
                for (int i = 0; i < accounts.size(); i++) {
                    if (currentId == accounts.get(i).getId()) {
                        selection = i;
                    }
                }
            } else {
                selection = accountNames.length - 1;
            }
            builder.setTitle(R.string.select_an_account)
                    .setSingleChoiceItems(accountNames, selection,
                            new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
//                            selection = i;
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            // Nothing
                        }
                    })
                    .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            long id = -1;
                            int selection = ((AlertDialog)dialogInterface)
                                    .getListView().getCheckedItemPosition();
                            if (selection < accounts.size()) {
                                id = accounts.get(selection).getId();
                            }
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong("accountId", id);
                            editor.commit();
                        }
                    });
            Dialog d = builder.create();
            d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            d.show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
