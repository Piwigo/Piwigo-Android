package org.piwigo.ui.main;

import android.view.View;

public class ClickHandler {

    private final String TAG = "LoginHandler";

    public void performLogin(String userName, String password) {
        // Some magic code of login

       // Log.d(TAG, "Username: " + userName + " & Password:" + password);
    }

    public void setPasswordVisible(View view, boolean isVisible) {

       // Log.d(TAG, isVisible ? "Password is visible" : "Password is not visible");
    }
}
