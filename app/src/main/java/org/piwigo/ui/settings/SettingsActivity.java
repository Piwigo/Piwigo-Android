/*
 * Piwigo for Android
 * Copyright (C) 2018-2018 Raphael Mack http://www.raphael-mack.de
 * Copyright (C) 2018-2018 Piwigo Team http://piwigo.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package org.piwigo.ui.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.piwigo.R;
import org.piwigo.databinding.ActivitySettingsBinding;
import org.piwigo.io.model.SuccessResponse;
import org.piwigo.ui.login.LoginActivity;
import org.piwigo.ui.shared.BaseActivity;

import javax.inject.Inject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import dagger.android.AndroidInjection;

public class SettingsActivity extends BaseActivity {

    @Inject
    SettingsViewModelFactory viewModelFactory;

    private SettingsViewModel viewModel;
    private ActivitySettingsBinding binding;

    private static final String TAG = SettingsActivity.class.getName();


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);


        viewModel = ViewModelProviders.of(this, viewModelFactory).get(SettingsViewModel.class);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);
        binding.setViewModel(viewModel);

        setSupportActionBar(findViewById(R.id.toolbar));
        ActionBar bar = getSupportActionBar();
        if (bar != null) bar.setDisplayHomeAsUpEnabled(true);

        initializeThumbnailSizeSpinner();
        initializeBrightnessSeekBar();
        initializeNumberRowSeekBar();
        initializeLogoutTextViewListener();
    }

    private void initializeLogoutTextViewListener(){
        TextView tvLogout = findViewById(R.id.tv_logout);
        tvLogout.setOnClickListener(view -> {
            viewModel.onLogoutClick();

        });

        viewModel.getLogoutSuccess().observe(this, this::logoutSuccess);
        viewModel.getLogoutError().observe(this, this::logoutError);
    }



    private void logoutSuccess(SuccessResponse response) {
        Toast.makeText(getApplicationContext(), R.string.settings_logout_successful, Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutError(Throwable throwable) {
        Toast.makeText(getApplicationContext(), String.format(getResources().getString(R.string.settings_logout_unsuccessfull), throwable.getMessage()), Toast.LENGTH_LONG).show();
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void initializeThumbnailSizeSpinner(){
        Spinner spinner = findViewById(R.id.spinner_thumbnail_size_value);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,

                R.array.thumbnails_size_array, R.layout.spinner_item);

        adapter.setDropDownViewResource(R.layout.spinner_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                SettingsPreferences.setSettingPreference(SettingsPreferences.KEY_THUMBNAIL_SIZE, spinner.getSelectedItem().toString());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeBrightnessSeekBar(){
        SeekBar brightnessSeekBar = findViewById(R.id.seekbar_brightness);
        TextView textView = findViewById(R.id.tv_brightness_value);

        int cBrightness = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
        textView.setText(cBrightness + "/255");
        brightnessSeekBar.setProgress(cBrightness);

        brightnessSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                Context context = getApplicationContext();
                boolean canWrite = Settings.System.canWrite(context);
                if(canWrite){
                    int sBrightness = i * 255/255;
                    textView.setText(sBrightness + "/255");

                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
                    Settings.System.putInt(context.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, sBrightness);

                } else {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void initializeNumberRowSeekBar(){
        SeekBar numberRowSeekbar = findViewById(R.id.seekbar_number_row);
        TextView textView = findViewById(R.id.tv_number_row_value);

        textView.setText( SettingsPreferences.getSettingPreference(SettingsPreferences.KEY_NUMBER_ROW, "3") + "/6");
        numberRowSeekbar.setProgress(Integer.parseInt(SettingsPreferences.getSettingPreference(SettingsPreferences.KEY_NUMBER_ROW, "3")));

        numberRowSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if(i == 0) i ++;
                int numberRow = i* 6/6;
                SettingsPreferences.setSettingPreference("number_row", String.valueOf(numberRow));
                textView.setText( numberRow + "/6");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }
}
