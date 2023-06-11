package com.example.sgbustimingwidget.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.adapter.SavedArrivalAdapter;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;

import java.util.ArrayList;
import java.util.Map;


public class SavedFragment extends Fragment {

    private DBHandler dbHandler;
    private NetworkEngine networkEngine;
    private ListView listViewSavedArrival;

    private ArrayList<BusInfoItem> savedArrivalItems;
    private SavedArrivalAdapter savedArrivalAdapter;

    public SavedFragment() {

    }
    public SavedFragment(DBHandler dbHandler,  NetworkEngine networkEngine) {
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
        View view = inflater.inflate(R.layout.fragment_saved, container, false);
        listViewSavedArrival = (ListView) view.findViewById(R.id.listSavedArrival);

        savedArrivalItems = new ArrayList<BusInfoItem>();
        savedArrivalAdapter = new SavedArrivalAdapter(getContext() , R.layout.item_saved_arrival, savedArrivalItems, dbHandler);

//        apiCallback.setBusInfoItems(savedArrivalItems);
//        apiCallback.setAdapter(savedArrivalAdapter);
        listViewSavedArrival.setAdapter(savedArrivalAdapter);

        Map<String, String>[] arrivalTable = this.dbHandler.GetTable("savedbusarrival");
//        System.out.println(arrivalTable);
        for(int i = 0; i < arrivalTable.length; i++){
            System.out.println("Existing stops:" + arrivalTable[i].get("code") + "---" + arrivalTable[i].get("busno"));
            networkEngine.GetBusArrival(arrivalTable[i].get("code"), arrivalTable[i].get("busno"), savedArrivalItems, savedArrivalAdapter );
        }
        return view;
    }
}