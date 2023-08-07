package com.xsteinlab.sgbustimingwidget;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.xsteinlab.sgbustimingwidget.database.DBHandler;
import com.xsteinlab.sgbustimingwidget.fragment.ProfileFragment;
import com.xsteinlab.sgbustimingwidget.fragment.SavedFragment;
import com.xsteinlab.sgbustimingwidget.fragment.SearchFragment;
import com.xsteinlab.sgbustimingwidget.network.NetworkEngine;
import com.xsteinlab.sgbustimingwidget.utils.Utils;
import com.xsteinlab.sgbustimingwidget.widget.BusTimingWidget;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.w3c.dom.Text;


//TODO fix json error
//Make function to search us bus stop code to bus stop name

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;
    private SearchFragment searchFragment;
    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    public String SP_BATTERY_REQUEST = "SP_BATTERY_REQUEST";

    public boolean battery_requested;
    private SharedPreferences mSharedPreference;

    public BottomNavigationView getBottomNavigationView(){
        return bottomNavigationView;
    }

    public void displaySearchFragmentResult(String busStopName, String busNo){
        if(searchFragment == null){
            searchFragment = new SearchFragment(dbHandler, networkEngine);
        }
        Bundle args = new Bundle();
        args.putString(SearchFragment.BUNDLE_PRE_BUS_STOP_NAME, busStopName );
        args.putString(SearchFragment.BUNDLE_PRE_BUS_NO, busNo);
        searchFragment.setArguments(args);
        getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, searchFragment).commit();
    }

//    private void openBatterySettings()
//    {
//        Intent battery_intent = new Intent();
//        battery_intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
//        this.startActivity(battery_intent);
//    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

//        if (!Environment.isExternalStorageManager()){
//            Intent getpermission = new Intent();
//            getpermission.setAction(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
//            startActivity(getpermission);
//        }

        Utils.localLogging("Oh call me maybe");
