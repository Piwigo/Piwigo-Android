/*
 * Piwigo for Android
 * Copyright (C) 2016-2019 Piwigo Team http://piwigo.org
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

package org.piwigo.data.model;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.io.Serializable;
import java.util.ArrayList;

public class Image implements Serializable {


    public int id;

    public String name;

    public Object comment;

    public String author;

    public int width;

    public int height;

    private MutableLiveData<String> elementUrl;
    private MutableLiveData<Iterable<Variant>> mLiveVariants;

    /**
     * dead variants is the private list if variants, that shall not be modified outside
     * modifing this datastructure will not be visible to observers of the LiveData, that's why we call it dead
     *
     * => after modification of this structure be sure to trigger a mLiveVariants update
     */
    private ArrayList<Variant> mDeadVariants;

    public Image(String elementUrl, int width, int height) {
        this.elementUrl = new MutableLiveData<>(elementUrl);
        mDeadVariants = new ArrayList<>(3);
        mDeadVariants.add(new Variant(elementUrl, width, height));
        mLiveVariants = new MutableLiveData<>(mDeadVariants);
    }

    @Override public boolean equals(Object o) {
        if (o instanceof Image) {
            return id == ((Image) o).id;
        }
        return super.equals(o);
    }

    public LiveData<Iterable<Variant>> getVariants() {
        return mLiveVariants;
    }

    /**
     * In case a better or updated image is available the URL might change
     */
    public LiveData<String> getElementUrl() {
        return elementUrl;
    }

    /**
     * this is basically the Derivative, but we do not track the full list of defined derivatives by
     * the server, but only the variants we have (or are about) to download.
     *
     * Also the full size image is considered a variant.
     */
    public class Variant {
        private Variant(String url, int width, int height){
            this.url = url;
            this.width = width;
            this.height = height;
        }

        public int width;

        public int height;

        /**
         * the place to find that variant of the image, could be file:... or https://...
         */
        public String url;
    }
}
