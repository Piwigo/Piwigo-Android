/*
 * Copyright 2017-2018 Jeff Ayers
 * Copyright 2017-2018 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.main;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import android.view.View;

import org.piwigo.io.model.ImageInfo;
import org.piwigo.ui.photoviewer.PhotoViewerDialogFragment;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModel;

public class ImagesItemViewModel extends ViewModel {

    private final String url;
    private final int imageId;
    private final String title;

    private final ArrayList<ImageInfo> images;

    public ImagesItemViewModel(String url, int imageId, String title, ArrayList<ImageInfo> images) {
        this.url = url;
        this.imageId = imageId;
        this.title = title;
        this.images = images;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public int getImageId()
    {
        return (imageId);
    }

    public void onClickDo(View v)
    {
        Context ctx = v.getContext();
        while (ctx instanceof ContextWrapper
                && !(ctx instanceof AppCompatActivity)) {
            ctx = ((ContextWrapper)ctx).getBaseContext();
        }

        //TODO Maybe find a better way to do this (note that AppCompatActivity doesn't work on SDK 16)
        MainActivity mainActivity = (MainActivity)ctx;
        if(mainActivity != null)
        {
            Bundle bundle = new Bundle();
            bundle.putSerializable("images", images);
            bundle.putInt("position", getImageId());

            FragmentTransaction ft = mainActivity.getSupportFragmentManager().beginTransaction();
            PhotoViewerDialogFragment newFragment = PhotoViewerDialogFragment.newInstance();
            newFragment.setArguments(bundle);
            newFragment.show(ft, "PhotoViewer");
        }
    }

}