//        Utils.logToFile("Main Activity Started");


        mSharedPreference = PreferenceManager.getDefaultSharedPreferences(this);
        battery_requested = mSharedPreference.getBoolean(SP_BATTERY_REQUEST, false);


        // Define bus stop metatdata DB
        dbHandler = new DBHandler(MainActivity.this);


        // Define layout
        setContentView(R.layout.activity_main);

        // Define component
        networkEngine = new NetworkEngine(this, dbHandler);



        if (dbHandler.CountDbRows(DBHandler.BUS_STOP_TABLE) == 0){
            Utils.localLogging("Calling get busmetadata");
            networkEngine.GetBusStopMetadata(this, dbHandler);
        }

        toolbar = findViewById(R.id.topAppBar);


        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.about:
                        AlertDialog alertDialog = aboutAlertDialogP1();
                        alertDialog.show();
                        break;

                    case R.id.reload_db:
                        networkEngine.GetBusStopMetadata(MainActivity.this, dbHandler);
                        break;
                }
                return true;
            }
        });

        searchFragment = new SearchFragment(dbHandler, networkEngine);

        bottomNavigationView =  findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.search_page) {
//                Utils.localLogging("SIUZETE");
//                if(searchFragment.getBusInfoItems() != null){
//                    Utils.localLogging(searchFragment.getBusInfoItems().size());
//                }
                if(searchFragment == null){
                    searchFragment = new SearchFragment(dbHandler, networkEngine);
                }
                selectedFragment = searchFragment;
//                if(searchFragment.getBusInfoAdapter() != null){
//                    searchFragment.getBusInfoAdapter().notifyDataSetChanged();
//                }
            }
            else if (itemId == R.id.saved_page) {
                selectedFragment = new SavedFragment(dbHandler, networkEngine);
            }
            else if (itemId == R.id.profile_page) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, selectedFragment).commit();
            }
            return true;
        });




        // Set fragment
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Intent intent = getIntent();
        String fragmentStr = intent.getStringExtra(BusTimingWidget.EXTRA_FRAGMENT_STRING);

        if(fragmentStr!= null){
            if(fragmentStr.equals("saved_arrival")){
                ft.replace(R.id.flFragment, new SavedFragment(dbHandler, networkEngine));
                ft.commit();
                bottomNavigationView.setSelectedItemId(R.id.saved_page);
            }else{
                ft.replace(R.id.flFragment, new SearchFragment(dbHandler, networkEngine));
                ft.commit();
                bottomNavigationView.setSelectedItemId(R.id.search_page);
            }
        }
        else if (savedInstanceState == null) {
            ft.replace(R.id.flFragment, new SearchFragment(dbHandler, networkEngine));
            ft.commit();
            bottomNavigationView.setSelectedItemId(R.id.search_page);
        }



        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P)
        {
//            ActivityManager am = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
//            Boolean isRestricted = !pm.isIgnoringBatteryOptimizations(packageName)
            Intent batt_intent = new Intent();
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            if((!pm.isIgnoringBatteryOptimizations(packageName)) & (!battery_requested)){
                // Show some message to the user that app won't work in background and needs to change settings
                //... (implement dialog)
                // When user clicks 'Ok' or 'Go to settings', then:
//                openBatterySettings();
//                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
//                i.addCategory(Intent.CATEGORY_DEFAULT);
//                i.setData(Uri.parse("package:" + getPackageName()));
//                startActivity(i);
                AlertDialog alertDialog =  batteryAllowDialog();
                alertDialog.show();
            }
        }

    }

    // Defines dialogs
    public AlertDialog batteryAllowDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_battery_permission, null);

        Button buttonDecline = dialogLayout.findViewById(R.id.buttonDecline);
        Button buttonAllow = dialogLayout.findViewById(R.id.buttonAllow);

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();
        buttonDecline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                battery_requested = true;
                SharedPreferences.Editor editor = mSharedPreference.edit();
                editor.putBoolean(SP_BATTERY_REQUEST,battery_requested );
                editor.commit();
                alertDialog.dismiss();
            }
        });
        buttonAllow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.fromParts("package", getPackageName(), null));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                alertDialog.dismiss();
            }
        });
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    public AlertDialog aboutAlertDialogP1(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_about_p1, null);

        Button buttonNext = dialogLayout.findViewById(R.id.buttonNext);
        Button buttonBattery = dialogLayout.findViewById(R.id.buttonBattery);
        TextView textBatteryPsa = dialogLayout.findViewById(R.id.textBatteryPsa);


        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        if(!pm.isIgnoringBatteryOptimizations(packageName)){
            textBatteryPsa.setText("Unrestricted Battery Usage allowance for the app is required for the widget to work during battery saver mode. Please allow the app's battery unrestricted usage below.");
            textBatteryPsa.setTextColor(getResources().getColor(R.color.abs_red));
            buttonBattery.setVisibility(View.VISIBLE);
        }else{
            textBatteryPsa.setText("Unrestricted Battery Usage allowance for the app is required for the widget to work during battery saver mode. Current setting already allowed this battery usage.");
            textBatteryPsa.setTextColor(getResources().getColor(R.color.app_on_neutral_bg));
            buttonBattery.setVisibility(View.GONE);
        }

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();
        buttonBattery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = batteryAllowDialog();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP2();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });


        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
    }


    public AlertDialog aboutAlertDialogP2(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_about_p2, null);

        Button buttonNext = dialogLayout.findViewById(R.id.buttonNext);
        Button buttonPrevious = dialogLayout.findViewById(R.id.buttonPrevious);

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP3();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP1();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
    }

    public AlertDialog aboutAlertDialogP3(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_about_p3, null);

        Button buttonNext = dialogLayout.findViewById(R.id.buttonNext);
        Button buttonPrevious = dialogLayout.findViewById(R.id.buttonPrevious);

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP4();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP2();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
    }

    public AlertDialog aboutAlertDialogP4(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_about_p4, null);

        Button buttonNext = dialogLayout.findViewById(R.id.buttonNext);
        Button buttonPrevious = dialogLayout.findViewById(R.id.buttonPrevious);

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP5();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP3();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
    }

    public AlertDialog aboutAlertDialogP5(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_about_p5, null);

        Button buttonNext = dialogLayout.findViewById(R.id.buttonNext);
        Button buttonPrevious = dialogLayout.findViewById(R.id.buttonPrevious);

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();

        buttonNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP6();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP4();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
    }

    public AlertDialog aboutAlertDialogP6(){
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.dialog_about_p6, null);

        Button buttonClose = dialogLayout.findViewById(R.id.buttonClose);
        Button buttonPrevious = dialogLayout.findViewById(R.id.buttonPrevious);

        TextView mWebsite = (TextView) dialogLayout.findViewById(R.id.textWebsite);
        TextView mEmail = (TextView) dialogLayout.findViewById(R.id.textEmail);

        builder.setView(dialogLayout);
        AlertDialog alertDialog = builder.create();

        buttonClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        buttonPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog nextDialog = aboutAlertDialogP5();
                alertDialog.dismiss();
                nextDialog.show();
            }
        });

        mWebsite.setMovementMethod(LinkMovementMethod.getInstance());
        mEmail.setMovementMethod(LinkMovementMethod.getInstance());

        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        alertDialog.setCanceledOnTouchOutside(true);

        return alertDialog;
    }



}