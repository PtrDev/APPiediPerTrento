package com.example.appiedipertrento;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.fedorvlasov.lazylist.ImageLoader;

import org.brickred.socialauth.Profile;
import org.brickred.socialauth.android.DialogListener;
import org.brickred.socialauth.android.SocialAuthAdapter;
import org.brickred.socialauth.android.SocialAuthError;
import org.brickred.socialauth.android.SocialAuthListener;

import java.io.UnsupportedEncodingException;

/**
 * Created by Pietro on 27/12/13.
 * Copyright Pietro 2014
 */

public class MyAir extends Fragment{

    private static final String ARG_SECTION_NUMBER = "section_number";
    public int SHARE_ACTIVITY = 30;
    public QuickMessage Mex;
    public String quality = "";

    boolean LOGIN = false;
    public int mShortAnimationDuration;
    public String facebook = SocialAuthAdapter.Provider.FACEBOOK.toString();
    public String google_plus = SocialAuthAdapter.Provider.GOOGLEPLUS.toString();

    public Profile profile = null;
    public SocialAuthAdapter adapter = null;
    public ImageLoader imageLoader;

    View login_layout = null;
    View profile_layout = null;

    //fragment 3
    Button login = null;
    TextView bottom = null;
    ImageView logo = null;

    //user profile
    TextView name = null;
    ImageView picture = null;
    Button share = null;
    ImageView provider_logo = null;

    public int color_provider = 0;


    public static MyAir newInstance(int sectionNumber) {
        MyAir fragment = new MyAir();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }


    public MyAir() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Mex = new QuickMessage(getActivity());

        mShortAnimationDuration = getResources().getInteger(
                android.R.integer.config_longAnimTime);

        adapter = new SocialAuthAdapter(new ResponseListener());
        adapter.addProvider(SocialAuthAdapter.Provider.FACEBOOK, R.drawable.facebook_new);
        //adapter.addProvider(SocialAuthAdapter.Provider.GOOGLEPLUS, R.drawable.google_plus_icon);
        //adapter.addCallBack(SocialAuthAdapter.Provider.GOOGLEPLUS, "https://www.example.com/oauth2callback");

        imageLoader = new ImageLoader(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment3, container, false);
        assert rootView != null;

        login_layout = rootView.findViewById(R.id.login_layout);
        profile_layout = rootView.findViewById(R.id.profile_layout);

        //fragment 3
        bottom = (TextView) rootView.findViewById(R.id.fr3_bottom);
        logo = (ImageView) rootView.findViewById(R.id.fr3_logo);
        login = (Button) rootView.findViewById(R.id.fr3_login);

        //user profile
        picture = (ImageView) rootView.findViewById(R.id.up_picture);
        name = (TextView) rootView.findViewById(R.id.up_profile_name);
        provider_logo = (ImageView) rootView.findViewById(R.id.up_logo);
        share = (Button) rootView.findViewById(R.id.up_share);

        adapter.enable(login);

        logged(LOGIN);
        setProfileLayout();

