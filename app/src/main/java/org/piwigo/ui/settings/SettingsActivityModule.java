package org.piwigo.ui.settings;


import android.content.res.Resources;

import org.piwigo.ui.main.MainActivity;

import dagger.Module;
import dagger.Provides;

@Module
public class SettingsActivityModule {

    @Provides Resources provideResources(MainActivity activity) {
        return activity.getResources();
    }
}