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

/**
 * represents an item (photo, album, video, ...) in the gallery, at a defined position in the current result
 */
public class PositionedItem<T> {
    private final int position;
    private final T item;
    private boolean updateNeeded;

    public PositionedItem(int position, T item, boolean updateNeeded){
        this.position = position;
        this.item = item;
        this.updateNeeded = updateNeeded;
    }

    public int getPosition() {
        return position;
    }

    public T getItem() {
        return item;
    }

    public boolean isUpdateNeeded(){
        return updateNeeded;
    }
}
