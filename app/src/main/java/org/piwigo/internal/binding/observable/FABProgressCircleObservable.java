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

package org.piwigo.internal.binding.observable;

import android.databinding.BaseObservable;

public class FABProgressCircleObservable extends BaseObservable {

    public static final int STATE_HIDDEN = 0;
    public static final int STATE_VISIBLE = 1;
    public static final int STATE_FINAL = 2;

    private int state = STATE_HIDDEN;

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
