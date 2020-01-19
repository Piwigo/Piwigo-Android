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

import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import org.piwigo.data.model.Category;
import org.piwigo.data.model.Image;
import org.piwigo.data.model.ImageVariant;

@Database(entities =
        {
            Image.class,
            ImageVariant.class,
            Category.class,
            CacheDBInternals.ImageCategoryMap.class
        },
        version = 1)

@TypeConverters({CacheDBInternals.class})
public abstract class CacheDatabase extends RoomDatabase {
    public abstract ImageDao imageDao();
    public abstract ImageVariantDao variantDao();
    public abstract CategoryDao categoryDao();
    public abstract ImageCategoryMapDao imageCategoryMapDao();
}
