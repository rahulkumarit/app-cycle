package com.techzellent.hicycle;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.techzellent.hicycle.barcode.BarcodeCaptureActivity;
import com.techzellent.hicycle.util.AlertUtil;
import com.techzellent.hicycle.util.SharedPrefUtil;
import com.techzellent.hicycle.wsCalling.WSUtils;
import com.techzellent.hicycle.wsCalling.WsCalling;
import com.techzellent.hicycle.wsCalling.WsReponse;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class Hire extends Activity implements WsReponse {

    int BARCODE_READER_REQUEST_CODE = 101;
    String TAG = Hire.class.getSimpleName();
    private TextView txtTimer;
    private Button btnHireLeave, btnUnlock;
    private TextView tvTitle;
    private Calendar calendarStart, calendarNow;
    private Chronometer cm;
    private long startTime = 0;
    private FirebaseAnalytics mFirebaseAnalytics;
    private ProgressBar hire_progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hire);
        tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText("Hire Bicycle");
        txtTimer = findViewById(R.id.tv_timer);
        btnHireLeave = findViewById(R.id.btnHireLeave);
        btnUnlock = findViewById(R.id.btnUnlock);
        cm = (Chronometer) findViewById(R.id.cm_hire);
        hire_progress = findViewById(R.id.hire_progress);
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        btnHireLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startTime == 0) {
                    Intent intent = new Intent(Hire.this, BarcodeCaptureActivity.class);
                    startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
                } else {
                    btnHireLeave.setText("Hire Bicycle");
                    cm.stop();
                    startTime = 0;
                    (new SharedPrefUtil(Hire.this)).saveStartTime(startTime);
                    wsCallingEndTrip();
                }
            }
        });
        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hire_progress.setVisibility(View.VISIBLE);
                btnUnlock.setEnabled(false);
                callWsUnlock();
            }
        });

        cm.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if (startTime != 0) {
                    calendarNow = Calendar.getInstance();
                    long diff = calendarNow.getTime().getTime() - startTime;
                    diff = diff / 1000;
                    int sec = (int) diff % 60;
                    int min = (int) diff / 60;
                    int hr = min / 60;
                    min = min % 60;
                    txtTimer.setText("" + hr + " : " + min + " : " + sec);
                }
            }
        });
    }

    private void wsCallingEndTrip() {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", "test");//need to change here
            jsonBody.put("password", "test");//need ti change here
            jsonBody.put("cycleNum", "1");
            jsonBody.put("stationNum", "1");
            final String requestBody = jsonBody.toString();
            WsCalling.postResponseWithParam(WSUtils.WS_ENDTRIP_CODE, WSUtils.WS_ENDTRIP, requestBody, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void callWsUnlock() {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", "test");//need to change here
            jsonBody.put("password", "test");//need ti change here
            jsonBody.put("cycleNum", "1");
            jsonBody.put("stationNum", "1");
            final String requestBody = jsonBody.toString();
            WsCalling.postResponseWithParam(WSUtils.WS_UNLOCK_CODE, WSUtils.WS_UNLOCK, requestBody, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime = (new SharedPrefUtil(Hire.this)).getStartTime();
        if (startTime == 0) {
            btnHireLeave.setText("Hire Bicycle");
        } else {
            btnHireLeave.setText("Unassign Bicycle");
            cm.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        cm.stop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == BARCODE_READER_REQUEST_CODE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
//                    Point[] p = barcode.cornerPoints;
                    Log.e(TAG, barcode.displayValue);
                    calendarStart = Calendar.getInstance();
                    startTime = calendarStart.getTime().getTime();
                    (new SharedPrefUtil(Hire.this)).saveStartTime(startTime);
                    btnHireLeave.setText("Unassign Bicycle");
                    cm.start();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SUCCESS, "true");
                    bundle.putString(FirebaseAnalytics.Param.VALUE, barcode.displayValue);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    (new AlertUtil(Hire.this)).showAlertOk(TAG, "Bicycle  " + barcode.displayValue, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            hire_progress.setVisibility(View.VISIBLE);
                            wsCallingForHiring();
                        }
                    });

                } else {
                    Log.e(TAG, "QR Code not scanned Properly");
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SUCCESS, "true");
                    bundle.putString(FirebaseAnalytics.Param.VALUE, "No QR");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
                    (new AlertUtil(Hire.this)).showAlertOk(TAG, "null Bicycle  data", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                        }
                    });
                }

            } else
                Log.e(TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.SUCCESS, "false");
            bundle.putString(FirebaseAnalytics.Param.VALUE, "No QR");
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);
//                (new AlertUtil(Hire.this)).showAlertOk(TAG,CommonStatusCodes.getStatusCodeString(resultCode) );
            Toast.makeText(this, "QR Code not scanned", Toast.LENGTH_SHORT).show();
        }
    }

    private void wsCallingForHiring() {
        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("userId", "test");//need to change here
            jsonBody.put("password", "test");//need ti change here
            jsonBody.put("cycleNum", "1");
            jsonBody.put("stationNum", "1");
            final String requestBody = jsonBody.toString();
            WsCalling.postResponseWithParam(WSUtils.WS_ASSIGN_CODE, WSUtils.WS_ASSIGN, requestBody, this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void successReposse(int responseCode, String response) {
        hire_progress.setVisibility(View.GONE);
        switch (responseCode) {
            case WSUtils.WS_ASSIGN_CODE:
                parsingWsCalling(response);
                break;
            case WSUtils.WS_UNLOCK_CODE:
                parsingUnlockWs(response);
                btnUnlock.setEnabled(true);
                break;

            case WSUtils.WS_ENDTRIP_CODE:
                parseEndTrip(response);
                break;
            default:
                break;
        }
    }

    private void parseEndTrip(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean status = jsonObject.getBoolean("status");
            String message = jsonObject.getString("message");
            Toast.makeText(Hire.this, message, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
        }
    }

    private void parsingUnlockWs(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean status = jsonObject.getBoolean("status");
            String message = jsonObject.getString("message");
            Toast.makeText(Hire.this, message, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
        }
    }

    private void parsingWsCalling(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            boolean status = jsonObject.getBoolean("status");
            String message = jsonObject.getString("message");
            Toast.makeText(Hire.this, message, Toast.LENGTH_SHORT).show();
        } catch (JSONException e) {
        }
    }

    @Override
    public void errorResponse(int responseCode, String exception) {
        hire_progress.setVisibility(View.GONE);
        btnUnlock.setEnabled(true);
        switch (responseCode) {
            case WSUtils.WS_UNLOCK_CODE:
                break;
            default:


        }
    }
}
