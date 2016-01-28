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

package org.piwigo.ui.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;

import org.piwigo.internal.di.component.ActivityComponent;
import org.piwigo.ui.activity.BaseActivity;

public abstract class BaseFragment extends Fragment {

    private ActivityComponent activityComponent;

    @Override public void onAttach(Context context) {
        super.onAttach(context);
        activityComponent = ((BaseActivity) context).getActivityComponent();
    }

    protected ActivityComponent getActivityComponent() {
        return activityComponent;
    }

}
