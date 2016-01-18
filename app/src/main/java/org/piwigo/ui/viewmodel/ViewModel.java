package org.piwigo.ui.viewmodel;

import android.os.Bundle;

public interface ViewModel {

    void onSaveState(Bundle outState);

    void onRestoreState(Bundle savedInstanceState);

    void onDestroy();

}
