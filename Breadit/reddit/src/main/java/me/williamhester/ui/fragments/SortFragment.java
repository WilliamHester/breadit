package me.williamhester.ui.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import me.williamhester.network.RedditApi;
import me.williamhester.reddit.R;

/**
 * Created by william on 9/4/14.
 */
public class SortFragment extends Fragment {

    private SortFragmentCallback mCallback;

    public static SortFragment newInstance() {
        return new SortFragment();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        if (activity instanceof SortFragmentCallback) {
            mCallback = (SortFragmentCallback) activity;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sort, root, false);
        Button hot = (Button) view.findViewById(R.id.hot_sort);
        Button newButton = (Button) view.findViewById(R.id.new_sort);
        Button rising = (Button) view.findViewById(R.id.rising_sort);
        Button controversial = (Button) view.findViewById(R.id.controversial_sort);
        Button top = (Button) view.findViewById(R.id.top_sort);

        hot.setTag(RedditApi.SORT_TYPE_HOT);
        newButton.setTag(RedditApi.SORT_TYPE_NEW);
        rising.setTag(RedditApi.SORT_TYPE_RISING);
        controversial.setTag(RedditApi.SORT_TYPE_CONTROVERSIAL);
        top.setTag(RedditApi.SORT_TYPE_TOP);

        hot.setOnClickListener(mPrimarySortClickListener);
        newButton.setOnClickListener(mPrimarySortClickListener);
        rising.setOnClickListener(mPrimarySortClickListener);
        controversial.setOnClickListener(mPrimarySortClickListener);
        top.setOnClickListener(mPrimarySortClickListener);

        Button hour = (Button) view.findViewById(R.id.hour_sort);
        Button day = (Button) view.findViewById(R.id.day_sort);
        Button week = (Button) view.findViewById(R.id.week_sort);
        Button month = (Button) view.findViewById(R.id.month_sort);
        Button year = (Button) view.findViewById(R.id.year_sort);
        Button all = (Button) view.findViewById(R.id.all_sort);

        hour.setTag(RedditApi.SECONDARY_SORT_HOUR);
        day.setTag(RedditApi.SECONDARY_SORT_DAY);
        week.setTag(RedditApi.SECONDARY_SORT_WEEK);
        month.setTag(RedditApi.SECONDARY_SORT_MONTH);
        year.setTag(RedditApi.SECONDARY_SORT_YEAR);
        all.setTag(RedditApi.SECONDARY_SORT_ALL);

        hour.setOnClickListener(mSecondarySortClickListener);
        day.setOnClickListener(mSecondarySortClickListener);
        week.setOnClickListener(mSecondarySortClickListener);
        month.setOnClickListener(mSecondarySortClickListener);
        year.setOnClickListener(mSecondarySortClickListener);
        all.setOnClickListener(mSecondarySortClickListener);

        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mCallback.onCancel();
            }
        });

        return view;
    }

    private final View.OnClickListener mPrimarySortClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tag = (String) view.getTag();
            if (tag.equals(RedditApi.SORT_TYPE_TOP) || tag.equals(RedditApi.SORT_TYPE_CONTROVERSIAL)) {

            }
            mCallback.onPrimarySortSelected(tag);
        }
    };

    private final View.OnClickListener mSecondarySortClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String tag = (String) view.getTag();
            mCallback.onSecondarySortSelected(tag);
        }
    };

    public interface SortFragmentCallback {
        public void onPrimarySortSelected(String sort);
        public void onSecondarySortSelected(String sort);
        public void onCancel();
    }

}
