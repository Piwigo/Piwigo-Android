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
import androidx.room.Query;
import androidx.room.Transaction;
import androidx.room.Update;

import org.piwigo.data.model.Category;

import java.util.List;

import io.reactivex.Single;

@Dao
public abstract class CategoryDao {

    @Insert
    public abstract void insert(Category category) throws SQLException;

    @Delete
    public abstract void delete(Category category) throws SQLException;

    @Update
    public abstract int update(Category category) throws SQLException;

    @Transaction
    public void upsert(Category category) throws SQLException{
        int id = update(category);
        if (id == 0) {
            insert(category);
        }
    }

    public Single<List<Category>> getCategoriesIn(int categoryId) throws SQLException{
        if(categoryId == 0){
            return getRootCategoriesRaw();
        }else{
            return getCategoriesRaw(categoryId);
        }
    }

    @Query("WITH RECURSIVE parents(id, name, lvl, parentCatId) AS (" +
            "SELECT id, name, 0 as lvl, parentCatId FROM Category " +
            "UNION ALL " +
            "SELECT CC.id, CP.name, lvl + 1 as lvl, CP.parentCatId FROM parents CC INNER JOIN Category CP ON CP.id = CC.parentCatId" +
            ")" +
            "SELECT name from parents WHERE id = :categoryId ORDER BY lvl DESC" +
            "")
    public abstract Single<List<String>> getCategoryPath(Integer categoryId) throws SQLException;

    // TODO: #90 implement sorting by adding a parameter to give the sorting order
    @Query("SELECT * FROM Category WHERE parentCatId = :categoryId ORDER BY globalRank")
    public abstract Single<List<Category>> getCategoriesRaw(Integer categoryId) throws SQLException;

    @Query("SELECT * FROM Category WHERE parentCatId IS NULL ORDER BY globalRank")
    public abstract Single<List<Category>> getRootCategoriesRaw() throws SQLException;

    @Query("SELECT * FROM Category")
    public abstract Single<List<Category>> getAllCategories() throws SQLException;
}
