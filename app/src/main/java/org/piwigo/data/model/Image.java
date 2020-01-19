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

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Date;

@Entity
public class Image implements Serializable {

    @PrimaryKey
    public int id;

    public String file;

    public String name;

    public String description;

    public String author;

    public int width;

    public int height;

    public Date creationDate;

    public Date availableDate;

    public String elementUrl;

    public Image(String elementUrl) {
        this.elementUrl = elementUrl;
    }

    @Override public boolean equals(Object o) {
        if (o instanceof Image) {
            return id == ((Image) o).id;
        }
        return super.equals(o);
    }

}
