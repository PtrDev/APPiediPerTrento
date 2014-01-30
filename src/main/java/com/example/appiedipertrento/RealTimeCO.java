package com.example.appiedipertrento;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;
import com.sensorcon.sdhelper.SDHelper;
import com.sensorcon.sensordrone.CoreDrone;
import com.sensorcon.sensordrone.DroneEventListener;
import com.sensorcon.sensordrone.DroneEventObject;
import com.sensorcon.sensordrone.DroneStatusListener;
import com.sensorcon.sensordrone.android.Drone;
import com.sensorcon.sensordrone.android.tools.DroneQSStreamer;
import com.sensorcon.sensordrone.android.tools.DroneStreamer;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;


/**
 * Created by Pietro on 27/12/13.
 * Copyright Pietro 2014
 */

public class RealTimeCO extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static final String PROVA = "Prova" ;
    private static String LAST_MAC = "last_mac";
    private SharedPreferences MacAdress;
    public static boolean LAST = true;
    public static float quality_value = -10;
    public static String quality_text = "error";
    public static String quality_text_average = "error";
    public static boolean connected = false;

    public QuickMessage Mex;
    public ProgressDialog progressDialog = null;
    public btEnabledClass myBtConnecter;
    public String weekDay;
    public Handler myHandler;
    public LatLng Trento = new LatLng(46.0667, 11.1167);

    BluetoothAdapter myBluetooth;
    private boolean ENABLED = true;
    private int BLUETHOOTH_ENABLED = 1;
    public static int LENGTH = 5;

    public Drone myDrone;
    public SDHelper myHelper;
    public DroneEventListener myListener;
    public DroneStatusListener myDListener;
    public DroneStreamer myBlinker;
    public DroneQSStreamer[] streamerArray = new DroneQSStreamer[2];
    public int streamingRate = 1000;
    public int blinkerRate = 500;
    public boolean led = true;

    ScaleAnimation hide6;
    Button[] buttons = new Button[LENGTH+1];
    public static TextView[] texts = new TextView[LENGTH+1];
    int[] button_id = new int[]{R.id.fr1_mean_absolute,
            R.id.fr1_mean_weekly,
            R.id.fr1_mean_daily,
            R.id.fr1_mean_yesterday,
            R.id.fr1_mean_today,
            R.id.fr1_myDrone};
    int[] text_id = new int[]{R.id.fr1_text_absolute,
            R.id.fr1_text_weekly,
            R.id.fr1_text_daily,
            R.id.fr1_text_yesterday,
            R.id.fr1_text_today,
            R.id.fr1_text_drone};
    int mAnimationDuration = 500;

    Runnable waitLatLong = new Runnable() {
        @Override
        public void run() {
            Double lat = COMap.myPosition().latitude;
            Double lon = COMap.myPosition().longitude;
            if ((lat <= Trento.latitude+2 && lat >= Trento.latitude-2) &&
                    (lon <= Trento.longitude+2 && lon >= Trento.longitude-2)){
                myHandler.removeCallbacks(waitLatLong);
                new getJSONFromUrl(getActivity(),true)
                        .execute("https://spatialdb.fbk.eu/appiedi/covalue/"+lon+"/"+lat);
            }else {
                myHandler.postDelayed(waitLatLong,10);
            }
        }
    };

    public static RealTimeCO newInstance(int sectionNumber) {
        RealTimeCO fragment = new RealTimeCO();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        args.putString(PROVA, "boh");
        fragment.setArguments(args);
        return fragment;
    }

    public RealTimeCO() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", Locale.UK);
        Calendar calendar = Calendar.getInstance();
        weekDay = dayFormat.format(calendar.getTime());
        weekDay = weekDay+"\nAverage";

        myHandler = new Handler();

        MacAdress = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String lastMac = MacAdress.getString(LAST_MAC,"");
        if (lastMac.equals("") || lastMac == null){
            SharedPreferences.Editor prefEditor = MacAdress.edit();
            prefEditor.putString(LAST_MAC, getString(R.string.Mac_base));
            prefEditor.commit();
        }
        Mex = new QuickMessage(getActivity());
        myBtConnecter = new btEnabledClass();
        myHelper = new SDHelper();
        myDrone = new Drone();
        myBlinker = new DroneStreamer(myDrone,blinkerRate) {
            @Override
            public void repeatableTask() {
                if (led)
                {
                    myDrone.setRightLED(255,0,0);
                    myDrone.setLeftLED(0,255,0);
                }
                else
                    myDrone.setLEDs(0,0,0);
                led = !led;
            }
        };
        streamerArray[0] = new DroneQSStreamer(myDrone, CoreDrone.QS_TYPE_PRECISION_GAS);
        myListener = new DroneEventListener() {
            @Override
            public void capacitanceMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void adcMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void rgbcMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void pressureMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void altitudeMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void irTemperatureMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void humidityMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void temperatureMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void reducingGasMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void oxidizingGasMeasured(DroneEventObject droneEventObject) {

            }

            @Override
            public void precisionGasMeasured(DroneEventObject droneEventObject) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        texts[LENGTH].setText(String.format("%.3f",myDrone.precisionGas_ppmCarbonMonoxide)+ " ppm");
                        float value = myDrone.precisionGas_ppmCarbonMonoxide;
                        quality_value = value;
                        if (value < 5){
                            texts[LENGTH].setBackgroundResource(R.drawable.shade_green);
                            quality_text = getString(R.string.fr1_quality_good);
                        }else if (value >= 5 && value <= 10){
                            texts[LENGTH].setBackgroundResource(R.drawable.shade_orange);
                            quality_text = getString(R.string.fr1_quality_moderate);
                        }else{
                            texts[LENGTH].setBackgroundResource(R.drawable.shade_red);
                            quality_text = getString(R.string.fr1_quality_bad);
                        }
                    }
                });
                streamerArray[0].streamHandler.postDelayed(streamerArray[0],streamingRate);
            }

            @Override
            public void uartRead(DroneEventObject droneEventObject) {

            }

            @Override
            public void i2cRead(DroneEventObject droneEventObject) {

            }

            @Override
            public void usbUartRead(DroneEventObject droneEventObject) {

            }

            @Override
            public void customEvent(DroneEventObject droneEventObject) {

            }

            @Override
            public void connectEvent(DroneEventObject droneEventObject) {
                SharedPreferences.Editor prefEditor = MacAdress.edit();
                prefEditor.putString(LAST_MAC, myDrone.lastMAC);
                prefEditor.commit();
                stopWaiting();
                Mex.quickMessage("Connected!");
                connected = true;
                buttons[LENGTH].startAnimation(hide6);
                buttons[LENGTH].setOnClickListener(null);
                myBlinker.start();
                streamerArray[0].enable();
                myDrone.quickEnable(CoreDrone.QS_TYPE_PRECISION_GAS);
            }

            @Override
            public void disconnectEvent(DroneEventObject droneEventObject) {
                streamerArray[0].disable();
                quality_value = -10;
                quality_text = "error";
                connected = false;
                myDrone.quickDisable(CoreDrone.QS_TYPE_PRECISION_GAS);
                Mex.quickMessage("Disconnected!");
            }

            @Override
            public void connectionLostEvent(DroneEventObject droneEventObject) {
                myBlinker.stop();
                quality_value = -10;
                quality_text = "error";
                connected = false;
                Mex.quickMessage("Connection lost");
                String lastMac = MacAdress.getString(LAST_MAC,"");
                if (myBluetooth.isEnabled() && myDrone.btConnect(lastMac))
                    Mex.quickMessage("Reconnected");
            }

            @Override
            public void unknown(DroneEventObject droneEventObject) {

            }
        };
        myDListener = new DroneStatusListener() {
            @Override
            public void capacitanceStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void adcStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void rgbcStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void pressureStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void altitudeStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void irStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void humidityStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void temperatureStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void oxidizingGasStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void reducingGasStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void precisionGasStatus(DroneEventObject droneEventObject) {
                if (myDrone.precisionGasStatus)
                    streamerArray[0].run();
            }

            @Override
            public void batteryVoltageStatus(DroneEventObject droneEventObject) {
                //measure battery voltage
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        AlertDialog.Builder build = new AlertDialog.Builder(getActivity(),2);
                        build.setTitle("Battery Voltage")
                                .setMessage(String.format("%.2f",myDrone.batteryVoltage_Volts)+" V")
                                .setCancelable(true)
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                    }
                                });
                        AlertDialog dialog = build.create();
                        dialog.show();
                    }
                });
            }

            @Override
            public void chargingStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void customStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void unknownStatus(DroneEventObject droneEventObject) {

            }

            @Override
            public void lowBatteryStatus(DroneEventObject droneEventObject) {

            }
        };
        myDrone.registerDroneListener(myListener);
        myDrone.registerDroneListener(myDListener);
        if (myDrone.isConnected)
            myDrone.disconnect();
        myBluetooth = BluetoothAdapter.getDefaultAdapter();
        if (myBluetooth == null){
            //Bluetooth not available
            NoBluetoothAlert();
            ENABLED = false;
        }
        waitLatLong.run();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment1, container, false);
        assert rootView != null;

        for (int i = 0; i< buttons.length; i++){
            buttons[i] = (Button) rootView.findViewById(button_id[i]);
            texts[i] = (TextView) rootView.findViewById(text_id[i]);
            buttons[i].setTag(i);
            texts[i].setTag(i+10);
            if (i == 2){
                buttons[i].setText(weekDay);
            }
            if (i % 2 == 1){
                buttons[i].setOnClickListener(new ButtonClickListener(buttons[i],true));
                texts[i].setOnClickListener(new TextClickListener(texts[i],true));
            }else {
                buttons[i].setOnClickListener(new ButtonClickListener(buttons[i],false));
                texts[i].setOnClickListener(new TextClickListener(texts[i],false));
            }
        }

        return rootView;
    }

    public void newConnection(){
        SharedPreferences.Editor prefEditor = MacAdress.edit();
        prefEditor.putString(LAST_MAC, getString(R.string.Mac_base));
        prefEditor.commit();
        myHelper.scanToConnect(myDrone, getActivity(), getActivity(), true, progressDialog);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == BLUETHOOTH_ENABLED && resultCode == Activity.RESULT_OK){
            myBtConnecter.btEnabled();
        }else if (requestCode == BLUETHOOTH_ENABLED && resultCode == Activity.RESULT_CANCELED){
            stopWaiting();
            AlertDialog.Builder build = new AlertDialog.Builder(getActivity(),2);
            build.setTitle("ERROR!")
                    .setMessage(getString(R.string.fr1_bt_not_available))
                    .setCancelable(false)
                    .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                        }
                    });
            AlertDialog dialog = build.create();
            dialog.show();
        }
    }

    @Override
    public void onDestroy() {
        if (myDrone.isConnected){
            myBlinker.stop();
            myDrone.setLEDs(0,0,0);
            myDrone.disconnect();
        }
        super.onDestroy();
    }

    public void startWaiting() {
        progressDialog = ProgressDialog.show(getActivity(), "", "Connecting...", true, true);
    }

    public void stopWaiting() {
        if (progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    public void NoBluetoothAlert(){
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        build.setTitle("ERROR!")
                .setMessage("Bluetooth not available")
                .setCancelable(false)
                .setPositiveButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                    }
                });
        AlertDialog dialog = build.create();
        dialog.show();
    }

    public void SensordroneConnectDialog(){
        AlertDialog.Builder build = new AlertDialog.Builder(getActivity());
        build.setTitle("Sensordrone")
                .setMessage("Connect your Sensordrone?")
                .setCancelable(true)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        startWaiting();
                        myBtConnecter.btEnabled();
                    }
                })
                .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // do nothing
                    }
                });
        AlertDialog dialog = build.create();
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment1,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.change_device){
            LAST = false;
            if (myDrone.isConnected){
                myBlinker.stop();
                myDrone.setLEDs(0,0,0);
                myDrone.disconnect();
            }
            myBtConnecter.btEnabled();
            return true;
        }else if (item.getItemId() == R.id.battery_voltage){
            if (myDrone.isConnected){
                myDrone.measureBatteryVoltage();
            }else {
                Mex.quickMessage("Sensordrone not connected");
            }
        }else if (item.getItemId() == R.id.disconnect){
            if (myDrone.isConnected){
                myBlinker.stop();
                myDrone.setLEDs(0,0,0);
                myDrone.disconnect();
            }else {
                Mex.quickMessage("Sensordrone not connected");
            }
        }else if (item.getItemId() == R.id.refresh){
            waitLatLong.run();
        }else if (item.getItemId() == R.id.trend){
            Intent intent = new Intent(getActivity(),CO_Trend.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    public class btEnabledClass{

        public void btEnabled(){
            if (ENABLED){
                if (!myBluetooth.isEnabled()){
                    Intent btEnabler = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(btEnabler, BLUETHOOTH_ENABLED);
                }else {
                    if (LAST){
                        String lastMac = MacAdress.getString(LAST_MAC,"");
                        if (!myDrone.btConnect(lastMac)){
                            newConnection();
                        }
                    }else {
                        LAST = true;
                        newConnection();
                    }
                }
            }else {
                NoBluetoothAlert();
            }
        }
    }


    /*
     * BUTTON LISTENER + START ANIMATION
     *
     */

    public class ButtonClickListener implements View.OnClickListener{

        private Button button;
        private boolean left;

        public ButtonClickListener(Button b,boolean l){
            button = b;
            left = l;
        }

        @Override
        public void onClick(View view) {
            ScaleAnimation hide;
            if (left){
                hide = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.5f,
                        Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            }else {
                hide = new ScaleAnimation(1.0f, 0.0f, 1.0f, 0.5f,
                        Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            }
            int tag = (Integer) button.getTag();
            if (tag == 5) {
                // Connect sensordrone
                if (!myDrone.isConnected) {
                    hide6 = hide;
                    SensordroneConnectDialog();
                }
            }
            hide.setDuration(mAnimationDuration);
            hide.setAnimationListener(new Listener(button, false, left));
            if (tag != LENGTH || myDrone.isConnected){
                button.startAnimation(hide);
                button.setOnClickListener(null);
            }
        }
    }

    /*
     *
     * TEXT VIEW LISTENER * START ANIMATION
     *
     */

    public class TextClickListener implements View.OnClickListener{

        private TextView textView;
        private boolean left;
        private int tag;

        public TextClickListener(TextView b,boolean l){
            textView = b;
            left = l;
            tag = (Integer) textView.getTag()-10;
        }

        @Override
        public void onClick(View view) {
            ScaleAnimation show;
            if (left){
                show = new ScaleAnimation(0.0f, 1.0f, 0.5f, 1.0f,
                        Animation.RELATIVE_TO_SELF,1.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            }else {
                show = new ScaleAnimation(0.0f, 1.0f, 0.5f, 1.0f,
                        Animation.RELATIVE_TO_SELF,0.0f, Animation.RELATIVE_TO_SELF, 0.5f);
            }
            show.setDuration(mAnimationDuration);
            show.setAnimationListener(new Listener(buttons[tag], true, left));
            buttons[tag].setVisibility(View.VISIBLE);
            buttons[tag].startAnimation(show);
        }
    }

    /*
     *
     *  ANIMATION LISTENER
     *
     */

    public class Listener implements Animation.AnimationListener{

        private Button button;
        private boolean show;
        private boolean left;

        public Listener(Button b, boolean shown, boolean l){
            button = b;
            show = shown;
            left = l;
        }

        @Override
        public void onAnimationStart(Animation animation) {

        }

        @Override
        public void onAnimationEnd(Animation animation) {
            if (show){
                button.setVisibility(View.VISIBLE);
                button.setOnClickListener(new ButtonClickListener(button,left));
            }else {
                button.setVisibility(View.INVISIBLE);
            }
        }

        @Override
        public void onAnimationRepeat(Animation animation) {

        }
    }
}
