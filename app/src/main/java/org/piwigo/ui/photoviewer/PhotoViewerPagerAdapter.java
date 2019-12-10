package org.piwigo.ui.photoviewer;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;
import com.squareup.picasso.Picasso;

import org.piwigo.R;
import org.piwigo.data.model.Image;

import java.util.List;

public class PhotoViewerPagerAdapter extends PagerAdapter {

    private Context context;
    private List<Image> images;
    private LayoutInflater inflater;
    private Picasso picasso;

    public PhotoViewerPagerAdapter(Context context, List<Image> images)
    {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup container, int position)
    {
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.item_fullscreen_image, container, false);
        TouchImageView imageViewPreview = view.findViewById(R.id.imgDisplay);
        Image image = images.get(position);

        // TODO: trigger URL updates to get needed resolution and handle LiveData updates
        picasso.load(image.elementUrl).noFade().into(imageViewPreview);
        container.addView(view);
        return view;
    }

    @Override
    public int getCount() {
        return (images.size());
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object obj) {
        return (view == ((View) obj));
    }

    @Override
    public void destroyItem(ViewGroup container, int position, @NonNull Object object) {
        container.removeView((View) object);
    }

    void setPicassoInstance(Picasso picasso)
    {
        this.picasso = picasso;
    }
}