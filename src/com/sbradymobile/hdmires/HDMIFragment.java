/*
 * Copyright (C) 2012 The CyanogenMod Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sbradymobile.hdmires;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.text.InputType;
import android.util.Log;

import com.sbradymobile.hdmires.R;

public class HDMIFragment extends PreferenceFragment implements OnPreferenceChangeListener {

    private EditTextPreference mWidthPref;
    private EditTextPreference mHeightPref;
    private EditTextPreference mDensityPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = getActivity();
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean active = sharedPrefs.getBoolean("service_active", true);

        if (active && !isMyServiceRunning()) {
            Intent intent = new Intent(context, HDMIService.class);
            context.startService(intent);
        }

        addPreferencesFromResource(R.xml.hdmi_preferences);

        mWidthPref = (EditTextPreference)findPreference("width");
        mWidthPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        mWidthPref.setOnPreferenceChangeListener(this);

        mHeightPref = (EditTextPreference)findPreference("height");
        mHeightPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        mHeightPref.setOnPreferenceChangeListener(this);

        mDensityPref = (EditTextPreference)findPreference("density");
        mDensityPref.getEditText().setInputType(InputType.TYPE_CLASS_NUMBER);
        mDensityPref.setOnPreferenceChangeListener(this);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        Intent intent = new Intent("com.sbradymobile.hdmires.RESIZE_DISPLAY");
        getActivity().sendBroadcast(intent);
        return true;
    }

    private boolean isMyServiceRunning() {
        ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (HDMIService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
