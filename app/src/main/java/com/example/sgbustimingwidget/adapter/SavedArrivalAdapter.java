package com.example.sgbustimingwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.database.DBHandler;

import java.util.List;


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
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.saved_arrival_item, parent, false);
        }


        if (busInfoItem != null) {
            TextView textViewBusNo =  currentItemView.findViewById(R.id.textViewBusNo);
            TextView textViewArrivalTime = currentItemView.findViewById(R.id.textViewArrivalTime);
            ToggleButton saveButton = currentItemView.findViewById(R.id.toggleButtonSave);
            TextView textViewBusStopName = currentItemView.findViewById(R.id.textViewBusStopName);
            TextView textViewBusStopCode =  currentItemView.findViewById(R.id.textViewBusStopCode);


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
                String s = String.format("%1$s, %2$s, %3$s min(s)", arrivalTimeArr[0], arrivalTimeArr[1], arrivalTimeArr[2]);
                textViewArrivalTime.setText(s);
            }

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
