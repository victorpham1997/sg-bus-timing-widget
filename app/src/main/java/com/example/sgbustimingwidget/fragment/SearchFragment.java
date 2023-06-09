package com.example.sgbustimingwidget.fragment;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoAdapter;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;

public class SearchFragment extends Fragment {

    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    private ImageButton buttonSearch;
    private ImageButton buttonReloadDb;
    private ImageButton buttonOpenStopOnMap;
    private EditText editBusNo;
    private EditText editBusStopCode;
    private TextView textBusStopCode;
    private TextView textBusStopName;
    private TextView textStreetName;
    private ListView listBusInfo;
    private LinearLayout layoutBusStopInfo;

    private Handler handler;
    private BusInfoAdapter busInfoAdapter;
    private ArrayList<BusInfoItem> busInfoItems;

    private Map<String,String> searchedBusStop;

    public SearchFragment(DBHandler dbHandler, NetworkEngine networkEngine) {
        // Required empty public constructor
        this.dbHandler = dbHandler;
        this.networkEngine = networkEngine;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        listBusInfo = (ListView) view.findViewById(R.id.listBusInfo);
//        apiCallback.setListView(listBusInfo);

        buttonSearch = (ImageButton) view.findViewById(R.id.buttonSearch);
        buttonReloadDb = (ImageButton) view.findViewById(R.id.buttonReloadDb);
        buttonOpenStopOnMap = (ImageButton) view.findViewById(R.id.buttonOpenStopOnMap);
        editBusNo = (EditText) view.findViewById(R.id.editBusNoSearch);
        editBusStopCode = (EditText) view.findViewById(R.id.editBusStopSearch);
        textBusStopCode = (TextView) view.findViewById(R.id.textBusStopCode);
        textBusStopName = (TextView) view.findViewById(R.id.textBusStopName);
        textStreetName = (TextView) view.findViewById(R.id.textStreetName);
        layoutBusStopInfo = (LinearLayout) view.findViewById(R.id.layoutBusStopInfo);


        buttonSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dbHandler.FindBusStop(editBusStopCode.getText().toString()).length < 1){
                    Toast.makeText(getActivity(),"Unable to find any result for " + editBusStopCode.getText().toString(),Toast.LENGTH_LONG).show();
                    return;
                }
                layoutBusStopInfo.setVisibility(View.VISIBLE);

                busInfoItems = new ArrayList<BusInfoItem>();
                busInfoAdapter = new BusInfoAdapter(getContext() , R.layout.item_bus_info, busInfoItems, dbHandler);

//                apiCallback.setBusInfoItems(busInfoItems);
//                apiCallback.setAdapter(busInfoAdapter);
                listBusInfo.setAdapter(busInfoAdapter);

                searchedBusStop =  dbHandler.FindBusStop(editBusStopCode.getText().toString())[0];

                String busStopCode = searchedBusStop.get("code");
                networkEngine.GetBusArrival(busStopCode, editBusNo.getText().toString(), busInfoItems, busInfoAdapter);
                Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0];
                textBusStopCode.setText(busStopCode);
                textBusStopName.setText(busStopMetadata.get("description"));
                textStreetName.setText(busStopMetadata.get("roadname"));

            }
        });

        buttonReloadDb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                networkEngine.GetBusStopMetadata(getActivity(), dbHandler);
                System.out.println("lalala: " + busInfoItems);

            }
        });

        buttonOpenStopOnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String longitude = searchedBusStop.get("long");
                String latitude = searchedBusStop.get("lat");
                String busStopLabel = String.format("%s (%s)", searchedBusStop.get("description"), searchedBusStop.get("code"));

                String formatStr = "geo:0,0?q=%s,%s(%s)";
                String uri = String.format(Locale.ENGLISH, formatStr, latitude, longitude, busStopLabel);

                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(uri));
                startActivity(intent);
            }
        });
        editBusStopCode.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    buttonSearch.performClick();
                    return true;
                }
                return false;
            }
        });
        editBusNo.setOnEditorActionListener(new TextView.OnEditorActionListener(){
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    buttonSearch.performClick();
                    return true;
                }
                return false;
            }
        });
    return view;
    }
}