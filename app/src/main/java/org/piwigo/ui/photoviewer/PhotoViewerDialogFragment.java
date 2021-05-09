package org.piwigo.ui.photoviewer;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.databinding.ObservableArrayList;
import androidx.databinding.ObservableBoolean;
import androidx.fragment.app.DialogFragment;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.Picasso;

import org.piwigo.R;
import org.piwigo.data.model.Image;
import org.piwigo.data.model.PositionedItem;
import org.piwigo.data.model.VariantWithImage;
import org.piwigo.data.repository.ImageRepository;
import org.piwigo.ui.main.AlbumsViewModel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableObserver;

public class PhotoViewerDialogFragment extends DialogFragment
{
    public ObservableArrayList<VariantWithImage> images = new ObservableArrayList<>();
    private ViewPager viewPager;
    private PhotoViewerPagerAdapter pagerAdapter;
    private int selectedPosition = 0;

    @Inject
    Picasso picasso;

    @Inject ImageRepository imageRepository;

    public static PhotoViewerDialogFragment newInstance() {
        PhotoViewerDialogFragment f = new PhotoViewerDialogFragment();
        return (f);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_fullscreen_images, container, false);
        viewPager = v.findViewById(R.id.viewpager);
        imageRepository.getImages(getArguments().getInt("categoryID"))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new ImageSubscriber());

        selectedPosition = getArguments().getInt("position");
        pagerAdapter = new PhotoViewerPagerAdapter(getContext(), images);
        synchronized (pagerAdapter) {
            pagerAdapter.setPicassoInstance(picasso);
            viewPager.setAdapter(pagerAdapter);
            viewPager.setOffscreenPageLimit(3);
        }
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

    private boolean isLoadingImages = false;
    public ObservableBoolean isLoading = new ObservableBoolean();

    private class ImageSubscriber extends DisposableObserver<PositionedItem<VariantWithImage>> {
        public ImageSubscriber(){
            super();
            isLoadingImages = true;
        }

        @Override
        public void onNext(PositionedItem<VariantWithImage> item) {
            synchronized (pagerAdapter) {
                if (images.size() == item.getPosition()) {
                    images.add(item.getItem());
                } else {
                    while (images.size() <= item.getPosition()) {
                        images.add(null);
                    }
                    images.set(item.getPosition(), item.getItem());
                }
                pagerAdapter.notifyDataSetChanged();
                if (item.getPosition() == selectedPosition) {
                    setCurrentItem(selectedPosition);
                }
            }
        }

        @Override
        public void onComplete() {
            isLoadingImages = false;
        }

        @Override
        public void onError(Throwable e) {
            isLoadingImages = false;
            // TODO handle errors
            e.printStackTrace();
        }
    }

}
