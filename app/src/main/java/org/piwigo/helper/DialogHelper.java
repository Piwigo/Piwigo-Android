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

    /**
     * @param title title of the problem dialog to show
     * @param description details for the user
     * @param problem throwable that caused the trouble
     * @param reportDetail details that are not shown to the user, but will be included if they "REPORT" the issue
     * @param context the android context of the caller to show the dialog
     */
    public void showLogDialog(String title, String description, Throwable problem, String reportDetail, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_Piwigo_ErrorDialog);

        builder.setTitle(title)
                .setMessage(description + "\n\n" + problem.getLocalizedMessage())
                .setPositiveButton(R.string.button_ok, (dialog, which) -> {
                    dialog.cancel();
                })
                .setNegativeButton(R.string.button_report, (dialog, id) -> {
                    ACRA.getErrorReporter().putCustomData("REPORT_TITLE", title);
                    ACRA.getErrorReporter().putCustomData("REPORT_DESC", description);
                    ACRA.getErrorReporter().putCustomData("REPORT_DETAIL", reportDetail);
                    ACRA.getErrorReporter().putCustomData("REPORT", "TRUE");
                    ACRA.getErrorReporter().handleSilentException(problem);
                    ACRA.getErrorReporter().putCustomData("REPORT", "FALSE");
                    dialog.cancel();
                })
                .show();
    }
}
