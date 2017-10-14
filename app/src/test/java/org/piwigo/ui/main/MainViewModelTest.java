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

import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MainViewModelTest {

    private MainViewModel viewModel;

    @Rule public TestRule rule = new InstantTaskExecutorRule();

    @Before public void setup() {
        viewModel = new MainViewModel();
    }

    @Test @SuppressWarnings("unchecked") public void observerReceivesSelectedMenuItem() {
        MenuItem menuItem = mock(MenuItem.class);
        Observer<MenuItem> observer = (Observer<MenuItem>) mock(Observer.class);
        viewModel.getSelectedMenuItem().observeForever(observer);

        viewModel.navigationListener.onNavigationItemSelected(menuItem);

        verify(observer).onChanged(menuItem);
    }
}