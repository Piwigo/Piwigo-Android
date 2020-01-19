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

package org.piwigo.ui.main;

import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import android.content.Context;
import android.content.ContextWrapper;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import org.piwigo.R;

public class AlbumItemViewModel extends ViewModel {
    public final ObservableField<String> url = new ObservableField<String>();
    private final String title;
    private final String photos;
    private final Integer catId;

    AlbumItemViewModel(String url, String title, String photos, Integer categoryId) {
        this.url.set(url);
        this.title = title;
        this.photos = photos;
        this.catId = categoryId;
    }

    public String getTitle() {
        return title;
    }

    public String getPhotos() {
        return photos;
    }

    public Integer getCatId() { return catId;}

    public void onClickDo(View v){
        Context ctx = v.getContext();
        while (ctx instanceof ContextWrapper
                && !(ctx instanceof AppCompatActivity)) {
            ctx = ((ContextWrapper)ctx).getBaseContext();
        }

        MainActivity mainActivity = (MainActivity)ctx;

        if (mainActivity != null)
            mainActivity.refreshFAB(catId);

        if(ctx instanceof AppCompatActivity) {
            Bundle bndl = new Bundle();
            bndl.putInt("Category", getCatId());
            bndl.putString("Title", getTitle());
            AlbumsFragment frag = new AlbumsFragment();
            frag.setArguments(bndl);
            ((AppCompatActivity) ctx).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, frag)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
