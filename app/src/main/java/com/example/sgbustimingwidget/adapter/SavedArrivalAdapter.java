package com.example.sgbustimingwidget.adapter;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.sgbustimingwidget.MainActivity;
import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.fragment.SearchFragment;
import com.example.sgbustimingwidget.network.NetworkEngine;

import java.util.List;

import J.N;


public class SavedArrivalAdapter extends ArrayAdapter<BusInfoItem> {
    private int resourceLayout;
    private Context context;
    private DBHandler dbHandler;

    public SavedArrivalAdapter(Context context, int resource, List<BusInfoItem> busInfoItems, DBHandler dbHandler) {
        super(context, resource, busInfoItems);
        this.resourceLayout = resource;
        this.context = context;
        this.dbHandler =dbHandler;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BusInfoItem busInfoItem = getItem(position);
        View currentItemView = convertView;

        String busNo = busInfoItem.getBusNo();
        String busStopCode = busInfoItem.getBusStopCode();
        String busStopName = busInfoItem.getBusStopName();
        String[] arrivalTimeArr = busInfoItem.getArrivalTimeArr();

        Boolean saveStatus = this.dbHandler.checkSavedBusArrival(busStopCode, busNo);


        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_saved_arrival, parent, false);
        }

        if (busInfoItem != null) {
            TextView textViewBusNo =  currentItemView.findViewById(R.id.textViewBusNo);
            TextView textViewArrivalTime = currentItemView.findViewById(R.id.textViewArrivalTime);
            ToggleButton saveButton = currentItemView.findViewById(R.id.toggleButtonSave);
            TextView textViewBusStopName = currentItemView.findViewById(R.id.textViewBusStopName);
            TextView textViewBusStopCode =  currentItemView.findViewById(R.id.textViewBusStopCode);
            LinearLayout layoutBusStopInfo = currentItemView.findViewById(R.id.layoutBusStopInfo);


            if (textViewBusNo != null) {
                textViewBusNo.setText(busNo);
            }
            if (textViewBusStopName != null) {
                textViewBusStopName.setText(busStopName);
            }
            if (textViewBusStopCode != null) {
                textViewBusStopCode.setText(busStopCode);
            }
            if (textViewArrivalTime != null) {
                String s = busInfoItem.getArrivalTimeStr();
                textViewArrivalTime.setText(s);
            }

            layoutBusStopInfo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) view.getContext()).displaySearchFragmentResult(busStopName, "");
                }
            });

            textViewBusNo.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((MainActivity) view.getContext()).displaySearchFragmentResult(busStopName, "");
                }
            });

            saveButton.setChecked(saveStatus);
            saveButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked){
                        //toggle is enabled
                        System.out.println("CHECKED");
                        dbHandler.addNewSavedBusArrival(busStopCode, busNo);
                    }else{
                        //toggle is disabled
                        System.out.println("UNCHECKED");
                        dbHandler.removeSavedBusArrival(busStopCode, busNo);
                    }
                }
            });
        }
        return currentItemView;
    }
}
