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

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import static androidx.room.ForeignKey.SET_NULL;

@Entity(
        indices = {@Index("parentCatId")},
        foreignKeys = {
                @ForeignKey(
                        entity = Category.class,
                        parentColumns = "id",
                        childColumns = "parentCatId",
                        onDelete = SET_NULL
                )}
)
public class Category {

    @PrimaryKey
    public int id;

    public String name;

    public String comment;

    public String globalRank;

    public int nbImages;

    @Nullable
    public Integer parentCatId;

    public int totalNbImages;

    public int representativePictureId;

    public int nbCategories;

    public String thumbnailUrl;

    @Override public boolean equals(Object o) {
        if (o instanceof Category) {
            return id == ((Category) o).id;
        }
        return super.equals(o);
    }

}