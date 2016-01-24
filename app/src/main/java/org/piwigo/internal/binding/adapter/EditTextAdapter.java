package org.piwigo.internal.binding.adapter;

import android.databinding.BindingAdapter;
import android.databinding.BindingConversion;
import android.text.Editable;
import android.widget.EditText;

import org.piwigo.internal.binding.observable.EditTextObservable;
import org.piwigo.internal.binding.observable.ErrorObservable;
import org.piwigo.ui.text.SimpleTextWatcher;

public class EditTextAdapter {

    @BindingConversion public static String convertToString(EditTextObservable observable) {
        return observable.get();
    }

    @BindingAdapter("bind:observable") public static void bindObservable(EditText editText, EditTextObservable observable) {
        bindObservable(editText, observable, null);
    }

    @BindingAdapter({"bind:observable", "bind:error"}) public static void bindObservable(EditText editText, EditTextObservable observable, ErrorObservable errorObservable) {
        // Track if TextWatcher is bound via the tag to avoid added multiple
        boolean bound = editText.getTag() != null && (boolean) editText.getTag();
        if (!bound) {
            editText.setText(observable.get());
            editText.addTextChangedListener(new SimpleTextWatcher() {

                @Override public void afterTextChanged(Editable s) {
                    // Update the value without notifying the observer (as that moves the cursor to position 0)
                    observable.set(s.toString(), false);
                    if (errorObservable != null && errorObservable.hasError()) {
                        errorObservable.clear();
                    }
                }

            });

            // Move cursor to the end as that's more useful
            if (editText.getText().length() > 0) {
                editText.setSelection(editText.getText().length());
            }

            editText.setTag(true);
        } else if (!editText.getText().toString().equals(observable.get())) {
            editText.setText(observable.get());
        }
    }

}
