package me.williamhester.reddit.ui.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.reddit.models.reddit.Account;
import me.williamhester.reddit.models.AccountManager;
import me.williamhester.reddit.models.reddit.GenericListing;
import me.williamhester.reddit.models.reddit.GenericResponseWrapper;
import me.williamhester.reddit.models.reddit.Message;
import me.williamhester.reddit.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.reddit.ui.activities.SettingsActivity;
import me.williamhester.reddit.ui.activities.SubmitActivity;

/**
 * The NavigationDrawerFragment provides top-level navigation to the application.
 */
public class NavigationDrawerFragment extends AccountFragment {

  private NavigationDrawerCallbacks mCallback;

  public static NavigationDrawerFragment newInstance() {
    return new NavigationDrawerFragment();
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    try {
      mCallback = (NavigationDrawerCallbacks) activity;
    } catch (ClassCastException e) {
      throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
    }
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
  }

  @Override
  public void onViewCreated(View view, Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    Spinner accountSpinner = (Spinner) view.findViewById(R.id.account_spinner);
    accountSpinner.setAdapter(new AccountAdapter());
    accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override
      public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if (i == AccountManager.getAccounts().size()) {
          if (AccountManager.getAccount() != null) {
            AccountManager.setAccount(null);
            mCallback.onAccountChanged();
            onAccountChanged();
          }
        } else {
          Account a = AccountManager.getAccounts().get(i);
          if (!a.equals(AccountManager.getAccount())) {
            AccountManager.setAccount(AccountManager.getAccounts().get(i));
            mCallback.onAccountChanged();
            onAccountChanged();
          }
        }
      }

      @Override
      public void onNothingSelected(AdapterView<?> adapterView) {
        // Do nothing
      }
    });
    selectCurrentAccount(view);

    final View home = view.findViewById(R.id.home);
    final View messages = view.findViewById(R.id.inbox);

    home.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mCallback.onHomeSelected();
      }
    });
    messages.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mCallback.onMessagesSelected();
      }
    });

    View submit = view.findViewById(R.id.submit);
    submit.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        Intent i = new Intent(getActivity(), SubmitActivity.class);
        startActivity(i);
      }
    });

    View settings = view.findViewById(R.id.settings);
    settings.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        Intent i = new Intent(getActivity(), SettingsActivity.class);
        Bundle b = new Bundle();
        i.putExtras(b);
        startActivityForResult(i, SettingsFragment.LOG_IN_REQUEST);
      }
    });

    View myAccount = view.findViewById(R.id.my_account);
    myAccount.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mCallback.onMyAccountSelected();
      }
    });

    View friends = view.findViewById(R.id.friends);
    friends.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mCallback.onFriendsSelected();
      }
    });

    final TextView unreadCount = (TextView) view.findViewById(R.id.unread_count);
    RedditApi.getMessages(getActivity(), Message.UNREAD, null, new FutureCallback<JsonObject>() {
      @Override
      public void onCompleted(Exception e, JsonObject result) {
        if (e != null) {
          // do something?
          return;
        }
        Gson gson = new Gson();

        // Generics are just beautiful.
        TypeToken<GenericResponseWrapper<GenericListing<Message>>> token =
            new TypeToken<GenericResponseWrapper<GenericListing<Message>>>() {
            };

        GenericResponseWrapper<GenericListing<Message>> wrapper =
            gson.fromJson(result, token.getType());
        GenericListing<Message> listing = wrapper.getData();
        ArrayList<Message> messages = new ArrayList<>();

        for (GenericResponseWrapper<Message> message : listing.getChildren()) {
          messages.add(message.getData());
        }

        unreadCount.setText(String.valueOf(messages.size()));
      }
    });
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == SettingsFragment.LOG_IN_REQUEST) {
      if (resultCode == SettingsActivity.RESULT_LOGGED_IN) {
        // Reinitialize the AccountManager, as the list of accounts is now invalid
        AccountManager.init(getActivity());
        mCallback.onAccountChanged();
        if (getView() != null) {
          // Refresh the list of accounts.
          Spinner accountSpinner = (Spinner) getView().findViewById(R.id.account_spinner);
          accountSpinner.setAdapter(new AccountAdapter());
        }
        onAccountChanged();
      }
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override
  public void onAccountChanged() {
    selectCurrentAccount(getView());
  }

  private void selectCurrentAccount(View v) {
    if (v != null) {
      final Spinner accountSpinner = (Spinner) v.findViewById(R.id.account_spinner);
      if (AccountManager.getAccount() == null) {
        accountSpinner.setSelection(AccountManager.getAccounts().size());
      } else {
        accountSpinner.post(new Runnable() {
          @Override
          public void run() {
            accountSpinner.setSelection(AccountManager.getAccounts().indexOf(AccountManager.getAccount()));
          }
        });
      }
    }
  }

  public void setSubreddit(String subreddit) {
    if (!TextUtils.isEmpty(subreddit)) {
      mCallback.onSubredditSelected(subreddit);
    }
  }

  public void setUnreadCount(int count) {
    if (getView() != null) {
      TextView unreadCount = (TextView) getView().findViewById(R.id.unread_count);
      unreadCount.setText(String.valueOf(count));
    }
  }

  @Override
  public void onDetach() {
    super.onDetach();
    mCallback = null;
  }

  public interface NavigationDrawerCallbacks {
    void onSubredditSelected(String subreddit);

    void onAccountChanged();

    void onHomeSelected();

    void onMessagesSelected();

    void onMyAccountSelected();

    void onFriendsSelected();
  }

  private class AccountAdapter extends ArrayAdapter<String> {

    public AccountAdapter() {
      super(getActivity(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
    }

    @Override
    public String getItem(int position) {
      if (position == AccountManager.getAccounts().size()) {
        return getResources().getString(R.string.logged_out);
      } else {
        return AccountManager.getAccounts().get(position).getUsername();
      }
    }

    @Override
    public int getCount() {
      return AccountManager.getAccounts().size() + 1; // Have to account for the "Not Logged In" option
    }
  }
}
