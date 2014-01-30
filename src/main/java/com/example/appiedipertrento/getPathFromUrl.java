package com.example.appiedipertrento;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
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
        JSONObject results = null;

        if (string_result != null){
            try {
                JSONObject jsonObject = new JSONObject(string_result);

                if (jsonObject.getString("error").equals("null")){
                    //Parse JSON and get the path

                    pathLatLongList = new LatLng[results.length()];
                    PolylineOptions path = new PolylineOptions();
                    path.width(5f);

                    for (int i = 0; i < results.length(); i++){
                    }

                    COMap.map.addPolyline(path);
                    //COMap.map.animateCamera(CameraUpdateFactory.newLatLng());

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