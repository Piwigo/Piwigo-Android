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

import android.accounts.Account;
import android.arch.core.executor.testing.InstantTaskExecutorRule;
import android.arch.lifecycle.Observer;

import com.google.common.base.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.piwigo.accounts.UserManager;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class MainViewModelTest {

    @Mock UserManager userManager;
    @Mock Account account;

    private MainViewModel viewModel;

    @Rule public TestRule rule = new InstantTaskExecutorRule();

    @Before @SuppressWarnings("Guava") public void setup() {
        MockitoAnnotations.initMocks(this);

        when(userManager.getActiveAccount()).thenReturn(Optional.of(account));
        when(userManager.getUsername(account)).thenReturn("username");
        when(userManager.getSiteUrl(account)).thenReturn("http://piwigo.org/demo");

        viewModel = new MainViewModel(userManager);
    }

    @Test public void constructor_getAccountInfoFromUserManager() {
        verify(userManager).getActiveAccount();
        verify(userManager).getUsername(account);
        verify(userManager).getSiteUrl(account);
    }

    @Test @SuppressWarnings("unchecked") public void getSelectedMenuItem_observerReceivesSelectedMenuItem() {
        int itemId = 1;
        Observer<Integer> observer = (Observer<Integer>) mock(Observer.class);
        viewModel.getSelectedNavigationItemId().observeForever(observer);

        viewModel.navigationItemId.set(itemId);

        verify(observer).onChanged(itemId);
    }
}