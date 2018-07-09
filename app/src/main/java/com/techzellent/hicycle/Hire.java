package com.techzellent.hicycle;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.techzellent.hicycle.barcode.BarcodeCaptureActivity;
import com.techzellent.hicycle.util.AlertUtil;
import com.techzellent.hicycle.util.SharedPrefUtil;

import org.w3c.dom.Text;

import java.util.Calendar;

public class Hire extends Activity {

    int BARCODE_READER_REQUEST_CODE=101;
    String TAG = Hire.class.getSimpleName();

    TextView txtTimer;
    Button btnHireLeave, btnUnlock;
    private TextView tvTitle;
    private Calendar calendarStart, calendarNow;
    private Chronometer cm;
    private long startTime=0;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hire);

        tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText("Hire Bicycle");
        
        txtTimer= (TextView) findViewById(R.id.tv_timer);
        btnHireLeave = (Button) findViewById(R.id.btnHireLeave);
        btnUnlock = (Button) findViewById(R.id.btnUnlock);

        cm = (Chronometer) findViewById(R.id.cm_hire);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);



        btnHireLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(startTime == 0){
                    Intent intent = new Intent(Hire.this, BarcodeCaptureActivity.class);
                    startActivityForResult(intent, BARCODE_READER_REQUEST_CODE);
                }
                else{
//                    Call WS to unassign Bicycle, on success call below code
                    btnHireLeave.setText("Hire Bicycle");
                    cm.stop();
                    startTime=0;
                    (new SharedPrefUtil(Hire.this)).saveStartTime(startTime);

                }
            }
        });

        btnUnlock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        cm.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer chronometer) {
                if(startTime != 0){
                    calendarNow = Calendar.getInstance();
                    long diff =  calendarNow.getTime().getTime() - startTime;
                    diff = diff/1000;
                    int sec = (int) diff % 60;
                    int min = (int) diff / 60;
                    int hr = min / 60;
                    min = min % 60;
                    txtTimer.setText("" + hr + " : " + min + " : " + sec);

                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        startTime=(new SharedPrefUtil(Hire.this)).getStartTime();
        if(startTime == 0){
            btnHireLeave.setText("Hire Bicycle");
        }
        else{
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

        if (requestCode == BARCODE_READER_REQUEST_CODE)
        {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if(data != null){
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
//                    Point[] p = barcode.cornerPoints;
                    Log.e(TAG, barcode.displayValue);
                    calendarStart =Calendar.getInstance();
                    startTime = calendarStart.getTime().getTime();
                    (new SharedPrefUtil(Hire.this)).saveStartTime(startTime);
                    btnHireLeave.setText("Unassign Bicycle");
                    cm.start();
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SUCCESS, "true");
                    bundle.putString(FirebaseAnalytics.Param.VALUE , barcode.displayValue);
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT , bundle);
                    (new AlertUtil(Hire.this)).showAlertOk(TAG,"Bicycle  " +barcode.displayValue );
                }
                else{
                    Log.e(TAG, "QR Code not scanned Properly");
                    Bundle bundle = new Bundle();
                    bundle.putString(FirebaseAnalytics.Param.SUCCESS, "true");
                    bundle.putString(FirebaseAnalytics.Param.VALUE , "No QR");
                    mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT , bundle);
                    (new AlertUtil(Hire.this)).showAlertOk(TAG,"null Bicycle  data" );
                }

            }
            else
                Log.e(TAG, String.format(getString(R.string.barcode_error_format),
                        CommonStatusCodes.getStatusCodeString(resultCode)));
                Bundle bundle = new Bundle();
                bundle.putString(FirebaseAnalytics.Param.SUCCESS, "false");
                bundle.putString(FirebaseAnalytics.Param.VALUE , "No QR");
                mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT , bundle);
//                (new AlertUtil(Hire.this)).showAlertOk(TAG,CommonStatusCodes.getStatusCodeString(resultCode) );
            Toast.makeText(this, "QR Code not scanned", Toast.LENGTH_SHORT).show();

        }

    }



}
