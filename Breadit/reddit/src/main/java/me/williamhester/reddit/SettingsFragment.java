package me.williamhester.reddit;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import me.williamhester.areddit.Account;

public class SettingsFragment extends PreferenceFragment {

    private Preference mRemoveViewedFront;
    private Preference mRemoveViewedSub;
    private Preference mClearHistory;
    private Preference mHideNSFW;
    private Preference mWarnNSFW;
    private Preference mLogIn;
    private Preference mLogOut;
    private Preference mSwitchUsers;
    private Account mAccount;
    private SharedPreferences mPrefs;


    public static SettingsFragment newInstance(Account account) {
        SettingsFragment fragment = new SettingsFragment();
        Bundle args = new Bundle();
        args.putParcelable("account", account);
        fragment.setArguments(args);
        return fragment;
    }
    public SettingsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("preferences");
        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);
        mPrefs = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);

        mRemoveViewedFront = findPreference("pref_remove_viewed_front");
        mRemoveViewedSub = findPreference("pref_remove_viewed_sub");
        mClearHistory = findPreference("pref_clear_history");
        mHideNSFW = findPreference("pref_hide_nsfw");
        mWarnNSFW = findPreference("pref_warn_nsfw");
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
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if(preference == mClearHistory) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),android.R.style.Theme_Holo_Dialog);
            builder.setMessage(R.string.are_you_sure);
            builder.setTitle(R.string.clear_history);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    mAccount.setHistory("");
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
            builder.create().show();
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
