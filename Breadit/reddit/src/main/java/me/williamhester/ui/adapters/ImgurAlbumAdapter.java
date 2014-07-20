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
import me.williamhester.ui.fragments.ImgurImageFragment;

/**
 * Created by william on 6/24/14.
 */
public class ImgurAlbumAdapter extends FragmentPagerAdapter {

    private List<ImgurImage> mImages;
    private FragmentManager mFragmentManager;

    public ImgurAlbumAdapter(FragmentManager fm, ImgurAlbum album) {
        super(fm);
        mImages = album.getImages();
        mFragmentManager = fm;
    }

    public ImgurAlbumAdapter(FragmentManager fm, ImgurImage image) {
        super(fm);
        mImages = new ArrayList<>();
        mImages.add(image);
        mFragmentManager = fm;
    }

    @Override
    public Object instantiateItem(ViewGroup viewGroup, int position) {
        mFragmentManager.executePendingTransactions();
        FragmentTransaction ft = mFragmentManager.beginTransaction();

        // Do we already have this fragment?
        Fragment fragment = mFragmentManager.findFragmentByTag("IMAGE_" + mImages.get(position).getId());
        if (fragment != null) {
            ft.attach(fragment);
        } else {
            fragment = getItem(position);
            ft.add(viewGroup.getId(), fragment, "IMAGE_" + mImages.get(position).getId());
        }
        ft.commitAllowingStateLoss();
        return fragment;
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
