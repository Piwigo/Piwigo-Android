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
