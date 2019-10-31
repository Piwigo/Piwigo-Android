package org.piwigo.ui.settings;

import android.content.Context;

import org.piwigo.accounts.UserManager;
import org.piwigo.io.repository.UserRepository;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

@Singleton
public class SettingsViewModelFactory implements ViewModelProvider.Factory {

    private final Context context;
    private final UserRepository userRepository;
    private final UserManager userManager;


    @Inject
    public SettingsViewModelFactory(Context context, UserRepository userRepository, UserManager userManager) {
        this.userManager = userManager;
        this.context = context;
        this.userRepository = userRepository;
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(SettingsViewModel.class)) {
            //noinspection unchecked
            return (T) new SettingsViewModel(userManager, userRepository, context.getResources());
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}