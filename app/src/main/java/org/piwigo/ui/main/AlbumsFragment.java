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

package org.piwigo.ui.main;

import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.piwigo.R;
import org.piwigo.databinding.FragmentAlbumsBinding;
import org.piwigo.ui.shared.BaseFragment;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class AlbumsFragment extends BaseFragment {

    private static final int PHONE_MIN_WIDTH = 320;
    private static final int TABLET_MIN_WIDTH = 360;

    @Inject AlbumsViewModelFactory viewModelFactory;

    private FragmentAlbumsBinding binding;

    @Override public void onAttach(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
        super.onAttach(context);
    }

    @Override @SuppressWarnings("deprecation") public void onAttach(Activity activity) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            AndroidSupportInjection.inject(this);
        }
        super.onAttach(activity);
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_albums, container, false);
        binding.recycler.setLayoutManager(new GridLayoutManager(getContext(), calculateColumnCount()));
        return binding.getRoot();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AlbumsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AlbumsViewModel.class);
        binding.setViewModel(viewModel);
        viewModel.loadAlbums(null);
    }

    private int calculateColumnCount() {
        Configuration configuration = getResources().getConfiguration();
        int screenSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean largeScreen = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        return (int) Math.floor(configuration.screenWidthDp / (largeScreen ? TABLET_MIN_WIDTH : PHONE_MIN_WIDTH));
    }


}
