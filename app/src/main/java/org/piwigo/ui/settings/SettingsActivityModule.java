package org.piwigo.ui.settings;

import android.content.res.Resources;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsActivityModule {

    @Provides
    Resources provideResources(SettingsActivity activity) {
        return activity.getResources();
    }
}
