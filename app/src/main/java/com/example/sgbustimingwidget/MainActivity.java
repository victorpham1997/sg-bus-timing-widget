package com.example.sgbustimingwidget;

import android.os.Bundle;

import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.fragment.SavedFragment;
import com.example.sgbustimingwidget.fragment.SearchFragment;
import com.example.sgbustimingwidget.network.NetworkEngine;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import java.util.Map;


//TODO fix json error
//Make function to search us bus stop code to bus stop name

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Define bus stop metatdata DB
        dbHandler = new DBHandler(MainActivity.this);

        // Define layout
        setContentView(R.layout.activity_main);

        // Define component
//        apiCallback = new ApiCallback_legacy(this, dbHandler);
        networkEngine = new NetworkEngine(this, dbHandler);



        if (dbHandler.CountDbRows() == 0){
            System.out.println("Calling get busmetadata");
            networkEngine.GetBusStopMetadata(this, dbHandler);
        }

        Map<String, String>[] tisMap =  dbHandler.Query("SELECT * FROM busstopmetadata WHERE code = 83139");
        System.out.println("aaa: " + tisMap);

        // Set fragment
        if (savedInstanceState == null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.flFragment, new SearchFragment(dbHandler, networkEngine));
            ft.commit();
        }

        // Set up navigation buttons

        bottomNavigationView =  findViewById(R.id.bottomNavigationView);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            Fragment selectedFragment = null;
            int itemId = item.getItemId();
            if (itemId == R.id.search_page) {
                selectedFragment = new SearchFragment(dbHandler, networkEngine);
            }
            else if (itemId == R.id.saved_page) {
                selectedFragment = new SavedFragment(dbHandler, networkEngine);
            }
//            else if (itemId == R.id.profile_page) {
//                selectedFragment = new ProfileFragment();
//            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction().replace(R.id.flFragment, selectedFragment).commit();
            }
            return true;
        });


    }

//        buttonSearch.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                if (dbHandler.FindBusStop(editBusStopCode.getText().toString()).length < 1){
//                    Toast.makeText(MainActivity.this,"Unable to find any result for " + editBusStopCode.getText().toString(),Toast.LENGTH_SHORT).show();
//                    return;
//                }
//                String busStopCode = dbHandler.FindBusStop(editBusStopCode.getText().toString())[0].get("code");
//                networkEngine.GetBusArrival(busStopCode, editBusNo.getText().toString());
////                String resp = ApiCalls.busArrivalCall("83139", "15");
////                System.out.println(resp);
//            }
//        });
//
//        buttonReloadDb.setOnClickListener(new View.OnClickListener() {
//            public void onClick(View v) {
//                networkEngine.GetBusStopMetadata(MainActivity.this, dbHandler);
//            }
//        });

//    @Override
//    @SuppressLint("ResourceType")
//    public void displayBusInfo(JSONArray busInfoJArr) throws JSONException {
//        String s[] = new String[busInfoJArr.length()];
//        for(int i = 0; i < busInfoJArr.length(); i++){
//            s[i] = busInfoJArr.getJSONObject(i).getString("ServiceNo");
//        }
//        ArrayAdapter<String> arr;
//        arr = new ArrayAdapter<String>(
//            this,
//            R.id.listBusInfo,
//            s);
//        listBusInfo.setAdapter(arr);
//    }


    //    button.setText("Press Me");
//    LinearLayout layout = (LinearLayout) findViewById(R.id.layout1);
//    layout.addView(myButton);


}