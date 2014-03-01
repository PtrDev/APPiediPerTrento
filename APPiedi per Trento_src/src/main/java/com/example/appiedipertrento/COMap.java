package com.example.appiedipertrento;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.maps.model.UrlTileProvider;
import com.google.maps.android.SphericalUtil;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

/**
 * Created by Pietro on 27/12/13.
 * Copyright Pietro 2014
 */

public class COMap extends Fragment{

    //private static final String SICURSKI_MAP_URL_FORMAT = "https://sicurskiwebportal.fbk.eu/geoserver/land/gwc/service/tms/1.0.0/sicur_ski_web%3Asicurski_mobile@EPSG%3A900913@png/{z}/{x}/{y}.png";
    private static final String SICURSKINEW_MAP_URL_FORMAT = "https://sicurskiwebportal.fbk.eu/geoserver/land/gwc/service/tms/1.0.0/skilo_basemap@EPSG%3A900913@jpg/{z}/{x}/{y}.jpg";

    private static final String ARG_SECTION_NUMBER = "section_number";
    private int FIND_PATH_ID = 37;
    public Boolean START = false;
    public Boolean LAYER = false;
    public Boolean PATH = false;

    public static MarkerOptions start_position,stop_position;

    public QuickMessage Mex = null;
    public Handler myHandler= null;
    MarkerOptions casa = null;
    MarkerOptions Start = null;
    MarkerOptions Stop = null;

    int num = 0;
    int delay = 5000;

    static GoogleMap map = null;

    CharSequence[] options = {/*"Ski layer",*/"Map","Satellite","Terrain","Hybrid"};

    //LatLng pos_casa = new LatLng(46.106883,11.113357);
    LatLng Trento = new LatLng(46.0667, 11.1167);

    GoogleMap.InfoWindowAdapter adapter = new GoogleMap.InfoWindowAdapter() {
        @Override
        public View getInfoWindow(Marker marker) {
            return null;
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }
    };

