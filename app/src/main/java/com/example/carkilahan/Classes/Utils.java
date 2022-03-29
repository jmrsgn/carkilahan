package com.example.carkilahan.Classes;

import android.util.Log;

import com.example.carkilahan.BuildConfig;
import com.example.carkilahan.R;


public class Utils {
    public static final String EMAIL = BuildConfig.EMAIL;
    public static final String PASSWORD = BuildConfig.PASSWORD;

    public static void divider() {
        String divider = "";
        for(int i = 0; i < 50; i ++) { divider += "-"; }
        Log.d("Divider ", divider);
    }
}
