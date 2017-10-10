/*
 * Copyright 2017 Phil Bayfield https://philio.me
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