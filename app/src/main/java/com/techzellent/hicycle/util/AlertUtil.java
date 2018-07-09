package com.techzellent.hicycle.util;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Build;

public class AlertUtil {

    Context context;
    public AlertUtil(Context cnt)
    {
        this.context= cnt;
    }

    public void showAlertOk(String title, String msg){
        AlertDialog.Builder builder;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
        }
        else{
            builder= new AlertDialog.Builder(context);
        }

        builder.setTitle(title)
                .setMessage(msg)
                .setPositiveButton("OK",null)
                .show();
    }
}
