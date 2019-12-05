/*
 * Piwigo for Android
 * Copyright (C) 2016-2018 Piwigo Team http://piwigo.org
 * Copyright (C) 2018-2018 Raphael Mack http://www.raphael-mack.de
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
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.model.MethodListResponse;
import org.piwigo.io.model.SuccessResponse;
import org.piwigo.io.repository.MethodsRepository;
import org.piwigo.io.repository.UserRepository;

import java.util.List;

public class MainViewModel extends ViewModel {
    // TODO: cleanup here...
    public static int STAT_OFFLINE = 0;
    public static int STAT_LOGGED_IN = 1;
    public static int STAT_LOGGED_OFF = 2;
    public static int STAT_AUTH_FAILED = 3;

    private static final String TAG = MainViewModel.class.getName();

    private MutableLiveData<SuccessResponse> logoutSuccess = new MutableLiveData<>();
    private MutableLiveData<Throwable> logoutError = new MutableLiveData<>();

    LiveData<SuccessResponse> getLogoutSuccess() {
        return logoutSuccess;
    }

    LiveData<Throwable> getLogoutError() {
        return logoutError;
    }

    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> url = new ObservableField<>();
    public ObservableBoolean drawerState = new ObservableBoolean(false);
    public ObservableBoolean showingRootAlbum = new ObservableBoolean(true);
    public ObservableBoolean displayFab = new ObservableBoolean(false);
    public ObservableInt navigationItemId = new ObservableInt(R.id.nav_albums);

    // TODO: finish loginstatus
    public ObservableInt loginStatus = new ObservableInt(STAT_OFFLINE);
    public ObservableField<String> piwigoVersion = new ObservableField<>("");
    public ObservableBoolean communityEnabled = new ObservableBoolean(false);

    private MutableLiveData<Integer> selectedNavigationItemId = new MutableLiveData<>();
    private MethodsRepository mMethodsRepository;
    private UserRepository mUserRepository;
    private UserManager userManager;

    MainViewModel(UserManager userManager, UserRepository userRepository, MethodsRepository methodsRepository) {
        Account account = userManager.getActiveAccount().getValue();
        this.userManager = userManager;
        if (account != null) {
            username.set(userManager.getUsername(account));
            url.set(userManager.getSiteUrl(account));
            displayFab.set(!userManager.isGuest(account));
        }
        mUserRepository = userRepository;
        mMethodsRepository = methodsRepository;

        navigationItemId.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {

            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                selectedNavigationItemId.setValue(navigationItemId.get());
                drawerState.set(false);
            }
        });

        if (userManager.getActiveAccount().getValue() != null) {
            mMethodsRepository.getMethodList(userManager.getActiveAccount().getValue())
                    .subscribe(new MainViewModel.MethodsSubscriber());
        }
    }

    LiveData<Integer> getSelectedNavigationItemId() {
        return selectedNavigationItemId;
    }

    public void onLogoutClick() {
        if (userManager.getActiveAccount().getValue() != null) {
            mUserRepository.logout(userManager.getActiveAccount().getValue())
                    .compose(applySchedulers())
                    .subscribe(new MainViewModel.LogoutSubscriber());
        } else {
            Throwable e = new Throwable(String.valueOf(R.string.account_empty_message));
            logoutError.setValue(e);
        }
    }

    private class MethodsSubscriber extends Subscriber<List<String>> {

        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
        }

        @Override
        public void onNext(List<String> methods) {
            if (methods.contains("community.session.getStatus")) {
                //Here we put "false" as Piwigo expects this value to be false when Community is enabled..
                Log.e("MVM", "Community method found.");
                userManager.setFakedByCommunity(userManager.getActiveAccount().getValue(), "false");
            } else {
                Log.e("MVM", "Community method not found.");
                userManager.setFakedByCommunity(userManager.getActiveAccount().getValue(), "true");
            }
        }
    }


    private class LogoutSubscriber extends Subscriber<SuccessResponse> {

        @Override
        public void onCompleted() {
        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, e.getMessage());
            logoutError.setValue(e);
        }

        @Override
        public void onNext(SuccessResponse successResponse) {
            Log.i(TAG, successResponse.toString());
            logoutSuccess.setValue(successResponse);
        }
    }

    private <T> rx.Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

}
