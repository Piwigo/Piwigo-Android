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

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.piwigo.R;
import org.piwigo.data.model.Category;
import org.piwigo.databinding.FragmentAlbumsBinding;
import org.piwigo.io.event.RefreshRequestEvent;
import org.piwigo.io.PreferencesRepository;
import org.piwigo.ui.shared.BaseFragment;
import org.piwigo.ui.shared.BindingRecyclerViewAdapter;

import javax.inject.Inject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import dagger.android.support.AndroidSupportInjection;

public class AlbumsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int PHONE_MIN_WIDTH = 320;
    private static final int TABLET_MIN_WIDTH = 360;

    @Inject
    AlbumsViewModelFactory viewModelFactory;

    @Inject
    PreferencesRepository preferences;

    private FragmentAlbumsBinding binding;
    private int categoryID;
    private String categoryName;

    public AlbumsFragment() {
        super();
        /* TODO: architecture improvement: can we move categoryID and name into the AlbumsViewModel?
         *   maybe even use call Category for this, to have not only ID and name, but all properties? */
        categoryID = 0;
        categoryName = "";
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            categoryID = bundle.getInt("Category", 0);
            categoryName = bundle.getString("Title", getString(R.string.nav_albums));
        }
        super.onAttach(context);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Subscribe
    public void onEvent(RefreshRequestEvent event) {
        binding.getViewModel().onRefresh(binding.dataRecycler.getAdapter());
    }

    @Override
    public void onResume() {
        MainViewModel vm = ViewModelProviders.of(this.getActivity(), viewModelFactory).get(MainViewModel.class);
        binding.getViewModel().setMainViewModel(vm);
        vm.title.set(categoryName);
        vm.showingRootAlbum.set(categoryID == 0);
        EventBus.getDefault().register(this);
        super.onResume();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_albums, container, false);
        binding.dataRecycler.setHasFixedSize(true);

        binding.dataRecycler.setLayoutManager(new GridLayoutManager(getContext(), calculateColumnCount()));
        GridLayoutManager lm = new GridLayoutManager(getContext(),
                calculateColumnCount() * preferences.getInt(PreferencesRepository.KEY_PREF_PHOTOS_PER_ROW));
        lm.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                AlbumsViewModel.ViewElement e = getViewModel().data.get(position);
                if(e == null){
                    return 1;
                }
                else {
                    int t = e.getType();
                    if (t == AlbumsViewModel.ViewElement.IMAGE)
                        return 1;
                    else
                        return preferences.getInt(PreferencesRepository.KEY_PREF_PHOTOS_PER_ROW);
                }
            }
        });
        binding.dataRecycler.setLayoutManager(lm);

        binding.swiperefresh.setOnRefreshListener(this);
        return binding.getRoot();
    }

    public AlbumsViewModel getViewModel() {
        return binding.getViewModel();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
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

    @Override
    public void onRefresh() {
        binding.getViewModel().onRefresh(binding.dataRecycler.getAdapter());
    }
}
