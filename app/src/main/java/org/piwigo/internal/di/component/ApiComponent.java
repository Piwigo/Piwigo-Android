package org.piwigo.internal.di.component;

import org.piwigo.internal.di.module.ApiModule;
import org.piwigo.internal.di.module.NetworkModule;
import org.piwigo.io.RestService;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {NetworkModule.class, ApiModule.class})
public interface ApiComponent {

    RestService restService();

}
