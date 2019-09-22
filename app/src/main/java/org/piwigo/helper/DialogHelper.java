package org.piwigo.helper;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.InputType;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatEditText;

import org.piwigo.R;

import java.util.Objects;

public class DialogHelper {
    public static DialogHelper INSTANCE;
    private boolean isOpened = false;

    public DialogHelper() {
        INSTANCE = this;
    }

    public void showErrorDialog(int titleId, int messageId, Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.Theme_Piwigo_ErrorDialog);

        builder.setTitle(titleId);
        builder.setMessage(messageId);
        builder.setPositiveButton(R.string.button_ok, (dialog, which) -> {
            closeDialog(dialog);
        });
        builder.show();
    }

    private void closeDialog(DialogInterface dialog)
    {
        dialog.cancel();
    }
}
