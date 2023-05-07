package com.example.sgbustimingwidget.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sgbustimingwidget.MainActivity;
import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoAdapter;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class SearchFragment extends Fragment {

    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    private Button buttonSearch;
    private Button buttonReloadDb;
    private EditText editBusNo;
    private EditText editBusStopCode;
    private TextView textBusStopCode;
    private TextView textBusStopName;
    private TextView textStreetName;
    private ListView listBusInfo;

    private Handler handler;
    private BusInfoAdapter busInfoAdapter;
    private ArrayList<BusInfoItem> busInfoItems;

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

        buttonSearch = (Button) view.findViewById(R.id.buttonSearch);
        buttonReloadDb = (Button) view.findViewById(R.id.buttonReloadDb);
        editBusNo = (EditText) view.findViewById(R.id.editBusNoSearch);
        editBusStopCode = (EditText) view.findViewById(R.id.editBusStopSearch);
        textBusStopCode = (TextView) view.findViewById(R.id.textBusStopCode);
        textBusStopName = (TextView) view.findViewById(R.id.textBusStopName);
        textStreetName = (TextView) view.findViewById(R.id.textStreetName);


        buttonSearch.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (dbHandler.FindBusStop(editBusStopCode.getText().toString()).length < 1){
                    Toast.makeText(getActivity(),"Unable to find any result for " + editBusStopCode.getText().toString(),Toast.LENGTH_SHORT).show();
                    return;
                }

                busInfoItems = new ArrayList<BusInfoItem>();
                busInfoAdapter = new BusInfoAdapter(getContext() , R.layout.bus_info_item, busInfoItems, dbHandler);

//                apiCallback.setBusInfoItems(busInfoItems);
//                apiCallback.setAdapter(busInfoAdapter);
                listBusInfo.setAdapter(busInfoAdapter);

                String busStopCode = dbHandler.FindBusStop(editBusStopCode.getText().toString())[0].get("code");
                networkEngine.GetBusArrival(busStopCode, editBusNo.getText().toString(), busInfoItems, busInfoAdapter);
                Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0];
                textBusStopCode.setText(busStopCode);
                textBusStopName.setText(busStopMetadata.get("description"));
                textStreetName.setText(busStopMetadata.get("roadname"));

//                String resp = ApiCalls.busArrivalCall("83139", "15");
//                System.out.println(resp);
            }
        });

        buttonReloadDb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                networkEngine.GetBusStopMetadata(getActivity(), dbHandler);
                System.out.println("lalala: " + busInfoItems);

            }
        });

        return view;
    }
}