package org.piwigo.ui.settings;

import android.accounts.Account;
import android.content.res.Resources;
import android.util.Log;

import org.piwigo.R;
import org.piwigo.accounts.UserManager;
import org.piwigo.io.model.SuccessResponse;
import org.piwigo.io.repository.UserRepository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class SettingsViewModel extends ViewModel {

    private static final String TAG = SettingsViewModel.class.getName();

    private final UserRepository userRepository;
    private final Resources resources;
    private final UserManager userManager;
    private Account account;

    private MutableLiveData<SuccessResponse> logoutSuccess = new MutableLiveData<>();
    private MutableLiveData<Throwable> logoutError = new MutableLiveData<>();

    LiveData<SuccessResponse> getLogoutSuccess() {
        return logoutSuccess;
    }

    LiveData<Throwable> getLogoutError() {
        return logoutError;
    }

    public SettingsViewModel(UserManager userManager, UserRepository userRepository, Resources resources) {
        this.userRepository = userRepository;
        this.resources = resources;
        this.userManager = userManager;
        this.account = userManager.getActiveAccount().getValue();

    }

    public void onLogoutClick() {
        if (account != null) {
            userRepository.logout(userManager.getActiveAccount().getValue())
                    .compose(applySchedulers())
                    .subscribe(new LogoutSubscriber());
        } else {
            Throwable e = new Throwable(String.valueOf(R.string.account_empty_message));
            logoutError.setValue(e);
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
            userManager.removeAccount();
            userManager.refreshAccounts();
            logoutSuccess.setValue(successResponse);
        }
    }

    private <T> rx.Observable.Transformer<T, T> applySchedulers() {
        return observable -> observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
