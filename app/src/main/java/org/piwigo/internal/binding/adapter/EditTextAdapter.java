package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import org.piwigo.internal.binding.observable.EditTextObservable;

public class EditTextAdapter {

    @BindingConversion public static String convertToString(EditTextObservable observable) {
        return observable.get();
    }

    @BindingAdapter("bind:observable") public static void bindObservable(EditText editText, EditTextObservable observable) {
        // Track if TextWatcher is bound via the tag to avoid added multiple
        boolean bound = editText.getTag() != null && (boolean) editText.getTag();
        if (!bound) {
            editText.setText(observable.get());
            editText.addTextChangedListener(new TextWatcher() {

                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

                @Override public void afterTextChanged(Editable s) {
                    // Update the value without notifying the observer (moves the cursor to position 0)
                    observable.set(s.toString(), false);
                    observable.setError(0);
                }

            });
            if (editText.getText().length() > 0) {
                editText.setSelection(editText.getText().length());
            }
            editText.setTag(true);
        } else if (observable.get() != null && !editText.getText().toString().equals(observable.get())) {
            editText.setText(observable.get());
        }

        // Populate errors to the EditText or a parent TextInputLayout
        if (editText.getParent() instanceof TextInputLayout) {
            ((TextInputLayout) editText.getParent()).setError(observable.getError() == 0 ? null : editText.getResources().getString(observable.getError()));
        } else {
            editText.setError(observable.getError() == 0 ? null : editText.getResources().getString(observable.getError()));
        }
    }

}
