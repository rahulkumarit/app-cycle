package com.techzellent.hicycle;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.techzellent.hicycle.util.SharedPrefUtil;

public class Splash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (new SharedPrefUtil(Splash.this).isLoggedIn()){
            goToStationMap();
        }
        else {
            goToLogin();
        }
    }

    private void goToStationMap(){
        Intent i = new Intent(Splash.this, StationMap.class);
        Splash.this.startActivity(i);
        Splash.this.finish();
    }

    private void goToLogin(){
        Intent i = new Intent(Splash.this, LoginActivity.class);
        Splash.this.startActivity(i);
        Splash.this.finish();
    }

}
