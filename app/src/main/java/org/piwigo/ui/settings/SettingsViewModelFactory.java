package org.piwigo.ui.settings;
import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

@Singleton
public class SettingsViewModelFactory implements ViewModelProvider.Factory {

    @Inject
    public SettingsViewModelFactory() {
    }

    @Override public <T extends ViewModel> T create(Class<T> viewModelClass) {
        if (viewModelClass.isAssignableFrom(SettingsViewModel.class)) {
            //noinspection unchecked
            return (T) new SettingsViewModel();
        }
        throw new IllegalStateException("Unable to create " + viewModelClass.getName());
    }
}
