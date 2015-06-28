package me.williamhester.ui.adapters;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.Fragment;

import me.williamhester.tools.Url;
import me.williamhester.ui.fragments.GifFragment;
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
        if (mUrl.contains(".gif") || mParser.getType() == Url.DIRECT_GFY
                || mParser.getType() == Url.GFYCAT_LINK) {
            return GifFragment.newInstance(mUrl);
        }
        return ImageFragment.newInstance(mUrl);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
