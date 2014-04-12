package me.williamhester.reddit;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import java.io.IOException;
import java.net.MalformedURLException;

import me.williamhester.areddit.Account;

/**
 * Created by William Hester on 3/20/14.
 * This class allows the user to sign in upon the first opening of the app.
 */
public class SetupActivity extends FragmentActivity {

    private Context mContext = this;

    private Button mSave;
    private Button mSkip;
    private static EditText mUsername;
    private static EditText mPassword;

    private ViewPager mViewPager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        getActionBar().hide();

        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mSave = (Button) findViewById(R.id.save);
        mSkip = (Button) findViewById(R.id.skip);

        mSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mViewPager.getCurrentItem() == 0) {
                    mViewPager.setCurrentItem(1, true);
                } else {
                    new LoginUserTask().execute();
                }
            }
        });
        mSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle b = new Bundle();
                b.putBoolean("finishedSetup", true);
                Intent i = new Intent(mContext, MainActivity.class);
                i.putExtras(b);
                mContext.startActivity(i);
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        PagerAdapter pa = new WelcomePagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(pa);
        mViewPager.setOnPageChangeListener((ViewPager.OnPageChangeListener) pa);
    }

    private class WelcomePagerAdapter extends FragmentPagerAdapter
            implements ViewPager.OnPageChangeListener {

        public WelcomePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

        }

        @Override
        public void onPageSelected(int position) {
            if (position == 0) {
                mSave.setText(R.string.next);
            } else {
                mSave.setText(R.string.save_and_begin);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {

        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return Fragment.instantiate(mContext, SetupActivity.SetupWelcomeFragment.class.getName());
                case 1:
                    return Fragment.instantiate(mContext, SetupActivity.SetupFragment.class.getName());
            }
            return null;
        }

    }

    public static class SetupWelcomeFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_welcome, null);
        }
    }

    public static class SetupFragment extends Fragment {

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View v = inflater.inflate(R.layout.fragment_setup, null);
            mPassword = (EditText) v.findViewById(R.id.password);
            mUsername = (EditText) v.findViewById(R.id.username);
            return v;
        }

    }

    private class LoginUserTask extends AsyncTask<Void, Void, Bundle> {

        @Override
        protected Bundle doInBackground(Void... nothing) {
            Bundle b;
            try {
                b = new Bundle();
                Account account = Account.newUser(mUsername.getText().toString(),
                        mPassword.getText().toString());
                b.putParcelable("account", account);
                return b;
            } catch (MalformedURLException e) {
                Log.e("BreaditDebug", e.toString());
                return null;
            } catch (IOException e) {
                Log.e("BreaditDebug", e.toString());
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bundle b) {
            b.putBoolean("finishedSetup", true);
            Intent i = new Intent(mContext, MainActivity.class);
            i.putExtras(b);
            mContext.startActivity(i);
        }
    }

}
