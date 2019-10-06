package org.piwigo.ui.photoviewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import org.piwigo.R;
import org.piwigo.io.model.ImageInfo;

import java.util.ArrayList;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class PhotoViewerDialogFragment extends DialogFragment
{
    private ArrayList<ImageInfo> images;
    private ViewPager viewPager;
    private PhotoViewerPagerAdapter pagerAdapter;
    private int selectedPosition = 0;

    @Inject
    Picasso picasso;

    public static PhotoViewerDialogFragment newInstance() {
        PhotoViewerDialogFragment f = new PhotoViewerDialogFragment();
        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_fullscreen_images, container, false);
        viewPager = v.findViewById(R.id.viewpager);
        images = (ArrayList<ImageInfo>) getArguments().getSerializable("images");
        selectedPosition = getArguments().getInt("position");
        pagerAdapter = new PhotoViewerPagerAdapter(getContext(), images);

        pagerAdapter.setPicassoInstance(picasso);
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
        setCurrentItem(selectedPosition);
        return (v);
    }

    private void setCurrentItem(int position)
    {
        viewPager.setCurrentItem(position, false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Black_NoTitleBar_Fullscreen);
    }

    @Override
    public void onDestroy() {
        selectedPosition = 0;
        super.onDestroy();
    }
}
