package com.example.appiedipertrento;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.androidplot.xy.BoundaryMode;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.SimpleXYSeries;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYStepMode;

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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;

import static com.androidplot.xy.SimpleXYSeries.ArrayFormat;

/**
 * Created by Pietro on 29/01/14.
 * Copyright Pietro 2014
 */
public class CO_Trend extends FragmentActivity {

    public static int PICK_DATE = 81;
    public XYPlot myPlot = null;
    SimpleXYSeries values = null;
    SimpleXYSeries average_values = null;
    public static String starting_date;
    public static String end_date;
    public static Calendar calendar;
    public static SimpleDateFormat dayFormat;
    public static int year,month,day;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.co_trendlayout);

        calendar = Calendar.getInstance();
        dayFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.UK);

        // Graph data
        values = new SimpleXYSeries("CO Trend");
        average_values = new SimpleXYSeries("Average Value");

        // Graph formatting
        myPlot = (XYPlot) findViewById(R.id.mySimpleXYPlot);
        myPlot.setMarkupEnabled(false);

        // This constructor has added an option. Passing it an extra null still seems to work
        // Line, Vertex, Fill, PointLabelFormatter
        //myPlot.getLayoutManager().remove(myPlot.getLegendWidget());
        myPlot.getTitleWidget().setText("CO Average");
        myPlot.getGraphWidget().setMargins(0, 25, 5, 15);
        myPlot.setRangeLabel("CO Values");

        Intent intent = new Intent(this,PickDate.class);
        startActivityForResult(intent, PICK_DATE);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.co_trend_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.range) {
            Intent intent = new Intent(this,PickDate.class);
            startActivityForResult(intent, PICK_DATE);
            return true;
        }else if (id == R.id.refresh_graph){
            calendar.set(year,month,day);
            starting_date = dayFormat.format(calendar.getTime());
            calendar.add(Calendar.DATE,-1);
            new GetTrendFromUrl(this,true)
                    .execute("https://spatialdb.fbk.eu/appiedi/trendaverage/"+starting_date +"/"+end_date);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_DATE && resultCode == RESULT_OK){
            calendar.set(year, month, day);
            calendar.add(Calendar.DATE,-1);
            new GetTrendFromUrl(this,true)
                    .execute("https://spatialdb.fbk.eu/appiedi/trendaverage/"+starting_date +"/"+end_date);
        }
    }

    public LineAndPointFormatter setBackground(double value){
        LineAndPointFormatter F2;
        if (value < 5){
            F2 = new LineAndPointFormatter(Color.rgb(0, 189, 0), Color.rgb(0, 189, 0), null,null);
        }else if (value >= 5 && value <= 10){
            F2 = new LineAndPointFormatter(Color.rgb(249, 126, 2), Color.rgb(249, 126, 2), null,null);
        }else{
            F2 = new LineAndPointFormatter(Color.rgb(255, 0, 0), Color.rgb(255, 0, 0), null,null);
        }
        return F2;
    }

    class GetTrendFromUrl extends AsyncTask<String, String, String> {

        ProgressDialog progressDialog;
        Activity activity;
        QuickMessage mex;
        boolean progress;

        public GetTrendFromUrl(Activity a, boolean activeProgress){
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
                        GetTrendFromUrl.this.cancel(true);
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
            JSONObject results;

            if (string_result != null){
                try {
                    JSONObject jsonObject = new JSONObject(string_result);

                    if (jsonObject.getString("error:").equals("null")){

                        results = jsonObject.getJSONObject("results");

                        if (results.length() > 0){
                            Double[] co_trend = new Double[results.length()];
                            Double average = 0.0;
                            Double max = 0.0;
                            Double min = 0.0;

                            for (int i = 0; i < results.length(); i++){
                                calendar.add(Calendar.DATE,1);
                                String date = dayFormat.format(calendar.getTime());
                                co_trend[i] = results.getDouble(date);
                                if (co_trend[i] == -9999.0){
                                    co_trend[i] = 0.0;
                                }else {
                                    if (co_trend[i] > max){
                                        max = co_trend[i];
                                    }
                                    if (co_trend[i] < min){
                                        min = co_trend[i];
                                    }
                                }
                                average = average + co_trend[i];
                            }
                            average = average/co_trend.length;
                            Double[] val = new Double[co_trend.length];
                            for (int i = 0; i < results.length(); i++){
                                val[i] = average;
                            }
                            LineAndPointFormatter l2F = setBackground(average);
                            LineAndPointFormatter lF = new LineAndPointFormatter(
                                    Color.rgb(0, 0, 0), Color.rgb(0, 0, 255), null,null);
                            myPlot.addSeries(values, lF);
                            myPlot.addSeries(average_values, l2F);

                            // Y axis
                            myPlot.setRangeBoundaries(min - 1, max + 1, BoundaryMode.FIXED);
                            myPlot.setRangeStepValue(20);

                            // X axis
                            myPlot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, co_trend.length);
                            myPlot.setDomainStepValue(co_trend.length / 10);
                            myPlot.setDomainValueFormat(new DecimalFormat("0"));
                            myPlot.setDomainLabel(String.format("Average: %.2f ppm",average));

                            values.setModel(Arrays.asList(co_trend), ArrayFormat.Y_VALS_ONLY);
                            average_values.setModel(Arrays.asList(val), ArrayFormat.Y_VALS_ONLY);

                            myPlot.redraw();

                            mex.quickMessage("Done!");
                        }

                    }else {
                        mex.quickMessage("errror: "+jsonObject.getString("error"));
                    }
                } catch (JSONException e) {
                    Log.e("JSONException", e.toString());
                    mex.quickMessage("error: "+e.getMessage());
                }
            }
            if (progressDialog != null){
                this.progressDialog.dismiss();
            }
        }
    }
}
