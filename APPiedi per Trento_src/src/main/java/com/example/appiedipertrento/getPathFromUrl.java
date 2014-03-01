package com.example.appiedipertrento;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by Pietro on 07/01/14.
 * Copyright Pietro 2014
 */

public class getPathFromUrl extends AsyncTask<String, String, String>{

    ProgressDialog progressDialog;
    Activity activity;
    QuickMessage mex;
    boolean progress;
    public LatLng[] pathLatLongList;


    public getPathFromUrl(Activity a,boolean activeProgress){
        activity = a;
        progressDialog = new ProgressDialog(activity);
        progress = activeProgress;
        mex = new QuickMessage(activity);
    }

    protected void onPreExecute() {
        if (progress){
            progressDialog.setMessage("Calculating path...");
            progressDialog.show();
            progressDialog.setCancelable(false);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    //getPathFromUrl.this.cancel(true);
                }
            });
        }
    }

    @Override
    protected String doInBackground(String... uri) {
        HttpClient httpclient = new DefaultHttpClient();
        HttpResponse response;
        String responseString = null;
        try {
            response = httpclient.execute(new HttpGet(uri[0]));
            StatusLine statusLine = response.getStatusLine();
            if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();
                responseString = out.toString();
            } else{
                //Closes the connection.
                response.getEntity().getContent().close();
                throw new IOException(statusLine.getReasonPhrase());
            }
        } catch (UnsupportedEncodingException e1) {
            Log.e("UnsupportedEncodingException", e1.toString());
            e1.printStackTrace();
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String string_result) {
        super.onPostExecute(string_result);
        JSONArray results = null;
        Double[] lon,lat,co;

        if (string_result != null){
            try {
                JSONObject jsonObject = new JSONObject(string_result);

                if (jsonObject.getString("error").equals("null")){
                    //Parse JSON and get the path
                    results = jsonObject.getJSONArray("result");

                    int max = results.length();
                    lon = new Double[max];
                    lat = new Double[max];
                    co = new Double[max];

                    pathLatLongList = new LatLng[results.length()];

                    JSONArray array = results.getJSONArray(0);
                    lon[0] = array.getDouble(0);
                    lat[0] = array.getDouble(1);
                    co[0] = array.getDouble(2);

                    for (int i = 1; i < results.length(); i++){
                        array = results.getJSONArray(i);
                        lon[i] = array.getDouble(0);
                        lat[i] = array.getDouble(1);
                        co[i] = array.getDouble(2);

                        PolylineOptions path = new PolylineOptions();
                        path.width(5f);
                        if (co[i-1] < 2.5){
                            path.color(Color.rgb(0,0,255));
                        }else if (co[i-1] >= 2.5 && co[i-1] <= 5){
                            path.color(Color.rgb(0,189,0));
                        }else if (co[i-1] >= 5 && co[i-1] <= 7.5){
                            path.color(Color.rgb(255,231,54));
                        }else if (co[i-1] >= 7.5 && co[i-1] <= 10){
                            path.color(Color.rgb(249,126,2));
                        }else{
                            path.color(Color.rgb(0,255,0));
                        }
                        path.add(new LatLng(lat[i],lon[i])
                                ,new LatLng(lat[i-1],lon[i-1]));

                        COMap.map.addPolyline(path);
                    }
                    COMap.map.addMarker(COMap.start_position).showInfoWindow();
                    COMap.map.addMarker(COMap.start_position);

                    COMap.map.animateCamera(CameraUpdateFactory.
                            newLatLngZoom(new LatLng(lat[0], lon[0]),16.5f));

                    mex.quickMessage("Done!");

                }else {
                    mex.quickMessage(jsonObject.getString("error"));
                }

            } catch (JSONException e) {
                Log.e("JSONException", e.toString());
                //mex.quickMessage("Error! Couldn't download your data!");
            }
        }
        if (progressDialog != null){
            this.progressDialog.dismiss();
        }
    }
}