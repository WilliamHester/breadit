package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import com.koushikdutta.async.future.FutureCallback;

import java.util.ArrayList;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.models.GenericListing;
import me.williamhester.models.GenericResponseRedditWrapper;
import me.williamhester.models.Message;
import me.williamhester.models.Subreddit;
import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.MessageActivity;
import me.williamhester.ui.activities.SettingsActivity;
import me.williamhester.ui.activities.SubmitActivity;

/**
 * The fragment that contains the user's subreddits and info.
 */
public class NavigationDrawerFragment extends AccountFragment {

    private NavigationDrawerCallbacks mCallbacks;

    private final ArrayList<Subreddit> mSubredditList = new ArrayList<>();

    private Context mContext;
    private CheckBox mCheckbox;
    private Subreddit mSubreddit;
    private TextView mUnreadMessages;

    public static NavigationDrawerFragment newInstance() {
        return newInstance(null);
    }

    public static NavigationDrawerFragment newInstance(String subreddit) {
        Bundle args = new Bundle();
        args.putString("subreddit", subreddit);
        NavigationDrawerFragment fragment = new NavigationDrawerFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mCallbacks = (NavigationDrawerCallbacks) activity;
            mContext = activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement NavigationDrawerCallbacks.");
        }
    }

    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        final EditText subredditSearch = (EditText) v.findViewById(R.id.search_subreddit);
        final ImageButton search = (ImageButton) v.findViewById(R.id.search_button);
        ImageButton clear = (ImageButton) v.findViewById(R.id.clear);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                selectItem(subredditSearch.getText().toString().trim());
            }
        });
        subredditSearch.setImeActionLabel(getResources().getString(R.string.go),
                EditorInfo.IME_ACTION_GO);
        subredditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    selectItem(subredditSearch.getText().toString().trim().replace(" ", ""));
                }
                return false;
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subredditSearch.setText("");
            }
        });

        Spinner accountSpinner = (Spinner) v.findViewById(R.id.account_spinner);
        accountSpinner.setAdapter(new AccountAdapter());
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == AccountManager.getAccounts().size()) {
                    if (AccountManager.getAccount() != null) {
                        AccountManager.setAccount(null);
                        mCallbacks.onAccountChanged();
                        onAccountChanged();
                    }
                } else {
                    Account a = AccountManager.getAccounts().get(i);
                    if (!a.equals(AccountManager.getAccount())) {
                        AccountManager.setAccount(AccountManager.getAccounts().get(i));
                        mCallbacks.onAccountChanged();
                        onAccountChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        selectCurrentAccount(v);
        View unread = v.findViewById(R.id.messages);
        unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MessageActivity.class);
                startActivity(i);
            }
        });
        mUnreadMessages = (TextView) v.findViewById(R.id.unread_count);
        mUnreadMessages.setText("0 " + getResources().getQuantityString(R.plurals.new_messages, 0));

        RedditApi.getMessages(getActivity(), Message.UNREAD, null, mUnreadCallback);

        View submit = v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SubmitActivity.class);
                startActivity(i);
            }
        });

        View settings = v.findViewById(R.id.preferences);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                Bundle b = new Bundle();
                i.putExtras(b);
                startActivityForResult(i, SettingsFragment.LOG_IN_REQUEST);
            }
        });
        mCheckbox = (CheckBox) v.findViewById(R.id.subscribed_checkbox);
        mCheckbox.setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SettingsFragment.LOG_IN_REQUEST) {
            if (resultCode == SettingsActivity.RESULT_LOGGED_IN) {
                // Reinitialize the AccountManager, as the list of accounts is now invalid
                AccountManager.init(getActivity());
                mCallbacks.onAccountChanged();
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

    private View createHeaderView(LayoutInflater inflater) {
        View v = inflater.inflate(R.layout.fragment_navigation_drawer, null);
        final EditText subredditSearch = (EditText) v.findViewById(R.id.search_subreddit);
        final ImageButton search = (ImageButton) v.findViewById(R.id.search_button);
        ImageButton clear = (ImageButton) v.findViewById(R.id.clear);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                selectItem(subredditSearch.getText().toString().trim());
            }
        });
        subredditSearch.setImeActionLabel(getResources().getString(R.string.go),
                EditorInfo.IME_ACTION_GO);
        subredditSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_GO) {
                    InputMethodManager imm = (InputMethodManager) mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
                    selectItem(subredditSearch.getText().toString().trim().replace(" ", ""));
                }
                return false;
            }
        });
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                subredditSearch.setText("");
            }
        });

        Spinner accountSpinner = (Spinner) v.findViewById(R.id.account_spinner);
        accountSpinner.setAdapter(new AccountAdapter());
        accountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == AccountManager.getAccounts().size()) {
                    if (AccountManager.getAccount() != null) {
                        AccountManager.setAccount(null);
                        mCallbacks.onAccountChanged();
                        onAccountChanged();
                    }
                } else {
                    Account a = AccountManager.getAccounts().get(i);
                    if (!a.equals(AccountManager.getAccount())) {
                        AccountManager.setAccount(AccountManager.getAccounts().get(i));
                        mCallbacks.onAccountChanged();
                        onAccountChanged();
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        selectCurrentAccount(v);
        View unread = v.findViewById(R.id.messages);
        unread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), MessageActivity.class);
                startActivity(i);
            }
        });
        mUnreadMessages = (TextView) v.findViewById(R.id.unread_count);
        mUnreadMessages.setText("0 " + getResources().getQuantityString(R.plurals.new_messages, 0));

        RedditApi.getMessages(getActivity(), Message.UNREAD, null, mUnreadCallback);

        View submit = v.findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), SubmitActivity.class);
                startActivity(i);
            }
        });

        View settings = v.findViewById(R.id.preferences);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getActivity(), SettingsActivity.class);
                Bundle b = new Bundle();
                i.putExtras(b);
                startActivityForResult(i, SettingsFragment.LOG_IN_REQUEST);
            }
        });
        mCheckbox = (CheckBox) v.findViewById(R.id.subscribed_checkbox);
        mCheckbox.setVisibility(View.GONE);
        return v;
    }

    @Override
    public void onAccountChanged() {
        super.onAccountChanged();
        selectCurrentAccount(getView());
        mCheckbox.setVisibility(mAccount == null || mSubreddit == null ? View.GONE : View.VISIBLE);
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

    private void selectItem(String subreddit) {
        if (mCallbacks != null) {
            mCallbacks.onSubredditSelected(subreddit);
        }
    }

    public void setSubreddit(String subreddit) {
        mCheckbox.setVisibility(View.GONE);
        if (TextUtils.isEmpty(subreddit)) {
            mSubreddit = Subreddit.FRONT_PAGE;
        } else {
            mCallbacks.onSubredditSelected(subreddit);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    private FutureCallback<JsonObject> mUnreadCallback = new FutureCallback<JsonObject>() {
        @Override
        public void onCompleted(Exception e, JsonObject result) {
            if (e != null) {
                // do something?
                return;
            }
            Gson gson = new Gson();

            // Generics are just beautiful.
            TypeToken<GenericResponseRedditWrapper<GenericListing<Message>>> token =
                    new TypeToken<GenericResponseRedditWrapper<GenericListing<Message>>>() {
                    };

            GenericResponseRedditWrapper<GenericListing<Message>> wrapper =
                    gson.fromJson(result, token.getType());
            GenericListing<Message> listing = wrapper.getData();
            ArrayList<Message> messages = new ArrayList<>();

            for (GenericResponseRedditWrapper<Message> message : listing.getChildren()) {
                messages.add(message.getData());
            }

            mUnreadMessages.setText(messages.size() + " "
                    + getResources().getQuantityString(R.plurals.new_messages, messages.size()));
        }
    };

    public static interface NavigationDrawerCallbacks {
        public void onSubredditSelected(String subreddit);
        public void onAccountChanged();
    }

    private class AccountAdapter extends ArrayAdapter<String> {

        public AccountAdapter() {
            super(mContext, android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
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
