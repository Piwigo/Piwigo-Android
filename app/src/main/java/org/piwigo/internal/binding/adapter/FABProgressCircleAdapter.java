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

package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;

import com.github.jorgecastilloprz.FABProgressCircle;

import org.piwigo.internal.binding.observable.FABProgressCircleObservable;

public class FABProgressCircleAdapter {

    @BindingAdapter("bind:observable") public static void bindObservable(FABProgressCircle progressCircle, FABProgressCircleObservable observable) {
        boolean bound = progressCircle.getTag() != null && (boolean) progressCircle.getTag();
        if (!bound) {
            progressCircle.setTag(true);
            return;
        }

        switch (observable.getState()) {
            case FABProgressCircleObservable.STATE_VISIBLE:
                progressCircle.show();
                break;
            case FABProgressCircleObservable.STATE_HIDDEN:
                progressCircle.hide();
                break;
            case FABProgressCircleObservable.STATE_FINAL:
                progressCircle.beginFinalAnimation();
                break;
        }
    }

}
