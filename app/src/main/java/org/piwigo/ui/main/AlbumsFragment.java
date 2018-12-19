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
    private int categoryID;

    public AlbumsFragment(){
        super();
        categoryID = 0;
    }

    @Override public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            categoryID = bundle.getInt("Category", 0);
        }
        super.onAttach(context);
    }

    @Override public void onResume(){
        MainViewModel vm = ViewModelProviders.of(this.getActivity(), viewModelFactory).get(MainViewModel.class);
// as we don't work on #69 for release 0.9 let's remove this here...
//        vm.title.set("Album " + binding.getViewModel().getCategory());
        super.onResume();
    }

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_albums, container, false);
        binding.albumRecycler.setHasFixedSize(true);
        binding.albumRecycler.setLayoutManager(new GridLayoutManager(getContext(), calculateColumnCount()));
        binding.photoRecycler.setHasFixedSize(true);
        binding.photoRecycler.setLayoutManager(new GridLayoutManager(getContext(), calculateColumnCount() * 3));


        return binding.getRoot();
    }

    public AlbumsViewModel getViewModel(){
        return binding.getViewModel();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        AlbumsViewModel viewModel = ViewModelProviders.of(this, viewModelFactory).get(AlbumsViewModel.class);
        binding.setViewModel(viewModel);

        binding.getViewModel().loadAlbums(categoryID);
    }

    private int calculateColumnCount() {
        Configuration configuration = getResources().getConfiguration();
        int screenSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean largeScreen = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        return (int) Math.floor(configuration.screenWidthDp / (largeScreen ? TABLET_MIN_WIDTH : PHONE_MIN_WIDTH));
    }


}
