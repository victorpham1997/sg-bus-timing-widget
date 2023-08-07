package com.xsteinlab.sgbustimingwidget.adapter;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.xsteinlab.sgbustimingwidget.R;
import com.xsteinlab.sgbustimingwidget.database.DBHandler;
import com.xsteinlab.sgbustimingwidget.widget.BusTimingWidgetSingle;

import java.util.List;


public class ArrivalSelectionAdapter extends ArrayAdapter<BusInfoItem> {

    private Context context;
    private Intent intent;

    public ArrivalSelectionAdapter(Context context, int resource, List<BusInfoItem> busInfoItems, Intent intent) {
        super(context, resource, busInfoItems);
        this.context = context;
        this.intent = intent;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        BusInfoItem busInfoItem = getItem(position);
        View currentItemView = convertView;

        if (currentItemView == null) {
            currentItemView = LayoutInflater.from(getContext()).inflate(R.layout.item_arrival_selection, parent, false);
        }

        if(busInfoItem.getBusNo().equals("NODATA")){
            LinearLayout layoutBusStop = currentItemView.findViewById(R.id.layoutBusStop);
            TextView textViewBusNo =  currentItemView.findViewById(R.id.textViewBusNo);
            Button buttonSelect = currentItemView.findViewById(R.id.buttonSelect);
            layoutBusStop.setVisibility(View.GONE);
            buttonSelect.setVisibility(View.GONE);
            textViewBusNo.setText(context.getString(R.string.no_data_prompt));
            textViewBusNo.setTextSize(20);
            textViewBusNo.setGravity(Gravity.CENTER);
            return currentItemView;
        }


        String busNo = busInfoItem.getBusNo();
        String busStopCode = busInfoItem.getBusStopCode();
        String busStopName = busInfoItem.getBusStopName();
        String widgetId = busInfoItem.getWidgetId();


        if (busInfoItem != null) {
            TextView textViewBusNo =  currentItemView.findViewById(R.id.textViewBusNo);
            Button buttonSelect = currentItemView.findViewById(R.id.buttonSelect);
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

            if (widgetId != null && !widgetId.equals("")){
                buttonSelect.setEnabled(false);
            }

            buttonSelect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle extras = intent.getExtras();
                    int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
                    if (extras != null) {
                        appWidgetId = extras.getInt(
                                AppWidgetManager.EXTRA_APPWIDGET_ID,
                                AppWidgetManager.INVALID_APPWIDGET_ID);
                    }
                    Intent resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    ((Activity)context).setResult(Activity.RESULT_CANCELED, resultValue);


                    DBHandler dbHandler = new DBHandler(context);
                    dbHandler.WriteDataToTable(DBHandler.SAVED_BUS_ARV_TABLE, "widgetid=''", "widgetid='"+String.valueOf(appWidgetId)+"'");
                    dbHandler.WriteDataToTable(DBHandler.SAVED_BUS_ARV_TABLE, "widgetid="+String.valueOf(appWidgetId), String.format("code='%s' AND busno='%s'", busStopCode,busNo));

                    BusTimingWidgetSingle.updateWidgetSingle(context, dbHandler, busStopCode, busNo, appWidgetId);

                    resultValue = new Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                    ((Activity)context).setResult(Activity.RESULT_OK, resultValue);
                    ((Activity) context).finish();
                }
            });


        }
        return currentItemView;
    }
}
