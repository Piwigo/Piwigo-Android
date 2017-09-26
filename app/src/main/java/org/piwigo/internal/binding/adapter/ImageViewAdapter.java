/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.piwigo.R;

public class ImageViewAdapter {

    @BindingAdapter("heightRatio") public static void bindHeighRatio(ImageView imageView, double ratio) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.height = (int) (imageView.getMeasuredWidth() * ratio);
        imageView.setLayoutParams(params);
    }

    @BindingAdapter("srcUrl") public static void bindImageUrl(ImageView imageView, String url) {
        if(url != null) {
            /* a real URL for the iamge is available */
            Picasso.with(imageView.getContext())
                    .load(url)
                    .fit()
                    .centerCrop()
                    .into(imageView);
        }else{
            /* put the default image for empty albums */
            imageView.setImageResource(R.drawable.piwigo_logo);
        }
    }

}
