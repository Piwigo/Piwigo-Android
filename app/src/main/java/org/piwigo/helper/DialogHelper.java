package org.piwigo.helper;

import android.content.Context;

import androidx.appcompat.app.AlertDialog;

import org.acra.ACRA;
import org.piwigo.R;

public class DialogHelper {
    public static DialogHelper INSTANCE;

    public DialogHelper() {
        INSTANCE = this;
    }

    public void showErrorDialog(int titleId, int messageId, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_Piwigo_ErrorDialog);

        builder.setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .show();
    }

    public void showLogDialog(String title, String description, Throwable problem, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_Piwigo_ErrorDialog);

        builder.setTitle(title)
                .setMessage(description + "\n\n" + problem.getLocalizedMessage())
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .setNegativeButton(R.string.button_report, (dialog, id) -> {
                    ACRA.getErrorReporter().handleSilentException(problem);
                    dialog.cancel();
                })
                .show();
    }
}
