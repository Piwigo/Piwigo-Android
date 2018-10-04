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

import android.arch.lifecycle.ViewModel;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import org.piwigo.R;
import org.piwigo.helper.CommonVars;

public class AlbumItemViewModel extends ViewModel {

    private final String url;
    private final String title;
    private final String photos;
    private final Integer catid;

    CommonVars comvars = CommonVars.getInstance();

    AlbumItemViewModel(String url, String title, String photos, Integer CategoryId) {
        this.url = url;
        this.title = title;
        this.photos = photos;
        this.catid = CategoryId;
    }

    public String getUrl() {
        return url;
    }

    public String getTitle() {
        return title;
    }

    public String getPhotos() {
        return photos;
    }

    public Integer getCatId() { return catid;}

    public void onclickdo(View v){
        comvars.setValue(catid);
        if(v.getContext() instanceof AppCompatActivity) {
            ((AppCompatActivity) v.getContext()).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.content, new ImagesFragment())
                    .addToBackStack(null)
                    .commit();
        }

    }
}
