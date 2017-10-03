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

import android.content.res.Configuration;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.databinding.FragmentAlbumsBinding;
import org.piwigo.ui.viewmodel.AlbumsViewModel;

import javax.inject.Inject;

public class AlbumsFragment extends BaseFragment implements SwipeRefreshLayout.OnRefreshListener {

    private static final int PHONE_MIN_WIDTH = 320;
    private static final int TABLET_MIN_WIDTH = 360;

    @Inject AlbumsViewModel viewModel;

    FragmentAlbumsBinding binding;

    @Nullable @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_albums, container, false);
        binding.recycler.setLayoutManager(new GridLayoutManager(getContext(), calculateColumnCount()));
        ((SwipeRefreshLayout)binding.getRoot()).setOnRefreshListener(this);
        return binding.getRoot();
    }

    @Override public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        getActivityComponent().inject(this);
        bindLifecycleEvents(viewModel);
        binding.setViewModel(viewModel);
        viewModel.loadAlbums(null);
    }

    private int calculateColumnCount() {
        Configuration configuration = getResources().getConfiguration();
        int screenSize = configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean largeScreen = screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE;
        return (int) Math.floor(configuration.screenWidthDp / (largeScreen ? TABLET_MIN_WIDTH : PHONE_MIN_WIDTH));
    }

    public void refresh() {
        /* TODO: implement real refresh and call setRefreshing(false) after finishing the refresh on the swipe (see onRefresh)
        * maybe the viewModel.loadAlbums(null) already does the job? but is this synchronous and fully refreshed on return?
        * */
        viewModel.loadAlbums(null);
    }

    @Override
    public void onRefresh(){

        Toast.makeText(getActivity().getApplicationContext(), "Refreshing is not yet implemented :-(", Toast.LENGTH_LONG).show();
        /* for now just stop the refresh animation after 1 sec. */
        final Handler handler = new Handler();

        refresh();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                ((SwipeRefreshLayout)binding.getRoot()).setRefreshing(false);
            }
        }, 1000);
    }
}
