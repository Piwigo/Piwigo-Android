package org.piwigo.internal.binding.observable;

import android.databinding.BaseObservable;

public class EditTextObservable extends BaseObservable {

    private String text;

    public EditTextObservable() {}

    public EditTextObservable(String text) {
        this.text = text;
    }

    public void set(String text) {
        set(text, true);
    }

    public void set(String text, boolean notifyObservers) {
        if ((text == null && this.text != null) || (text != null && !text.equals(this.text))) {
            this.text = text;
            if (notifyObservers) {
                notifyChange();
            }
        }
    }

    public String get() {
        return text;
    }

    public boolean isEmpty() {
        return text == null || text.isEmpty();
    }

}
