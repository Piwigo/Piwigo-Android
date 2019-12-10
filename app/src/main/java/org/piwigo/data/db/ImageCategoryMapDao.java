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

import android.database.SQLException;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import org.piwigo.data.model.Category;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class ImageCategoryMapDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract public long rawInsert(CacheDBInternals.ImageCategoryMap join) throws SQLException;

    public void insert(List<CacheDBInternals.ImageCategoryMap> joins) throws SQLException {
        for(CacheDBInternals.ImageCategoryMap join: joins){

            List<CacheDBInternals.ImageCategoryMap> a = getImagesCategoryMap(join.categoryId, join.imageId);
            if(a.size() == 0) {
                rawInsert(join);
            }
        }
    }


    @Query("SELECT * FROM Category")
    abstract List<Category> getAllCategories() throws SQLException;

    @Query("SELECT * FROM ImageCategoryMap WHERE categoryId=:categoryId AND imageId=:imageId")
    abstract public List<CacheDBInternals.ImageCategoryMap> getImagesCategoryMap(int categoryId, int imageId) throws SQLException;

    @Delete()
    abstract public void delete(List<CacheDBInternals.ImageCategoryMap> join) throws SQLException;
}
