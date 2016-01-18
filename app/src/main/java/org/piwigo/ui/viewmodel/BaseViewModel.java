package org.piwigo.ui.viewmodel;

import android.os.Bundle;

public abstract class BaseViewModel implements ViewModel {

    @Override public void onSaveState(Bundle outState) {}

    @Override public void onRestoreState(Bundle savedState) {}

    @Override public void onDestroy() {}

}
