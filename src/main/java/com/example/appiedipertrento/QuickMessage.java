package com.example.appiedipertrento;

import android.app.Activity;
import android.widget.Toast;

/**
 * Created by Pietro on 28/12/13.
 * Copyright Pietro 2014
 */
public class QuickMessage {

    Activity activity;

    public QuickMessage(Activity a){
        activity = a;
    }

    public void quickMessage(final String msg) {
        activity.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                Toast.makeText(activity, msg, Toast.LENGTH_SHORT)
                        .show();
            }
        });
    }
}
