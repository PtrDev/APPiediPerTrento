package com.example.appiedipertrento;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.TextView;

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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by Pietro on 07/01/14.
 * Copyright Pietro 2014
 */

public class getJSONFromUrl extends AsyncTask<String, String, String>{

    ProgressDialog progressDialog;
    Activity activity;
    QuickMessage mex;
    boolean progress;

    public getJSONFromUrl(Activity a,boolean activeProgress){
        activity = a;
        progressDialog = new ProgressDialog(activity);
        progress = activeProgress;
        mex = new QuickMessage(activity);
    }

    protected void onPreExecute() {
        if (progress){
            progressDialog.setMessage("Downloading your data...");
            progressDialog.show();
            progressDialog.setCancelable(true);
            progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    //getJSONFromUrl.this.cancel(true);
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
            mex.quickMessage("Error! Couldn't connect to the Internet!");
        } catch (ClientProtocolException e2) {
            Log.e("ClientProtocolException", e2.toString());
            mex.quickMessage("Error! Couldn't connect to the Internet!");
            e2.printStackTrace();
        } catch (IllegalStateException e3) {
            Log.e("IllegalStateException", e3.toString());
            e3.printStackTrace();
            mex.quickMessage("Error! Couldn't connect to the Internet!");
        } catch (IOException e4) {
            Log.e("IOException", e4.toString());
            e4.printStackTrace();
            mex.quickMessage("Error! Couldn't connect to the Internet!");
        }
        return responseString;
    }

    @Override
    protected void onPostExecute(String string_result) {
        super.onPostExecute(string_result);
        JSONObject results,trend_average;

        if (string_result != null){
            try {
                JSONObject jsonObject = new JSONObject(string_result);

                if (jsonObject.getString("error").equals("null")){

                    SimpleDateFormat dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);
                    Calendar calendar = Calendar.getInstance();

                    results = jsonObject.getJSONObject("results");
                    trend_average = results.getJSONObject("trend_average");

                    String total = results.getString("total");
                    setBackground(RealTimeCO.texts[0],total);

                    String weekly = results.getString("weekly");
                    setBackground(RealTimeCO.texts[1],weekly);

                    String dow = results.getString("dow");
                    setBackground(RealTimeCO.texts[2],dow);

                    String daily_average = results.getString("daily_average");
                    setBackground(RealTimeCO.texts[3],daily_average);

                    String[] weekly_average = new String[8];

                    for (int i = 0; i <8; i++){
                        calendar.add(Calendar.DATE,-1);
                        String day = dayFormat.format(calendar.getTime());
                        weekly_average[i] = trend_average.getString(day);
                    }
                    setBackground(RealTimeCO.texts[4],weekly_average[0]);

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

    public void setBackground(TextView textView, String str_value){
        double value = Double.parseDouble(str_value);
        if (value < 5 && value >= -5){
            textView.setBackgroundResource(R.drawable.shade_green);
            textView.setText(String.format("%.3f ppm", value));
            RealTimeCO.quality_text_average = "good";
        }else if (value >= 5 && value <= 10){
            textView.setBackgroundResource(R.drawable.shade_orange);
            textView.setText(String.format("%.3f ppm", value));
            RealTimeCO.quality_text_average = "moderate";
        }else if (value > 10){
            textView.setBackgroundResource(R.drawable.shade_red);
            textView.setText(String.format("%.3f ppm", value));
            RealTimeCO.quality_text_average = "bad";
        }else {
            textView.setBackgroundResource(R.drawable.shade_green);
            textView.setText("???");
            RealTimeCO.quality_text_average = "???";
        }

    }
}