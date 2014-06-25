package me.williamhester.ui.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.ui.fragments.GfycatFragment;
import me.williamhester.ui.fragments.ImgurImageFragment;

/**
 * Created by william on 6/24/14.
 */
public class ImgurAlbumAdapter extends FragmentPagerAdapter {

    private List<ImgurImage> mImages;

    public ImgurAlbumAdapter(FragmentManager fm, ImgurAlbum album) {
        super(fm);
        mImages = album.getImages();
    }

    public ImgurAlbumAdapter(FragmentManager fm, ImgurImage image) {
        super(fm);
        mImages = new ArrayList<ImgurImage>();
        mImages.add(image);
    }

    @Override
    public Fragment getItem(int position) {
        if (mImages.get(position).isAnimated()) {
            return GfycatFragment.newInstance(mImages.get(position).getUrl());
        }
        return ImgurImageFragment.newInstance(mImages.get(position).getUrl());
    }

    @Override
    public int getCount() {
        return mImages.size();
    }
}
