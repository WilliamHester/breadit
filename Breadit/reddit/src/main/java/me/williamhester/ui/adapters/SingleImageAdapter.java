package me.williamhester.ui.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v13.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.ui.fragments.GfycatFragment;
import me.williamhester.ui.fragments.ImageFragment;

/**
 * Created by william on 6/24/14.
 */
public class SingleImageAdapter extends FragmentPagerAdapter {

    private FragmentManager mFragmentManager;
    private String mUrl;

    public SingleImageAdapter(FragmentManager fm, String imageUrl) {
        super(fm);
        mFragmentManager = fm;
        mUrl = imageUrl;
    }

    @Override
    public Fragment getItem(int position) {
        if (mUrl.contains(".gif")) {
            return GfycatFragment.newInstance(mUrl);
        }
        return ImageFragment.newInstance(mUrl);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
