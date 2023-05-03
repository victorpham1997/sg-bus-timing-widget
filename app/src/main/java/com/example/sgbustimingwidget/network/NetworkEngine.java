package com.example.sgbustimingwidget.network;

import android.content.Context;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.utils.Utils;

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
    private CronetEngine.Builder myBuilder;
//    private Executor executor;
//    private ApiCallback_legacy apiCallback;
    private Context context;
    private DBHandler dbHandler;
    private Handler handler;
    private ExecutorService cronetCallbackExecutorService;

    public NetworkEngine(Context context, DBHandler dbHandler) {
        this.context = context;
        this.dbHandler = dbHandler;
        this.handler  = new Handler(context.getMainLooper());
//        myBuilder = new CronetEngine.Builder(context);
        cronetEngine = CreateDefaultCronetEngine(context);
        cronetCallbackExecutorService = Executors.newFixedThreadPool(1);

//        executor = Executors.newSingleThreadExecutor();
//        this.apiCallback = apiCallback;
    }

    private static CronetEngine CreateDefaultCronetEngine(Context context){
        return new CronetEngine.Builder(context)
                .enableHttp2(true)
                .enableQuic(true)
                .build();
    }

    public void GetBusArrival(String busStopCode, String serviceNo, ArrayList<BusInfoItem> busInfoItems, ArrayAdapter infoAdapter ){
        String url = String.format("http://datamall2.mytransport.sg/ltaodataservice/BusArrivalv2?BusStopCode=%1s&ServiceNo=%2s", busStopCode, serviceNo);
        CronetEngine.Builder engineBuilder = new CronetEngine.Builder(context);
        CronetEngine engine = engineBuilder.build();
//        Executor executor = Executors.newSingleThreadExecutor();
        ApiCallback apiCallback = new ApiCallback() {
            @Override
            void postSucceeded(UrlRequest request, UrlResponseInfo info, JSONObject respJo) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            ProcessBusArrivalEndpoint(respJo, busInfoItems);
                            infoAdapter.notifyDataSetChanged();
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


    public void GetBusStopMetadata(Context context, DBHandler dbHandler){
        dbHandler.DeleteTable();
        GetBusStopMetadata(context, 0);
    }


    public void GetBusStopMetadata(Context context, int skips){
        String url = String.format("http://datamall2.mytransport.sg/ltaodataservice/BusStops?$skip=%1s", skips);
        Executor executor = Executors.newSingleThreadExecutor();
        ApiCallback apiCallback = new ApiCallback() {
            @Override
            void postSucceeded(UrlRequest request, UrlResponseInfo info, JSONObject respJo) throws JSONException, ParseException {
                JSONArray busStopsJa = respJo.getJSONArray("value");
                if (busStopsJa.length() > 0){
                    dbHandler.PushBusStopMetadataToDB(busStopsJa);
                    int currentDbRows = dbHandler.CountDbRows();
                    System.out.println("LENGTH: " + currentDbRows);
                    GetBusStopMetadata(context, currentDbRows);
                }else{
                    Toast.makeText(context,"Bus Stops Metadata is updated",Toast.LENGTH_SHORT).show();
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
        System.out.println("lalala: " + busInfoJArr.toString());
        String busStopCode = respJo.getString("BusStopCode");
        Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0]; //TODO rm 0th element to update

        for(int i = 0; i < busInfoJArr.length(); i++){
            String busNo = busInfoJArr.getJSONObject(i).getString("ServiceNo");
            String[] arrivalTimeArr = Utils.ExtractArrivalTime(busInfoJArr.getJSONObject(i));
            busInfoItems.add( new BusInfoItem(busNo, arrivalTimeArr, busStopCode, busStopMetadata));
        }
    }
}
