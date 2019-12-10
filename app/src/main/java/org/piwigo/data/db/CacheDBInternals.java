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

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.TypeConverter;

import org.piwigo.data.model.Category;
import org.piwigo.data.model.Image;

import java.util.Date;

/**
 * Class to hold some internal elements that are not intended for the direct use in tha application
 */
public class CacheDBInternals {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }

    @Entity(
            primaryKeys = {"categoryId", "imageId"},
            indices = {@Index("categoryId"), @Index("imageId")},
            foreignKeys = {
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "id",
                        childColumns = "categoryId",
                        onDelete = ForeignKey.CASCADE
                ),
                @ForeignKey(
                        entity = Image.class,
                        parentColumns = "id",
                        childColumns = "imageId",
                        onDelete = ForeignKey.CASCADE
                )}
            )

    public static class ImageCategoryMap {
        public ImageCategoryMap(final int categoryId, final int imageId) {
            this.categoryId = categoryId;
            this.imageId = imageId;
        }
        public final int categoryId;
        public final int imageId;
    }

}
