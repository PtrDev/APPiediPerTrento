package com.example.appiedipertrento;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TableLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

/**
 * Created by Pietro on 27/01/14.
 * Copyright Pietro 2014
 */
public class Find_Path extends Activity {

    EditText start,stop,start2,length;
    Button ok,cancel;
    ImageButton my_location_start,my_location_stop,my_location2_start;
    TableLayout modaliy1_layout,modaliy2_layout;
    RadioGroup radioGroup;
    boolean START,STOP,START2,LENGTH = false;
    int mod = 1;
    int max_length = -1;

    MarkerOptions start_pos,stop_pos;

    Handler myHandler;
    Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            if ((START && STOP)||(LENGTH && START2)){
                myHandler.removeCallbacks(myRunnable);
                finishWithResult(mod);
            }else {
                myHandler.postDelayed(myRunnable,100);
            }
        }
    };



    public Find_Path(){

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.find_path_layout);

        myHandler = new Handler();

        modaliy1_layout = (TableLayout) findViewById(R.id.modality1);
        modaliy2_layout = (TableLayout) findViewById(R.id.modality2);

        start = (EditText) findViewById(R.id.fp_start);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                START = false;
            }
        });
        stop = (EditText) findViewById(R.id.fp_destination);
        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                STOP = false;
            }
        });
        start2 = (EditText) findViewById(R.id.fp2_start);
        start2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                START2 = false;
            }
        });
        length = (EditText) findViewById(R.id.fp_max_length);
        length.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LENGTH = false;
            }
        });
        radioGroup = (RadioGroup) findViewById(R.id.fp_radioGroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                if (id == R.id.fp_radio_start_stop){
                    modaliy1_layout.setVisibility(View.VISIBLE);
                    modaliy2_layout.setVisibility(View.GONE);
                }else {
                    modaliy1_layout.setVisibility(View.GONE);
                    modaliy2_layout.setVisibility(View.VISIBLE);
                }
            }
        });
        my_location_start = (ImageButton) findViewById(R.id.fp_my_position);
        my_location_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = new LatLng(COMap.myPosition().latitude,COMap.myPosition().longitude);
                start_pos = CreateMarker("Here",latLng);
                start.setText("From here");
                START = true;
            }
        });
        my_location_stop = (ImageButton) findViewById(R.id.fp_end_my_position);
        my_location_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = new LatLng(COMap.myPosition().latitude,COMap.myPosition().longitude);
                stop_pos = CreateMarker("Here",latLng);
                stop.setText("To here");
                STOP = true;
            }
        });
        my_location2_start = (ImageButton) findViewById(R.id.fp2_my_position);
        my_location2_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LatLng latLng = new LatLng(COMap.myPosition().latitude,COMap.myPosition().longitude);
                start_pos = CreateMarker("Here",latLng);
                start2.setText("From here, to here");
                START2 = true;
            }
        });

        ok = (Button) findViewById(R.id.fp_ok);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                myRunnable.run();
                if (modaliy1_layout.getVisibility() == View.VISIBLE){
                    //modalità 1
                    if (!START)
                        new GeocoderTask(false,1).execute(String.valueOf(start.getText()));
                    if (!STOP)
                        new GeocoderTask(true,1).execute(String.valueOf(stop.getText()));
                    mod = 1;
                }else {
                    //modalità2
                    if (!START2)
                        new GeocoderTask(false,2).execute(String.valueOf(start2.getText()));
                    try {
                        max_length = Integer.parseInt(String.valueOf(length.getText()));
                    } catch(NumberFormatException nfe) {
                        Log.d("parsing",nfe.getMessage());
                    }
                    if (max_length <= 0){
                        length.setHintTextColor(Color.RED);
                        length.setHint("INSERT A CORRECT LENGTH!");
                        myHandler.removeCallbacks(myRunnable);
                    }else {
                        LENGTH = true;
                    }
                    mod = 2;
                }
            }
        });

        cancel = (Button) findViewById(R.id.fp_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishWithResult(0);
            }
        });
    }

    // An AsyncTask class for accessing the GeoCoding Web Service
    private class GeocoderTask extends AsyncTask<String, Void, List<Address>> {

        public boolean last;
        public int modality;

        public GeocoderTask(boolean l,int m){
            last = l;
            modality = m;
        }

        @Override
        protected List<Address> doInBackground(String... locationName) {
            // Creating an instance of Geocoder class
            Geocoder geocoder = new Geocoder(getBaseContext());
            List<Address> addresses = null;

            try {
                // Getting a maximum of 3 Address that matches the input text
                addresses = geocoder.getFromLocationName(locationName[0], 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return addresses;
        }

        @Override
        protected void onPostExecute(List<Address> addresses) {

            if(addresses==null || addresses.size()==0){
                if (!last && modality == 1){
                    start.setHintTextColor(Color.RED);
                    start.setHint("INSERT A CORRECT ADDRESS!");
                }else if (last && modality == 1) {
                    stop.setHintTextColor(Color.RED);
                    stop.setHint("INSERT A CORRECT ADDRESS!");
                }else {
                    start2.setHintTextColor(Color.RED);
                    start2.setHint("INSERT A CORRECT ADDRESS!");
                }
                myHandler.removeCallbacks(myRunnable);
            }else {
                String[] strAdresses;
                // Adding Markers on Google Map for each matching address
                Address temp = addresses.get(0);
                // Creating an instance of GeoPoint, to display in Google Map
                LatLng latLng = new LatLng(temp.getLatitude(), temp.getLongitude());

                String addressText = String.format("%s, %s",
                        temp.getMaxAddressLineIndex() > 0 ? temp.getAddressLine(0) : "",
                        temp.getCountryName());

                if (!last && modality == 1){
                    start_pos = CreateMarker(addressText,latLng);
                    START = true;
                }else if (last && modality == 1) {
                    stop_pos = CreateMarker(addressText,latLng);
                    STOP = true;
                }else {
                    start_pos = CreateMarker(addressText,latLng);
                    START2 = true;
                }
            }
        }
    }

    public MarkerOptions CreateMarker(String text, LatLng coord){
        return new MarkerOptions()
                .position(coord)
                .title(text);
    }

    private void finishWithResult(int modality)
    {
        Bundle conData = new Bundle();
        conData.putInt("mod",modality);
        conData.putInt("length", max_length);
        Intent intent = new Intent();
        intent.putExtras(conData);
        setResult(RESULT_OK, intent);
        COMap.start_position = start_pos;
        COMap.stop_position = stop_pos;
        finish();
    }
}
