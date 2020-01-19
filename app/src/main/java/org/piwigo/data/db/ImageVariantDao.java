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

package org.piwigo.data.db;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import org.piwigo.data.model.ImageVariant;
import org.piwigo.data.model.VariantWithImage;

import java.util.List;
import java.util.Map;

import io.reactivex.Single;

@Dao
public abstract class ImageVariantDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insert(ImageVariant variant);

    /* could also use Image.variants */
    @Query("SELECT * FROM ImageVariant WHERE imageId = :imageId AND url = :url")
    public abstract Single<List<ImageVariant>> variantsForImage(Integer imageId, String url);

    @Query("SELECT ImageVariant.imageId imageId, imageVariant.url url, ImageVariant.lastModified lastModified, imageVariant.storageLocation storageLocation FROM ImageVariant INNER JOIN ImageCategoryMap ON ImageCategoryMap.imageId = ImageVariant.imageId WHERE categoryId = :categoryId")
    public abstract Single<List<VariantInfo>> variantsInCategory(Integer categoryId);

    @Transaction
    @Query("SELECT * FROM ImageVariant WHERE imageId = :imageId")
    public abstract List<VariantWithImage> getVariantsWithImage(Integer imageId);

    @Transaction
    @Query("SELECT * FROM ImageVariant INNER JOIN ImageCategoryMap ON ImageCategoryMap.imageId = ImageVariant.imageId WHERE categoryId = :categoryId")
    public abstract Single<List<VariantWithImage>> getVariantsWithImageInCategory(Integer categoryId);

    public class VariantInfo{
        public int imageId;
        public String url;
        public String lastModified;
        public String storageLocation;
    }
}
