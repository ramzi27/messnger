package com.ramzi.messanger.utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Ramzi on 02-May-18.
 */

public class SharedPref {
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private Context context;

    public SharedPref(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(Const.SHARED_PREF, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void writeImage(String ss) {
        editor.putString(Const.IMAGE_KEY, ss);
        editor.apply();
    }

    public String readImage() {
        String ss = sharedPreferences.getString(Const.IMAGE_KEY, "ss");
        return ss;
    }

}
