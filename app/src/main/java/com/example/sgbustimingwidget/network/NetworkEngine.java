package com.example.sgbustimingwidget.network;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.widget.BusTimingWidget;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.utils.Utils;
import com.google.android.material.snackbar.Snackbar;

import org.chromium.net.CronetEngine;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkEngine {

    private CronetEngine cronetEngine;
    private Context context;
    private DBHandler dbHandler;
    private Handler handler;
    private ExecutorService cronetCallbackExecutorService;
    private TextView textReload;
    private Dialog reloadDialog;

    public NetworkEngine(Context context, DBHandler dbHandler) {
        this.context = context;
        this.dbHandler = dbHandler;
        this.handler  = new Handler(context.getMainLooper());
        cronetEngine = CreateDefaultCronetEngine(context);
        cronetCallbackExecutorService = Executors.newFixedThreadPool(1);
    }

    private static CronetEngine CreateDefaultCronetEngine(Context context){
        return new CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .build();
    }

    public void GetBusArrival(String busStopCode, String serviceNo, ArrayList<BusInfoItem> busInfoItems, ArrayAdapter infoAdapter ){
        String url = String.format("http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=%1$s&ServiceNo=%2$s", busStopCode.trim(), serviceNo.trim());
        CronetEngine.Builder engineBuilder = new CronetEngine.Builder(context);
        CronetEngine engine = engineBuilder.build();
        ApiCallback apiCallback = new ApiCallback() {
            @Override
            public void postSucceeded(UrlRequest request, UrlResponseInfo info, JSONObject respJo) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if(serviceNo != ""){
                                ProcessBusArrivalEndpoint(respJo, busInfoItems, serviceNo);
                            }else{
                                ProcessBusArrivalEndpoint(respJo, busInfoItems);
                            }
                            if(infoAdapter != null){
                                infoAdapter.notifyDataSetChanged();
                            }
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }});
            }
        };
        UrlRequest.Builder requestBuilder = engine. newUrlRequestBuilder(
                url, apiCallback, cronetCallbackExecutorService);
        requestBuilder.setHttpMethod("GET");
        requestBuilder.addHeader("accept", "application/json");
        requestBuilder.addHeader("AccountKey", "vBdsrPSuR5692Y//pACMfQ==");
        UrlRequest request = requestBuilder.build();
        request.start();
    }

    public void GetBusArrival(String busStopCode, String serviceNo, ApiCallback apiCallback ){
        String url = String.format("http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=%1$s&ServiceNo=%2$s", busStopCode.trim(), serviceNo.trim());
        CronetEngine.Builder engineBuilder = new CronetEngine.Builder(context);
        CronetEngine engine = engineBuilder.build();
        Executor executor = Executors.newSingleThreadExecutor();
        UrlRequest.Builder requestBuilder = engine. newUrlRequestBuilder(
                url, apiCallback, executor);
        requestBuilder.setHttpMethod("GET");
        requestBuilder.addHeader("accept", "application/json");
        requestBuilder.addHeader("AccountKey", "vBdsrPSuR5692Y//pACMfQ==");
        UrlRequest request = requestBuilder.build();
        System.out.println("Starting request");
        request.start();
    }

    public void GetBusStopMetadata(Context context, DBHandler dbHandler){
        dbHandler.DeleteTable();
        reloadDialog = new Dialog(context);
        reloadDialog.setContentView(R.layout.dialog_metadata_reload);
        reloadDialog.setCanceledOnTouchOutside(false);
        reloadDialog.setCancelable(false);
        reloadDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        textReload = (TextView) reloadDialog.findViewById(R.id.textReload);
        reloadDialog.show();

        GetBusStopMetadata(context, 0);
    }

    public void GetBusStopMetadata(Context context, int skips){
        Activity activity = (Activity) context;
        String url = String.format("http://datamall2.mytransport.sg/ltaodataservice/BusStops?$skip=%1$s", skips);
        Executor executor = Executors.newSingleThreadExecutor();
        ApiCallback apiCallback = new ApiCallback() {
            @Override
            public void postSucceeded(UrlRequest request, UrlResponseInfo info, JSONObject respJo) throws JSONException, ParseException {
                JSONArray busStopsJa = respJo.getJSONArray("value");
                if (busStopsJa.length() > 0){
                    dbHandler.PushBusStopMetadataToDB(busStopsJa);
                    int currentDbRows = dbHandler.CountDbRows(DBHandler.BUS_STOP_TABLE);
                    System.out.println("LENGTH: " + currentDbRows);
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            textReload.setText("Updated " + currentDbRows + " records");
                        }
                    });
                    GetBusStopMetadata(context, currentDbRows);
                }else{
//                    Toast.makeText(context,"Bus Stops Metadata is updated",Toast.LENGTH_SHORT).show();
                    int currentDbRows = dbHandler.CountDbRows(DBHandler.BUS_STOP_TABLE);

                    reloadDialog.dismiss();
                    Snackbar snackbar = Snackbar.make(((Activity) context).getWindow().getDecorView(), "Successfully updated "+ currentDbRows + " records",Snackbar.LENGTH_LONG);
                    snackbar.show();
                }
                }
        };

        UrlRequest.Builder requestBuilder = cronetEngine.newUrlRequestBuilder(
                url, apiCallback, executor);
        requestBuilder.setHttpMethod("GET");
        requestBuilder.addHeader("accept", "application/json");
        requestBuilder.addHeader("AccountKey", "vBdsrPSuR5692Y//pACMfQ==");
        UrlRequest request = requestBuilder.build();
        request.start();
    }


    public void ProcessBusArrivalEndpoint(JSONObject respJo, ArrayList<BusInfoItem> busInfoItems) throws JSONException, ParseException {
        JSONArray busInfoJArr = (JSONArray) respJo.get("Services");
        System.out.println("JSON 'Service' resp: " + busInfoJArr.toString());
        String busStopCode = respJo.getString("BusStopCode");
        Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0];
        for(int i = 0; i < busInfoJArr.length(); i++){
            String busNo = busInfoJArr.getJSONObject(i).getString("ServiceNo");
            String[] arrivalTimeArr = Utils.ExtractArrivalTime(busInfoJArr.getJSONObject(i));
            busInfoItems.add( new BusInfoItem(busNo, arrivalTimeArr, busStopCode, busStopMetadata));
        }
    }

    public void ProcessBusArrivalEndpoint(JSONObject respJo, ArrayList<BusInfoItem> busInfoItems, String bus_no) throws JSONException, ParseException {
        JSONArray busInfoJArr = (JSONArray) respJo.get("Services");
        System.out.println("JSON 'Service' resp: " + busInfoJArr.toString());
        String busStopCode = respJo.getString("BusStopCode");
        Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0];
        if(busInfoJArr.length() == 0){
            busInfoItems.add( new BusInfoItem(bus_no, null, busStopCode, busStopMetadata));
            return;
        }
        for(int i = 0; i < busInfoJArr.length(); i++){
            String busNo = busInfoJArr.getJSONObject(i).getString("ServiceNo");
            String[] arrivalTimeArr = Utils.ExtractArrivalTime(busInfoJArr.getJSONObject(i));
            busInfoItems.add( new BusInfoItem(busNo, arrivalTimeArr, busStopCode, busStopMetadata));
        }
    }
}
