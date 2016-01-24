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

public class ErrorObservable extends BaseObservable {

    private String error;

    public ErrorObservable() {}

    public ErrorObservable(String error) {
        this.error = error;
    }

    public void set(String error) {
        this.error = error;
        notifyChange();
    }

    public String get() {
        return error;
    }

    public void clear() {
        this.error = null;
        notifyChange();
    }

    public boolean hasError() {
        return error != null;
    }

}
