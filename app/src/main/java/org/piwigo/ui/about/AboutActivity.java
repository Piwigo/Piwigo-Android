/*
 * Piwigo for Android
 * Copyright (C) 2017-2017 Piwigo Team http://piwigo.org
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

package org.piwigo.ui.about;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;

import org.piwigo.R;
import org.piwigo.databinding.ActivityAboutBinding;
import org.piwigo.ui.shared.BaseActivity;

import javax.inject.Inject;

import dagger.android.AndroidInjection;

public class AboutActivity extends BaseActivity {
    @Inject AboutViewModelFactory viewModelFactory;

    AboutViewModel viewModel;

    private ActivityAboutBinding binding;

    @Override protected void onCreate(Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        setTheme(R.style.Theme_Piwigo_Login);
        super.onCreate(savedInstanceState);

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(AboutViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_about);
        binding.setViewModel(viewModel);

        binding.aboutTextView.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
