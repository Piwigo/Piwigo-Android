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
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import org.piwigo.data.model.Image;

import java.util.List;

import io.reactivex.Single;

@Dao
abstract public class ImageDao {
    @Insert
    abstract public void insert(Image image);

    @Delete
    abstract public void delete(Image image);

    @Update
    abstract public int update(Image image);

    @Transaction
    public void upsert(Image image) {
        int id = update(image);
        if (id == -1) {
            insert(image);
        }
    }

    @Query("SELECT * FROM Image")
    abstract public Single<List<Image>> getImages();

    @Query("SELECT Image.* FROM Image INNER JOIN ImageCategoryMap ON Image.id=ImageCategoryMap.imageId WHERE ImageCategoryMap.categoryId=:categoryId")
    abstract public Single<List<Image>> getImagesInCategory(int categoryId);

}
