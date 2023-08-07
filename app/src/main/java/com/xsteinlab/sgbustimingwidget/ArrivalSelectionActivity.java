package com.xsteinlab.sgbustimingwidget;

import android.os.Bundle;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.xsteinlab.sgbustimingwidget.adapter.ArrivalSelectionAdapter;
import com.xsteinlab.sgbustimingwidget.adapter.BusInfoItem;
import com.xsteinlab.sgbustimingwidget.database.DBHandler;
import com.xsteinlab.sgbustimingwidget.network.NetworkEngine;
import com.google.android.material.color.DynamicColors;
import com.xsteinlab.sgbustimingwidget.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Map;

public class ArrivalSelectionActivity extends AppCompatActivity {

    private DBHandler dbHandler;
    private NetworkEngine networkEngine;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Utils.localLogging("WHO CALLS THE DRAGON KNIGHT");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arrival_selection);
        DynamicColors.applyToActivitiesIfAvailable(this.getApplication());

        dbHandler = new DBHandler(ArrivalSelectionActivity.this);
        networkEngine = new NetworkEngine(this, dbHandler);

        if (dbHandler.CountDbRows(DBHandler.BUS_STOP_TABLE) == 0){
            Utils.localLogging("Calling get busmetadata");
            networkEngine.GetBusStopMetadata(this, dbHandler);
        }

        ListView listViewSavedArrival = (ListView) findViewById(R.id.listSavedArrival);
        ArrayList<BusInfoItem>  savedArrivalItems = new ArrayList<BusInfoItem>();
        Map<String, String>[] arrivalTable = this.dbHandler.GetTable("savedbusarrival");
        if(arrivalTable.length == 0){
            try {
                BusInfoItem busInfoItem = new BusInfoItem("NODATA", null, null, null);
                savedArrivalItems.add(busInfoItem);

            } catch (JSONException e) {
                throw new RuntimeException(e);
            }

        }

        for(int i = 0; i < arrivalTable.length; i++){
            try {
                Utils.localLogging("Existing stops:" + arrivalTable[i].get("code") + "---" + arrivalTable[i].get("busno"));
                Map<String, String> busStopMetadata = dbHandler.FindBusStop(arrivalTable[i].get("code"))[0];
                BusInfoItem busInfoItem = new BusInfoItem(arrivalTable[i].get("busno"), null, arrivalTable[i].get("code"), busStopMetadata);
                busInfoItem.setWidgetId(arrivalTable[i].get(DBHandler.WIDGET_ID_COL));
                savedArrivalItems.add(busInfoItem);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        ArrivalSelectionAdapter arrivalSelectionAdapter = new ArrivalSelectionAdapter(this, R.layout.item_arrival_selection, savedArrivalItems, getIntent());
        listViewSavedArrival.setAdapter(arrivalSelectionAdapter);

    }
}
