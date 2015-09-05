/*
 * Copyright 2015 Phil Bayfield https://philio.me
 * Copyright 2015 Piwigo Team http://piwigo.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.piwigo.ui.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import org.piwigo.R;
import org.piwigo.databinding.FragmentLoginBinding;

public class LoginFragment extends Fragment {

    private FragmentLoginBinding binding;

    @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false);
        addFocusListener(binding.loginUrl);
        addFocusListener(binding.username);
        addFocusListener(binding.password);
        return binding.getRoot();
    }

    private void addFocusListener(EditText editText) {
        final View.OnFocusChangeListener oldListener = editText.getOnFocusChangeListener();
        editText.setOnFocusChangeListener((v, hasFocus) -> {
            binding.appBar.setExpanded(false, true);
            if (oldListener != null) {
                oldListener.onFocusChange(v, hasFocus);
                editText.setOnFocusChangeListener(oldListener);
            }
        });
    }

}
