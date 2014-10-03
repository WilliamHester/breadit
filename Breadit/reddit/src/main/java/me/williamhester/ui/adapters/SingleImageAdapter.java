package me.williamhester.ui.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import me.williamhester.tools.Url;
import me.williamhester.ui.fragments.GfycatFragment;
import me.williamhester.ui.fragments.ImageFragment;

/**
 * Created by william on 6/24/14.
 */
public class SingleImageAdapter extends FragmentPagerAdapter {

    private String mUrl;
    private Url mParser;

    public SingleImageAdapter(FragmentManager fm, String imageUrl) {
        super(fm);
        mUrl = imageUrl;
        mParser = new Url(mUrl);
    }

    public SingleImageAdapter(FragmentManager fm, Url parser) {
        super(fm);
        mUrl = parser.getUrl();
        mParser = parser;
    }

    @Override
    public Fragment getItem(int position) {
        if (mUrl.contains(".gif") || mParser.getType() == Url.GFYCAT_LINK) {
            return GfycatFragment.newInstance(mUrl);
        }
        return ImageFragment.newInstance(mUrl);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
