package me.williamhester.ui.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import me.williamhester.models.Account;
import me.williamhester.models.AccountManager;
import me.williamhester.reddit.R;
import me.williamhester.ui.activities.SettingsActivity;
import me.williamhester.ui.activities.SubmitActivity;

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
        View v = inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
        final EditText subredditSearch = (EditText) v.findViewById(R.id.search_subreddit);
        final ImageButton search = (ImageButton) v.findViewById(R.id.search_button);
        ImageButton clear = (ImageButton) v.findViewById(R.id.clear);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
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
                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
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
        selectCurrentAccount(v);

        final TextView home = (TextView) v.findViewById(R.id.home);
        final TextView messages = (TextView) v.findViewById(R.id.inbox);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.setTypeface(Typeface.SANS_SERIF);
                home.setTypeface(home.getTypeface(), Typeface.BOLD);
                mCallback.onHomeSelected();
            }
        });
        messages.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages.setTypeface(messages.getTypeface(), Typeface.BOLD);
                home.setTypeface(Typeface.SANS_SERIF);
                mCallback.onMessagesSelected();
            }
        });

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
        return v;
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
        super.onAccountChanged();
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

    private void selectItem(String subreddit) {
        if (mCallback != null) {
            mCallback.onSubredditSelected(subreddit);
        }
    }

    public void setSubreddit(String subreddit) {
        if (!TextUtils.isEmpty(subreddit)) {
            mCallback.onSubredditSelected(subreddit);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    public static interface NavigationDrawerCallbacks {
        public void onSubredditSelected(String subreddit);
        public void onAccountChanged();
        public void onHomeSelected();
        public void onMessagesSelected();
        public void onSubmitSelected();
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
