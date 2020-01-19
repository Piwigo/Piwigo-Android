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

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.Relation;

import java.io.Serializable;

import static androidx.room.ForeignKey.CASCADE;

@Entity(indices = {@Index(value = {"url"},
        unique = true)})
/**
 * This is basically a derivative, but we store here as variant only those which are locally stored
 * but it's not guaranteed that they will stay here.
 */
public class ImageVariant implements Serializable {
    public ImageVariant(int imageId, int width, int height, String storageLocation, String lastModified, String url){
        this.imageId = imageId;
        this.width = width;
        this.height = height;
        this.storageLocation = storageLocation;
        this.lastModified = lastModified;
        this.url = url;
    }

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ForeignKey
            (entity = Image.class,
                    parentColumns = "id",
                    childColumns = "imageId",
                    onDelete = CASCADE)
    public int imageId;

    public int height;
    public int width;

    public String storageLocation;
    public String lastModified;
    public String url;
}

