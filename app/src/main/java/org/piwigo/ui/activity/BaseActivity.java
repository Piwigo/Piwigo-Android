package org.piwigo.ui.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import org.piwigo.internal.di.component.ApplicationComponent;
import org.piwigo.PiwigoApplication;

public abstract class BaseActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getApplicationComponent().inject(this);
    }

    protected ApplicationComponent getApplicationComponent() {
        return ((PiwigoApplication)getApplication()).getApplicationComponent();
    }

}
