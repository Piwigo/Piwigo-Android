/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.internal.binding.adapter;

import androidx.databinding.BindingAdapter;
import android.view.ViewGroup;
import android.widget.ImageView;

import  com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;

import org.piwigo.R;

public class ImageViewBindingAdapter {



    public ImageViewBindingAdapter() {

    }

    @BindingAdapter("android:src") public void loadImage(ImageView imageView, String url) {

        Glide.with(imageView)
                .load(url)
                .placeholder(R.drawable.ic_downloading_placeholder)
                //.override(400)
                .fitCenter()
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(imageView);
    }

    @BindingAdapter("heightRatio") public void setHeighRatio(ImageView imageView, double ratio) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = (int) (imageView.getMeasuredWidth() * ratio);
        imageView.setLayoutParams(params);
    }
}
