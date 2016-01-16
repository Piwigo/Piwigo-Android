package org.piwigo.ui.viewmodel;

import android.os.Bundle;

public interface ViewModel {

    void onSave(Bundle outState);

    void onRestore(Bundle savedInstanceState);

    void onDestroy();

}