        return rootView;
    }

    public void logged(boolean isVisible){
        if (!isVisible){
            login_layout.setVisibility(View.VISIBLE);
            profile_layout.setVisibility(View.GONE);
        }else {
            login_layout.setVisibility(View.GONE);
            profile_layout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_fragment3,menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()== R.id.log_out){
            if (LOGIN){
                LOGIN = false;
                animation(false);
                profile = null;
                return true;
            }else {
                Mex.quickMessage("Not logged in!");
            }
        }else if (item.getItemId() == R.id.change_account){
            if (LOGIN){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Log out")
                        .setCancelable(true)
                        .setMessage("Log out from " + adapter.getCurrentProvider().getProviderId() + "?")
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                if (adapter.signOut(getActivity(),adapter.getCurrentProvider().getProviderId())){
                                    Mex.quickMessage("Goodbye!");
                                    LOGIN = false;
                                    animation(false);
                                    profile = null;
                                }
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //nothing
                            }
                        });
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }else {
                Mex.quickMessage("Not logged in!");
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void AlertDialogShow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),AlertDialog.THEME_HOLO_LIGHT);
        builder.setTitle("Error")
                .setCancelable(true)
                .setMessage("Connect your Sensordrone!")
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        //nothing
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void setProfileLayout() {
        if (profile != null){
            String provider = adapter.getCurrentProvider().getProviderId();
            String url = null;
            if (provider.equals(facebook)){
                url = profile.getProfileImageURL()+"?type=large";
                color_provider = Color.rgb(64,121,255);
                provider_logo.setImageResource(R.drawable.facebook_new);
                share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(getActivity(),ShareActivity.class);
                        intent.putExtra("color",color_provider);
                        startActivityForResult(intent, SHARE_ACTIVITY);
                    }
                });
            }else if (provider.equals(google_plus)){
                url = profile.getProfileImageURL();
                color_provider = Color.rgb(209,66,49);
                provider_logo.setImageResource(R.drawable.google_plus_icon);
            }
            name.setText(profile.getFirstName()+"\n"+profile.getLastName());
            imageLoader.DisplayImage(url,picture);
            share.setBackgroundColor(color_provider);
        }
    }

    public void animation(boolean login){
        if (login){

            // show Profile_layout
            profile_layout.setAlpha(0f);
            profile_layout.setVisibility(View.VISIBLE);

            profile_layout.animate()
                    .alpha(1f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(null);

            login_layout.animate()
                    .alpha(0f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            login_layout.setVisibility(View.GONE);
                        }
                    });
        }else {

            //show login_layout
            login_layout.setAlpha(0f);
            login_layout.setVisibility(View.VISIBLE);

            login_layout.animate()
                    .alpha(1f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(null);

            profile_layout.animate()
                    .alpha(0f)
                    .setDuration(mShortAnimationDuration)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            profile_layout.setVisibility(View.GONE);
                        }
                    });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        getActivity();
        if (requestCode == SHARE_ACTIVITY && resultCode == Activity.RESULT_OK){
            Bundle res = data.getExtras();
            assert res != null;
            String result = res.getString("param_result","");
            if (RealTimeCO.connected){
                quality = " The quality of the air where I am is "+RealTimeCO.quality_text+"!";
            }else {
                quality = " The quality of the air where I am is "+RealTimeCO.quality_text_average+"!";
            }
            try {
                adapter.updateStory(result,
                        "APPiedi Per Trento",
                        "Find a low-polluted walk in Trento!",
                        "Have a walk, ride your bike, run with your friends choosing a low-polluted path in Trento!"+ quality,
                        "https://www.facebook.com",
                        "https://fbcdn-photos-a-a.akamaihd.net/hphotos-ak-prn1/t39.2081/p128x128/851591_1375870322668300_17545461_n.png",
                        new MessageListener());
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                Log.d("update_story",e.getMessage());
            }
        }
    }

    public final class ResponseListener implements DialogListener {
        @Override
        public void onComplete(Bundle values) {
            Log.d("ShareButton", "Authentication Successful");

            // Get name of provider after authentication
            final String providerName = values.getString(SocialAuthAdapter.PROVIDER);
            Log.d("ShareButton", "Provider Name = " + providerName);

            profile = adapter.getUserProfile();
            Log.d("username",profile.getFullName());
            Mex.quickMessage("Welcome "+profile.getFirstName());

            LOGIN = true;
            animation(true);

            setProfileLayout();

        }

        @Override
        public void onError(SocialAuthError error) {
            Log.d("ShareButton", "Authentication Error: " + error.getMessage());
        }

        @Override
        public void onCancel() {
            Log.d("ShareButton", "Authentication Cancelled");
        }

        @Override
        public void onBack() {
            Log.d("Share-Button", "Dialog Closed by pressing Back Key");
        }

    }


    // To get status of message after authentication
    public final class MessageListener implements SocialAuthListener<Integer> {
        @Override
        public void onExecute(String provider, Integer status) {
            if (status == 200 || status == 201 || status == 204)
                Mex.quickMessage("posted on "+provider);
            else
                Mex.quickMessage("error posting on "+provider);
        }

        @Override
        public void onError(SocialAuthError error) {
            Log.d("Sharing", "Error: " + error.getMessage());
        }
    }

}