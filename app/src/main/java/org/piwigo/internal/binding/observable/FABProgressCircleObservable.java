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

public class FABProgressCircleObservable extends BaseObservable {

    public static final int STATE_HIDDEN = 0;
    public static final int STATE_VISIBLE = 1;
    public static final int STATE_FINAL = 2;

    private int state = STATE_HIDDEN;

    public FABProgressCircleObservable() {}

    public FABProgressCircleObservable(int initalState) {
        state = initalState;
    }

    public void show() {
        if (state != STATE_FINAL && state != STATE_VISIBLE) {
            state = STATE_VISIBLE;
            notifyChange();
        }
    }

    public void hide() {
        if (state != STATE_FINAL && state != STATE_HIDDEN) {
            state = STATE_HIDDEN;
            notifyChange();
        }
    }

    public void beginFinalAnimation() {
        if (state != STATE_FINAL) {
            state = STATE_FINAL;
            notifyChange();
        }
    }

    public int getState() {
        return state;
    }
}
