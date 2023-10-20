package com.xsteinlab.sgbustimingwidget.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.xsteinlab.sgbustimingwidget.R;
import com.xsteinlab.sgbustimingwidget.adapter.BusInfoItem;
import com.xsteinlab.sgbustimingwidget.adapter.SavedArrivalAdapter;
import com.xsteinlab.sgbustimingwidget.database.DBHandler;
import com.xsteinlab.sgbustimingwidget.network.NetworkEngine;
import com.xsteinlab.sgbustimingwidget.utils.Utils;

import java.util.ArrayList;
import java.util.Map;


public class SavedFragment extends Fragment {

    private DBHandler dbHandler;
    private NetworkEngine networkEngine;
    private ListView listViewSavedArrival;
    private SwipeRefreshLayout swipeRefreshLayout;

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

    public void refreshListView(){
        savedArrivalItems = new ArrayList<BusInfoItem>();
        savedArrivalAdapter = new SavedArrivalAdapter(getContext() , R.layout.item_saved_arrival, savedArrivalItems, dbHandler);
        listViewSavedArrival.setAdapter(savedArrivalAdapter);
        Map<String, String>[] arrivalTable = this.dbHandler.GetTable("savedbusarrival");
//        Utils.localLogging(arrivalTable);
        for(int i = 0; i < arrivalTable.length; i++){
            Utils.localLogging("Existing stops:" + arrivalTable[i].get("code") + "---" + arrivalTable[i].get("busno"));
            networkEngine.GetBusArrival(arrivalTable[i].get("code"), arrivalTable[i].get("busno"), savedArrivalItems, savedArrivalAdapter );
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_saved, container, false);

        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                refreshListView();
            }
        });

        listViewSavedArrival = (ListView) view.findViewById(R.id.listSavedArrival);

        refreshListView();
//        savedArrivalAdapter = new SavedArrivalAdapter(getContext() , R.layout.item_saved_arrival, savedArrivalItems, dbHandler);
//
////        apiCallback.setBusInfoItems(savedArrivalItems);
////        apiCallback.setAdapter(savedArrivalAdapter);
//        listViewSavedArrival.setAdapter(savedArrivalAdapter);
//
//        Map<String, String>[] arrivalTable = this.dbHandler.GetTable("savedbusarrival");
////        Utils.localLogging(arrivalTable);
//        for(int i = 0; i < arrivalTable.length; i++){
//            Utils.localLogging("Existing stops:" + arrivalTable[i].get("code") + "---" + arrivalTable[i].get("busno"));
//            networkEngine.GetBusArrival(arrivalTable[i].get("code"), arrivalTable[i].get("busno"), savedArrivalItems, savedArrivalAdapter );
//        }
        return view;
    }
}