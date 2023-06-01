package com.example.sgbustimingwidget.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;


import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoAdapter;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.adapter.SavedArrivalAdapter;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.utils.Utils;

import org.chromium.net.CronetException;
import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ApiCallback_legacy extends UrlRequest.Callback{

    private static final int BYTE_BUFFER_CAPACITY_BYTES = 64 * 1024;
    private final ByteArrayOutputStream bytesReceived = new ByteArrayOutputStream();
    private final WritableByteChannel receiveChannel = Channels.newChannel(bytesReceived);


    private static final String TAG = "MyApiRequestCallback";
    private Context context;
    private Handler handler;
    private NetworkEngine networkEngine;
    private DBHandler dbHandler;
    private ArrayList<BusInfoItem> busInfoItems;
    private ArrayAdapter adapter;
    private ListView ls;
    private final long startTimeNanos;


    public ApiCallback_legacy(Context context, DBHandler dbHandler){
        this.context = context;
        this.handler  = new Handler(context.getMainLooper());
        this.dbHandler = dbHandler;
        startTimeNanos = System.nanoTime();
//        sharedPref = context.getSharedPreferences("SharedPref", Context.MODE_PRIVATE);
    }


    @Override
    public void onRedirectReceived(UrlRequest request, UrlResponseInfo info, String newLocationUrl) throws Exception {
        Log.i(TAG, "onRedirectReceived method called.");
        request.followRedirect();
    }

    @Override
    public void onResponseStarted(UrlRequest request, UrlResponseInfo info) throws Exception {
        Log.i(TAG, "onResponseStarted method called.");
//        Log.i(TAG, info.getUrl());
        bytesReceived.reset();
        request.read(ByteBuffer.allocateDirect(BYTE_BUFFER_CAPACITY_BYTES));
    }

    @Override
    public void onReadCompleted(UrlRequest request, UrlResponseInfo info, ByteBuffer byteBuffer) throws Exception {
        Log.i(TAG, "onReadCompleted method called.");
//        Log.i(TAG, info.getUrl());
        byteBuffer.flip();
        try {
            receiveChannel.write(byteBuffer);
        } catch (IOException e) {
            android.util.Log.i(TAG, "IOException during ByteBuffer read. Details: ", e);
        }
        byteBuffer.clear();
        request.read(byteBuffer);
    }

    @Override
    public void onSucceeded(UrlRequest request, UrlResponseInfo info) {
        Log.i(TAG, "onSucceeded method called.");

        long latencyNanos = System.nanoTime() - startTimeNanos;

        byte[] bodyBytes = bytesReceived.toByteArray();
        String packet = new String(bodyBytes);
        System.out.println("sssss: " + packet);

        try{
            JSONObject respJo = new JSONObject(packet);
            if (info.getUrl().contains("BusArrivalv2")){
                ProcessBusArrivalEndpoint(respJo);
            }else if(info.getUrl().contains("BusStops")){
                ProcessBusStopsEndpoints(respJo);
            }
        } catch (
                JSONException | ParseException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onFailed(UrlRequest request, UrlResponseInfo info, CronetException error) {

    }


    public void setNetworkEngine(NetworkEngine networkEngine){
        this.networkEngine = networkEngine;
    }

    public void setBusInfoItems(ArrayList<BusInfoItem> busInfoItems){
        this.busInfoItems = busInfoItems;
    }

    public void setAdapter(ArrayAdapter adapter){
        this.adapter = adapter;
    }

    public void ProcessBusArrivalEndpoint(JSONObject respJo) throws JSONException, ParseException {
        JSONArray busInfoJArr = (JSONArray) respJo.get("Services");
        System.out.println("lalala: " + busInfoJArr.toString());
        String busStopCode = respJo.getString("BusStopCode");
        Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0]; //TODO rm 0th element to update

        handler.post(new Runnable() {
            @Override
            public void run() {
                    try {
                        for(int i = 0; i < busInfoJArr.length(); i++){
                            String busNo = busInfoJArr.getJSONObject(i).getString("ServiceNo");
                            String[] arrivalTimeArr = Utils.ExtractArrivalTime(busInfoJArr.getJSONObject(i));
                        busInfoItems.add( new BusInfoItem(busNo, arrivalTimeArr, busStopCode, busStopMetadata));
                        }
                    } catch (JSONException | ParseException e) {
                        e.printStackTrace();
                    }
                adapter.notifyDataSetChanged();
            }
        });
    }

    public void ProcessBusStopsEndpoints(JSONObject respJo) throws JSONException {
        JSONArray busStopsJa = respJo.getJSONArray("value");
        if (busStopsJa.length() > 0){
            dbHandler.PushBusStopMetadataToDB(busStopsJa);
            int currentDbRows = dbHandler.CountDbRows(DBHandler.BUS_STOP_TABLE);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    System.out.println("LENGTH: " + currentDbRows);
                    networkEngine.GetBusStopMetadata(context, currentDbRows);
                }
            });
        }else{
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context,"Bus Stops Metadata is updated",Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

}
