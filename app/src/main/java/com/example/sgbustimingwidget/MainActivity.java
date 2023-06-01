package com.example.sgbustimingwidget;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.fragment.ProfileFragment;
import com.example.sgbustimingwidget.fragment.SavedFragment;
import com.example.sgbustimingwidget.fragment.SearchFragment;
import com.example.sgbustimingwidget.network.NetworkEngine;
import com.example.sgbustimingwidget.widget.BusTimingWidget;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.color.DynamicColors;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Map;


//TODO fix json error
//Make function to search us bus stop code to bus stop name

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private MaterialToolbar toolbar;


    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        System.out.println("Oh call me maybe");


        // Define bus stop metatdata DB
        dbHandler = new DBHandler(MainActivity.this);


        // Define layout
        setContentView(R.layout.activity_main);

        // Define component
        networkEngine = new NetworkEngine(this, dbHandler);



        if (dbHandler.CountDbRows(DBHandler.BUS_STOP_TABLE) == 0){
            System.out.println("Calling get busmetadata");
            networkEngine.GetBusStopMetadata(this, dbHandler);
        }

        toolbar = findViewById(R.id.topAppBar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.about:
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        LayoutInflater inflater = (MainActivity.this).getLayoutInflater();
                        View dialogLayout = inflater.inflate(R.layout.dialog_about, null);

                        Button buttonClose = dialogLayout.findViewById(R.id.buttonClose);
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
                        mWebsite.setMovementMethod(LinkMovementMethod.getInstance());
                        mEmail.setMovementMethod(LinkMovementMethod.getInstance());
                        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                        alertDialog.setCanceledOnTouchOutside(false);
                        alertDialog.show();
                        break;
                }
                return true;
            }
        });


        bottomNavigationView =  findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setItemIconTintList(null);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.search_page) {
                selectedFragment = new SearchFragment(dbHandler, networkEngine);
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

        // Set up navigation buttons

    }

}