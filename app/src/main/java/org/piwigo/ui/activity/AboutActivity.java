/*
 * Copyright 2017 Raphael Mack
 * Copyright 2017 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.activity;

import android.databinding.DataBindingUtil;
import android.os.Bundle;

import org.piwigo.R;
import org.piwigo.databinding.ActivityAboutBinding;
import org.piwigo.ui.viewmodel.AboutViewModel;

import javax.inject.Inject;


public class AboutActivity extends BaseActivity  {

    @Inject AboutViewModel viewModel;

    private ActivityAboutBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new AboutViewModel();
        viewModel.setView(this);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        bindLifecycleEvents(viewModel);
        binding.setViewModel(viewModel);
        viewModel.setView(this);
    }
}