    Runnable myRunnable = new Runnable() {
        @Override
        public void run() {
            LatLng location = new LatLng(map.getMyLocation().getLatitude(),map.getMyLocation().getLongitude());
            float quality = RealTimeCO.quality_value;
            String qtext = RealTimeCO.quality_text;
            BitmapDescriptor pointer;
            if (qtext.equals("bad")){
                pointer = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            }else if (qtext.equals("moderate")){
                pointer = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            }else if (qtext.equals("good")){
                pointer = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            }else{
                qtext = "not connected";
                quality = 0;
                pointer = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE);
            }
            map.moveCamera(CameraUpdateFactory.newLatLng(location));
            map.addMarker(new MarkerOptions()
                    .position(location)
                    .title(qtext.toUpperCase())
                    .snippet(String.format("%.1f",quality)+ " ppm")
                    .icon(pointer)).showInfoWindow();
            num++;
            myHandler.postDelayed(myRunnable,delay);
        }
    };

    public static COMap newInstance(int sectionNumber) {
        COMap fragment = new COMap();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public static LatLng myPosition(){
        if (map != null){
            if(map.getMyLocation() != null){
                return new LatLng(map.getMyLocation().getLatitude(),map.getMyLocation().getLongitude());
            }
        }
        return new LatLng(-999,-999);
    }

    public COMap() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        try {
            MapsInitializer.initialize(getActivity());
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
        Mex = new QuickMessage(getActivity());
        myHandler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment2, container, false);
        assert rootView != null;
        map = ((MapFragment) getActivity().getFragmentManager()
                .findFragmentById(R.id.fr2_map1)).getMap();

        if (map == null){

            //CANNOT LOAD GOOGLE MAPS
            View view = inflater.inflate(R.layout.nobluetooth, container, false);
            assert  view != null;

            TextView error = (TextView) view.findViewById(R.id.error_text);
            error.setText(getString(R.string.nGms_text));

            return view;

        }else{

            //find path
            Button find = (Button) rootView.findViewById(R.id.fr2_find_path);
            find.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),Find_Path.class);
                    startActivityForResult(intent, FIND_PATH_ID);
                }
            });

            //NEW MARKER HOME
            casa = new MarkerOptions()
                    .title("Trento, Italy")
                    .snippet("Home sweet home")
                    .position(Trento)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));

            //LOAD MAP
            //setUpMapIfNeeded();
            map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                @Override
                public void onMapClick(LatLng latLng) {
                    if (!START){
                        Start = new MarkerOptions()
                                .title("Start")
                                .position(latLng)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                        map.addMarker(Start).showInfoWindow();
                        START = true;
                    }else {
                        Stop = new MarkerOptions()
                                .title("Stop")
                                .position(latLng)
                                .icon(BitmapDescriptorFactory
                                        .defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));
                        map.addMarker(Stop).showInfoWindow();
                        START = false;
                        map.addPolyline(new PolylineOptions()
                                .add(Start.getPosition(),Stop.getPosition())
                                .color(Color.LTGRAY)
                                .width(5f));
                        double distance = SphericalUtil.computeDistanceBetween(Start.getPosition(),Stop.getPosition());
                        Mex.quickMessage(String.format("Distance: %.2f meters",distance));
                    }

                }
            });

            map.setInfoWindowAdapter(adapter);
            map.setMyLocationEnabled(true);
            map.setIndoorEnabled(false);

            map.moveCamera(CameraUpdateFactory.newLatLngZoom(Trento, 15));
            map.addMarker(casa).showInfoWindow();


            return rootView;
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void SelectMapType(){
        int CheckedItem;
        if (LAYER){
            CheckedItem = GoogleMap.MAP_TYPE_NONE;
        }else {
            CheckedItem = map.getMapType()-1;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle(getString(R.string.fr2_map_type_select))
                .setCancelable(true)
                .setSingleChoiceItems(options, CheckedItem, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int selected) {
                        if (selected == -1) {
                            LAYER = true;
                            setUpMapIfNeeded();
                        } else {
                            LAYER = false;
                            map.setMapType(selected+1);
                        }
                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //OK BUTTON
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment2,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.map_type){
            SelectMapType();
            return true;
        }else if (item.getItemId()== R.id.clear_map){
            map.clear();
            START = false;
            map.addMarker(casa).showInfoWindow();
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(casa.getPosition(),15));
            return true;
        }else if (item.getItemId()== R.id.refresh_path){
            pathRequest();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void setUpMapIfNeeded() {
        if (LAYER) {
            if (map != null) {
                setUpMap();
                MyUrlTileProvider mTileProvider = new MyUrlTileProvider(256,
                        256, SICURSKINEW_MAP_URL_FORMAT);
                map.addTileOverlay(new TileOverlayOptions()
                        .tileProvider(mTileProvider));

                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

                map.addTileOverlay(new TileOverlayOptions()
                        .tileProvider(new CustomMapTileProvider(getResources()
                                .getAssets())));
            }
        }
    }

    public void setUpMap() {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        TileProvider tileProvider = new UrlTileProvider(256, 256) {
            @Override
            public synchronized URL getTileUrl(int x, int y, int zoom) {
                // The moon tile coordinate system is reversed. This is not
                // normal.
                int reversedY = (1 << zoom) - y - 1;
                String s = String.format(Locale.US, SICURSKINEW_MAP_URL_FORMAT, zoom, x, reversedY);
                URL url;
                try {
                    url = new URL(s);
                } catch (MalformedURLException e) {
                    throw new AssertionError(e);
                }
                return url;
            }
        };
        map.addTileOverlay(new TileOverlayOptions().tileProvider(tileProvider));
    }

    public void pathRequest(){
        Double lat_start = start_position.getPosition().latitude;
        Double lon_start = start_position.getPosition().longitude;
        Double lat_end = stop_position.getPosition().latitude;
        Double lon_end = stop_position.getPosition().longitude;
        new getPathFromUrl(getActivity(),true)
                .execute("https://spatialdb.fbk.eu/appiedi/pathfinder/"+lon_start+"/"+lat_start+"/"+lon_end+"/"+lat_end);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == FIND_PATH_ID && resultCode == Activity.RESULT_OK){
            Bundle res = data.getExtras();
            assert res != null;
            int mod = res.getInt("mod",0);
            if (mod == 1){
                //modality1 -> linear path, input: start and destination
                PATH = true;
                pathRequest();
            }else if (mod == 2){
                //modality2 -> circular path, input: time
                int length = res.getInt("length", 0);
                //start_pos
                start_position.snippet(Integer.toString(length)+"km");
                map.addMarker(start_position).showInfoWindow();
                map.animateCamera(CameraUpdateFactory.newLatLng(start_position.getPosition()));
            }
        }
    }


    /*
     *
     * Find the layer from INTERNET SERVICE
     *
     */

    class MyUrlTileProvider extends UrlTileProvider {

        public String baseUrl;

        public MyUrlTileProvider(int width, int height, String url) {
            super(width, height);
            this.baseUrl = url;
        }

        @Override
        public URL getTileUrl(int x, int y, int zoom) {
            int reversedY = (1 << zoom) - y - 1;
            try {
                return new URL(baseUrl.replace("{z}", "" + zoom)
                        .replace("{x}", "" + x).replace("{y}", "" + reversedY));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
