package com.techzellent.hicycle.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefUtil {

    Context context;

    public SharedPrefUtil(Context cnt){
        this.context = cnt;
    }

    public void SaveLoginStatus(boolean isLoggedIn){
        SharedPreferences sp = context.getSharedPreferences("HiCycle",Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putBoolean("isLoggedIn",isLoggedIn);
        spe.commit();
    }

    public boolean isLoggedIn(){
        SharedPreferences sp= context.getSharedPreferences("HiCycle", Context.MODE_PRIVATE);
        boolean isLoggedIn = sp.getBoolean("isLoggedIn", false);
        return isLoggedIn;
    }

    public void saveStartTime(long startTime){
        SharedPreferences sp = context.getSharedPreferences("HiCycle", Context.MODE_PRIVATE);
        SharedPreferences.Editor spe = sp.edit();
        spe.putLong("startTime", startTime);
        spe.commit();
    }

    public long getStartTime(){
        SharedPreferences sp = context.getSharedPreferences("HiCycle",Context.MODE_PRIVATE);
        long startTime = sp.getLong("startTime", 0);
        return startTime;
    }


}
