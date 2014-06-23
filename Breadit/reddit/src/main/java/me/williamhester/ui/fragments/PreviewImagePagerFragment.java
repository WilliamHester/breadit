package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;

/**
 * Created by william on 6/22/14.
 */
public class PreviewImagePagerFragment extends Fragment {

    private ArrayList<ImgurImage> mImages;

    public static PreviewImagePagerFragment newInstance(ImgurImage image) {
        return new PreviewImagePagerFragment();
    }

    public static PreviewImagePagerFragment newInstance(ImgurAlbum album) {
        return new PreviewImagePagerFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        return null;
    }

    private class ImagePagerAdapter extends FragmentPagerAdapter {

        public ImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return mImages == null ? 0 : mImages.size();
        }

        @Override
        public Fragment getItem(int position) {
            return null;
        }
    }

}
