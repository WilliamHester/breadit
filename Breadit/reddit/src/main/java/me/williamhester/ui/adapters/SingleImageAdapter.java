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
import me.williamhester.tools.UrlParser;
import me.williamhester.ui.fragments.GfycatFragment;
import me.williamhester.ui.fragments.ImageFragment;

/**
 * Created by william on 6/24/14.
 */
public class SingleImageAdapter extends FragmentPagerAdapter {

    private String mUrl;
    private UrlParser mParser;

    public SingleImageAdapter(FragmentManager fm, String imageUrl) {
        super(fm);
        mUrl = imageUrl;
        mParser = new UrlParser(mUrl);
    }

    public SingleImageAdapter(FragmentManager fm, UrlParser parser) {
        super(fm);
        mUrl = parser.getUrl();
        mParser = parser;
    }

    @Override
    public Fragment getItem(int position) {
        if (mUrl.contains(".gif") || mParser.getType() == UrlParser.GFYCAT_LINK) {
            return GfycatFragment.newInstance(mUrl);
        }
        return ImageFragment.newInstance(mUrl);
    }

    @Override
    public int getCount() {
        return 1;
    }
}
