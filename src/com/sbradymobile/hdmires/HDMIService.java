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

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.preference.PreferenceManager;

import java.io.DataOutputStream;

public class HDMIService extends Service {

    private SharedPreferences mPref;
    private boolean mHDMIplugged = false;
    private boolean mAlwaysResize = false;
    private boolean mHDMIResize = false;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.HDMI_PLUGGED".equals(action)) {
                mHDMIplugged = intent.getBooleanExtra("state", false);
                resizeDisplay();
            } else if ("com.sbradymobile.hdmires.RESIZE_DISPLAY".equals(action)) {
                resizeDisplay();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mPref = PreferenceManager.getDefaultSharedPreferences(this);

        IntentFilter filter = new IntentFilter("android.intent.action.HDMI_PLUGGED");
        filter.addAction("com.sbradymobile.hdmires.RESIZE_DISPLAY");
        registerReceiver(mReceiver, filter);

        resizeDisplay();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void resizeDisplay() {
        mAlwaysResize = mPref.getBoolean("screen_active", false);
        mHDMIResize = mPref.getBoolean("service_active", true);
        try {
            boolean hdmi = mHDMIResize && mHDMIplugged;
            String width = mPref.getString(hdmi ? "width" : "screen_width", "2560");
            String height = mPref.getString(hdmi ? "height" : "screen_height", hdmi ? "1440" : "1600");
            String density = mPref.getString(hdmi ? "density" : "screen_density", "320");
            String resolution = hdmi || mAlwaysResize ? (width + "x" + height) : "reset";
            density = hdmi || mAlwaysResize ? density : "reset";
            resolution = "am display-size " + resolution;
            density = "am display-density " + density;
            Process suProcess = Runtime.getRuntime().exec("su"); 
            DataOutputStream out = new DataOutputStream(suProcess.getOutputStream());
            out.writeBytes(resolution);
            out.flush();
            out.close();
            suProcess = Runtime.getRuntime().exec("su"); 
            out = new DataOutputStream(suProcess.getOutputStream());
            out.writeBytes(density);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
