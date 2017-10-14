/*
 * Piwigo for Android
 * Copyright (C) 2016-2017 Piwigo Team http://piwigo.org
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

package org.piwigo.internal.binding.observable;

import android.databinding.BaseObservable;

public class NavigationItemObservable extends BaseObservable {

    private int itemId;

    public NavigationItemObservable() {}

    public NavigationItemObservable(int itemId) {
        this.itemId = itemId;
    }

    public void set(int itemId) {
        if (this.itemId != itemId) {
            this.itemId = itemId;
            notifyChange();
        }
    }

    public int get() {
        return itemId;
    }

}
