/*
 * Copyright 2016 Phil Bayfield https://philio.me
 * Copyright 2016 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
