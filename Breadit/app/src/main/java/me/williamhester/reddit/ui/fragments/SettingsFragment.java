package me.williamhester.reddit.ui.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import me.williamhester.reddit.SettingsManager;
import me.williamhester.reddit.databases.AccountDataSource;
import me.williamhester.reddit.models.reddit.Account;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.activities.LogInActivity;

public class SettingsFragment extends PreferenceFragment {

  public static final int LOG_IN_REQUEST = 1;

  private Preference mClearHistory;
  private Preference mLogIn;
  private Preference mLogOut;
  private Preference mSwitchUsers;

  public static SettingsFragment newInstance() {
    SettingsFragment fragment = new SettingsFragment();
    Bundle args = new Bundle();
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
    SharedPreferences prefs = getActivity().getSharedPreferences("preferences", Context.MODE_PRIVATE);
    prefs.registerOnSharedPreferenceChangeListener(new SharedPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals("pref_default_comment_sort")) {
          SettingsManager.setDefaultCommentSort(prefs
              .getString("pref_default_comment_sort", RedditApi.COMMENT_SORT_BEST));
        }
      }
    });
  }

  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_settings, container, false);
  }

  @Override
  public void onResume() {
    super.onResume();
    mClearHistory.setEnabled(AccountManager.isLoggedIn());
  }

  @Override
  public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
                                       @NonNull Preference preference) {
    if (preference == mClearHistory) {
      AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
      builder.setMessage(R.string.are_you_sure);
      builder.setTitle(R.string.clear_history);
      builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
          // TODO: Actually clear the history
        }
      });
      builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialogInterface, int i) {

        }
      });
      builder.show();
    } else if (preference == mLogIn) {
      Intent i = new Intent(getActivity(), LogInActivity.class);
      getActivity().startActivityForResult(i, LOG_IN_REQUEST);
    } else if (preference == mLogOut) {
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
              int selection = ((AlertDialog) dialogInterface)
                  .getListView().getCheckedItemPosition();
              if (selection < accounts.size()) {
                AccountManager.setAccount(accounts.get(selection));
              } else {
                AccountManager.setAccount(null);
              }
            }
          });
      Dialog d = builder.create();
      d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
      d.show();
    } else if (preference == mSwitchUsers) {
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
              int selection = ((AlertDialog) dialogInterface)
                  .getListView().getCheckedItemPosition();
              if (selection < accounts.size()) {
                mClearHistory.setEnabled(true);
                AccountManager.setAccount(accounts.get(selection));
              } else {
                mClearHistory.setEnabled(false);
                AccountManager.setAccount(null);
              }
            }
          });
      Dialog d = builder.create();
      d.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
      d.show();
    }
    return super.onPreferenceTreeClick(preferenceScreen, preference);
  }

}
