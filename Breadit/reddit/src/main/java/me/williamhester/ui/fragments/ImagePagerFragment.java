package me.williamhester.ui.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.koushikdutta.async.future.FutureCallback;

import me.williamhester.models.ImgurAlbum;
import me.williamhester.models.ImgurImage;
import me.williamhester.models.ResponseImgurWrapper;
import me.williamhester.reddit.R;

/**
 * Created by william on 6/24/14.
 */
public class ImagePagerFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup root, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_image_pager, root, false);
        ViewPager pager = (ViewPager) v.findViewById(R.id.view_pager);
        return v;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // Do the network calls
    }

    private class ImgurImageCallback implements FutureCallback<ResponseImgurWrapper<ImgurImage>> {
        @Override
        public void onCompleted(Exception e, ResponseImgurWrapper<ImgurImage> result) {

        }
    }

    private class ImgurAlbumCallback implements FutureCallback<ResponseImgurWrapper<ImgurAlbum>> {
        @Override
        public void onCompleted(Exception e, ResponseImgurWrapper<ImgurAlbum> result) {

        }
    }

}
