package com.xsteinlab.sgbustimingwidget.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.xsteinlab.sgbustimingwidget.R;
import com.xsteinlab.sgbustimingwidget.database.DBHandler;
import com.xsteinlab.sgbustimingwidget.utils.Utils;

import java.util.List;


public class BusInfoAdapter extends ArrayAdapter<BusInfoItem> {

    private int resourceLayout;
    private Context context;
    private DBHandler dbHandler;

    public BusInfoAdapter(Context context, int resource, List<BusInfoItem> busInfoItems, DBHandler dbHandler) {
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
        String[] arrivalTimeArr = busInfoItem.getArrivalTimeArr();

        Boolean saveStatus = this.dbHandler.checkSavedBusArrival(busStopCode, busNo);

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_bus_info, parent, false);
        }


        if (busInfoItem != null) {
            TextView textViewBusNo =  currentItemView.findViewById(R.id.textViewBusNo);
            TextView textViewArrivalTime = currentItemView.findViewById(R.id.textViewArrivalTime);
            ToggleButton saveButton = currentItemView.findViewById(R.id.toggleButtonSave);


            if (textViewBusNo != null) {
                textViewBusNo.setText(busNo);
            }
            if (textViewArrivalTime != null) {
                String s = busInfoItem.getArrivalTimeStr();
//                String s = String.format("%1$s, %2$s, %3$s min(s)", arrivalTimeArr[0], arrivalTimeArr[1], arrivalTimeArr[2]);
                textViewArrivalTime.setText(s);
            }

            saveButton.setChecked(saveStatus);
            saveButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked){
                        //toggle is enabled
                        Utils.localLogging("CHECKED");
                        dbHandler.addNewSavedBusArrival(busStopCode, busNo);
                    }else{
                        //toggle is disabled
                        Utils.localLogging("UNCHECKED");
                        dbHandler.removeSavedBusArrival(busStopCode, busNo);
                    }
                }
            });
        }
        return currentItemView;
    }
}
