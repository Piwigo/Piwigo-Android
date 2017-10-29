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

import android.databinding.BindingAdapter;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

public class ImageViewBindingAdapter {

    private final Picasso picasso;

    public ImageViewBindingAdapter(Picasso picasso) {
        this.picasso = picasso;
    }

    @BindingAdapter("android:src") public void loadImage(ImageView imageView, String url) {
        picasso.load(url)
                .fit()
                .centerCrop()
                .into(imageView);
    }

    @BindingAdapter("heightRatio") public void setHeighRatio(ImageView imageView, double ratio) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = (int) (imageView.getMeasuredWidth() * ratio);
        imageView.setLayoutParams(params);
    }
}
