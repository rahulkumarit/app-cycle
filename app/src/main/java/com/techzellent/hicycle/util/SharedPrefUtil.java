package com.techzellent.hicycle.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {
    private Context context;
    private SharedPreferences sp;
    private SharedPreferences.Editor spe;

    public SharedPrefUtil(Context cnt) {
        this.context = cnt;
        sp = context.getSharedPreferences("HiCycle", Context.MODE_PRIVATE);
        spe = sp.edit();
    }

    public void saveLoginStatus(boolean isLoggedIn) {
        spe.putBoolean("isLoggedIn", isLoggedIn);
        spe.commit();
    }

    public boolean isLoggedIn() {
        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);
        return isLoggedIn;
    }

    public void setToken(String token) {
        spe.putString("token", token);
        spe.commit();
    }

    public String getToken() {
        String token = sp.getString("token", "");
        return token;
    }

    public void saveStartTime(long startTime) {
        spe.putLong("startTime", startTime);
        spe.commit();
    }

    public long getStartTime() {
        long startTime = sp.getLong("startTime", 0);
        return startTime;
    }

}
