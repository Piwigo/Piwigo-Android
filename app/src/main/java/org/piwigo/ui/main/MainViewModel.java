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

import androidx.annotation.NonNull;
import androidx.databinding.Observable;
import androidx.databinding.ObservableBoolean;
import androidx.databinding.ObservableField;
import androidx.databinding.ObservableInt;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.piwigo.EspressoIdlingResource;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.restmodel.StatusResponse;
import org.piwigo.io.restmodel.SuccessResponse;
import org.piwigo.io.restrepository.RestUserRepository;

import io.reactivex.observers.DisposableObserver;

public class MainViewModel extends ViewModel {
    // TODO: cleanup here...
    public static int STAT_OFFLINE = 0;
    public static int STAT_LOGGED_IN = 1;
    public static int STAT_LOGGED_OFF = 2;
    public static int STAT_AUTH_FAILED = 3;
    public static int STAT_STATUS_FETCHED = 4;

    private static final String TAG = MainViewModel.class.getName();

    private MutableLiveData<Throwable> mError = new MutableLiveData<>();

    public ObservableField<String> title = new ObservableField<>();
    public ObservableField<String> username = new ObservableField<>();
    public ObservableField<String> url = new ObservableField<>();
    public ObservableBoolean drawerState = new ObservableBoolean(false);
    public ObservableBoolean showingRootAlbum = new ObservableBoolean(true);
    public ObservableBoolean displayFab = new ObservableBoolean(false);

    // TODO: finish loginstatus
    public ObservableInt loginStatus = new ObservableInt(STAT_OFFLINE);
    public ObservableField<String> piwigoVersion = new ObservableField<>("");

    private RestUserRepository mUserRepository;
    private UserManager userManager;

    MainViewModel(UserManager userManager, @NonNull RestUserRepository userRepository) {
        this.userManager = userManager;
        mUserRepository = userRepository;
        loginStatus.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
            @Override
            public void onPropertyChanged(Observable sender, int propertyId) {
                Log.d("Mainviewmodel", "login status changed to " + loginStatus.get());
                if (loginStatus.get() == STAT_LOGGED_IN) {
                    mUserRepository.status().subscribe(new StatusSubscriber());
                }
            }
        });
    }

    LiveData<Throwable> getError() {
        return mError;
    }

    public void setError(Throwable th){
        mError.setValue(th);
    }

    public void changeAccount(Account account) {
        userManager.setSessionCookie(null);
        userManager.setSessionToken(null);
        displayFab.set(!userManager.isGuest(account));
        if (account == null)
            return;
        EspressoIdlingResource.moreBusy("main login");
        username.set(userManager.getUsername(account));
        url.set(userManager.getSiteUrl(account));
        Log.d("MainViewModel", "login " + username.get() + " on " + url.get());
        LoginSubscriber loginSubscriber = new LoginSubscriber();
        if (userManager.isGuest(account)) {
            // fake login
            loginSubscriber.onNext(new SuccessResponse());
        } else {
            mUserRepository.login(account).subscribe(loginSubscriber);
        }
    }

    private class LoginSubscriber extends DisposableObserver<SuccessResponse> {
        @Override
        public void onComplete() {
            Log.d("LoginSubscriber", "onComplete");
        }

        @Override
        public void onError(Throwable e) {
            // TODO
            loginStatus.set(STAT_AUTH_FAILED);
            EspressoIdlingResource.lessBusy("main login", "LoginSubscriber.onError");
        }

        @Override
        public void onNext(SuccessResponse loginResponse) {
            Log.d("mainViewModel", "onNext " + userManager.sessionCookie());
            loginStatus.set(STAT_LOGGED_IN);
        }
    }

    private class StatusSubscriber extends DisposableObserver<StatusResponse> {
        @Override
        public void onComplete() {
            Log.d("StatusSubscriber", "onComplete");
            loginStatus.set(STAT_STATUS_FETCHED);
            EspressoIdlingResource.lessBusy("main login", "login status changed");
        }

        @Override
        public void onError(Throwable e) {
            // TODO
            loginStatus.set(STAT_AUTH_FAILED);
            EspressoIdlingResource.lessBusy("main login", "StatusSubscriber.onError");
        }

        @Override
        public void onNext(StatusResponse statusResponse) {
            Log.d("mainViewModel", "status response" + statusResponse.toString());
            if(statusResponse != null && statusResponse.result != null) {
                userManager.setSessionToken(statusResponse.result.pwgToken);
            }
        }
    }

}
