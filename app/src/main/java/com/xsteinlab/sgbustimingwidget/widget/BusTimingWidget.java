package com.xsteinlab.sgbustimingwidget.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.RemoteViews;

import com.xsteinlab.sgbustimingwidget.MainActivity;
import com.xsteinlab.sgbustimingwidget.R;
import com.xsteinlab.sgbustimingwidget.adapter.BusInfoItem;
import com.xsteinlab.sgbustimingwidget.database.DBHandler;
import com.xsteinlab.sgbustimingwidget.network.ApiCallback;
import com.xsteinlab.sgbustimingwidget.network.NetworkEngine;
import com.xsteinlab.sgbustimingwidget.utils.Utils;

import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class BusTimingWidget extends AppWidgetProvider {

    public static String INTENT_RELOAD_WIDGET = "INTENT_RELOAD_WIDGET";
    public static String INTENT_UPDATE_REFRESH_DATE = "INTENT_UPDATE_REFRESH_DATE";
    public static String INTENT_START_UPDATE = "INTENT_START_UPDATE";


    public static String EXTRA_FRAGMENT_STRING = "EXTRA_FRAGMENT_STRING";
    public static String EXTRA_DATE_STRING = "EXTRA_DATE_STRING";

    public static String EXTRA_SAVED_ARRIVAL = "EXTRA_SAVED_ARRIVAL";
    public static String INTENT_SAVED_ARRIVAL = "INTENT_SAVED_ARRIVAL";




    public static ArrayList<BusInfoItem> savedArrivalItems;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int appWidgetId : appWidgetIds) {

            Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
            // Get min width and height.
            int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
            int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

            Utils.localLogging(String.format("Widget size: %sx%s %sx%s",width,height,Utils.getCellsForSize(width),Utils.getCellsForSize(height)));

            UpdateSavedArrivalItems(context);

//            Intent savedArrivalIntent = new Intent(INTENT_SAVED_ARRIVAL);
//            savedArrivalIntent.putExtra(EXTRA_SAVED_ARRIVAL, savedArrivalItems);
//            context.sendBroadcast(savedArrivalIntent);

            RemoteViews views = GetNewLayoutFromSize(context,width,height);
            Intent list_view_intent = new Intent(context, WidgetRemoteViewService.class);
            list_view_intent.setData(Uri.fromParts("content", String.valueOf(width), null));
            views.setRemoteAdapter(R.id.widget_list, list_view_intent);

            Intent reloadIntent = new Intent(context, getClass());
            reloadIntent.setAction(INTENT_RELOAD_WIDGET);
            PendingIntent reloadPendingIntent = PendingIntent.getBroadcast(context, 0, reloadIntent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.buttonReload, reloadPendingIntent);


            Intent openAppIntent = new Intent(context, MainActivity.class);
            openAppIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            openAppIntent.putExtra(EXTRA_FRAGMENT_STRING, "saved_arrival");
            PendingIntent openAppPendingIntent = PendingIntent.getActivity(context, 2, openAppIntent, PendingIntent.FLAG_IMMUTABLE);
            views.setOnClickPendingIntent(R.id.textTitle, openAppPendingIntent);


            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        Utils.localLogging("BusTimingWidget intent received");
        final String action = intent.getAction();
        Utils.localLogging(action);

        if (INTENT_RELOAD_WIDGET.equals(action)){
            Utils.logToFile("Reloading BusTimingWidget widget");
            Utils.localLogging("Reloading BusTimingWidget widget");
            ComponentName thisWidget = new ComponentName(context, BusTimingWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(thisWidget));
        }

        if (INTENT_UPDATE_REFRESH_DATE.equals(action)){
            Utils.localLogging("Updating BusTimingWidget refresh date");
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

            Bundle extras = intent.getExtras();
            if(extras != null) {
                String refreshStr= extras.getString(EXTRA_DATE_STRING);

                ComponentName thisWidget = new ComponentName(context, BusTimingWidget.class);
                for (int id : appWidgetManager.getAppWidgetIds(thisWidget)) {
                    Bundle options = appWidgetManager.getAppWidgetOptions(id);
                    int width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
                    int height = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);
                    RemoteViews views = GetNewLayoutFromSize(context, width, height);
                    views.setTextViewText(R.id.textLastUpdate , refreshStr);
                    appWidgetManager.partiallyUpdateAppWidget(id, views);
                }
            }
        }

        if ("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS".equals(action)){
            Utils.localLogging("BusTimingWidget size changed");
            ComponentName thisWidget = new ComponentName(context, BusTimingWidget.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(thisWidget));
        }
    }

    @Override
    public void onRestored(Context context, int[] oldWidgetIds, int[] newWidgetIds) {
        super.onRestored(context, oldWidgetIds, newWidgetIds);

    }

    @Override
    public void onEnabled(Context context) {

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    public RemoteViews GetNewLayoutFromSize(Context context, int width, int height){
        int cellWidth = Utils.getCellsForSize(width);
        int cellHeight = Utils.getCellsForSize(height);
        if(cellHeight==1){
            return new RemoteViews(context.getPackageName(), R.layout.widget_arrival_style1);
        }else{
            return new RemoteViews(context.getPackageName(), R.layout.widget_arrival_style2);
        }
    }

    public static ArrayList<BusInfoItem> getErrorArrivalItems(Context context) throws JSONException {
        if (savedArrivalItems == null){
            Utils.logToFile("Error retrieving since savedArrivalItems is null");
        }else{
            Utils.logToFile("Error retrieving BUT savedArrivalItems is NOT null");
        }

        ArrayList<BusInfoItem> errorArrivalItems = new ArrayList<BusInfoItem>();
        BusInfoItem busInfoItem = new BusInfoItem("ERR", null, null, null);
        errorArrivalItems.add(busInfoItem);


        Intent reloadIntent = new Intent(context, BusTimingWidget.class);
        reloadIntent.setAction(INTENT_RELOAD_WIDGET);
        context.sendBroadcast(reloadIntent);

        return errorArrivalItems;
    }

//    public static ArrayList<BusInfoItem> getSavedArrivalItems() throws JSONException {
//
//        if(savedArrivalItems == null){
//            ArrayList<BusInfoItem> errorArrivalItems = new ArrayList<BusInfoItem>();
//            BusInfoItem busInfoItem = new BusInfoItem("ERR", null, null, null);
//            errorArrivalItems.add(busInfoItem);
//            return errorArrivalItems;
//        }
//        Collections.sort(savedArrivalItems, new Comparator<BusInfoItem>(){
//            public int compare(BusInfoItem b1, BusInfoItem b2){
//                Utils.localLogging(b1);
//                Utils.localLogging(b2);
//                return b1.getBusStopName().compareTo(b2.getBusStopName())*10 + b1.getBusNo().compareTo(b2.getBusNo()) ;
//            }
//        });
//        return savedArrivalItems;
//    }

    public void UpdateSavedArrivalItems(Context context){
        savedArrivalItems = new ArrayList<BusInfoItem>();
        DBHandler dbHandler = new DBHandler(context);
        NetworkEngine networkEngine = new NetworkEngine(context, dbHandler);
        Map<String, String>[] arrivalTable = dbHandler.GetTable(DBHandler.SAVED_BUS_ARV_TABLE);

        Intent startUpdateIntent = new Intent(context, BusTimingWidget.class);
        startUpdateIntent.setAction(INTENT_UPDATE_REFRESH_DATE);
        String refreshStr = "Refreshing . . .";
        startUpdateIntent.putExtra(EXTRA_DATE_STRING, refreshStr);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, BusTimingWidget.class));
        if(appWidgetIds != null && appWidgetIds.length > 0) {
            startUpdateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(startUpdateIntent);
        }

//        Intent intent_bypass_bgres = new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
//        intent_bypass_bgres.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent_bypass_bgres);

        if(arrivalTable.length == 0){
            try {
                BusInfoItem busInfoItem = new BusInfoItem("NODATA", null, null, null);
                savedArrivalItems.add(busInfoItem);
                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

                Intent intent = new Intent(context, BusTimingWidget.class);
                intent.setAction(INTENT_UPDATE_REFRESH_DATE);
                String currentDateTime = Utils.GetCurrentSgtTime("dd-MM-yyyy' 'HH:mm");
                String lastUpdateStr = "Last update " + currentDateTime;
                intent.putExtra(EXTRA_DATE_STRING, lastUpdateStr);
                if(appWidgetIds != null && appWidgetIds.length > 0) {
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                    context.sendBroadcast(intent);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            for(int i = 0; i < arrivalTable.length; i++){
                Utils.localLogging("Existing stops:" + arrivalTable[i].get("code") + "---" + arrivalTable[i].get("busno"));
                String busNo =arrivalTable[i].get("busno");
                String busStopCode =arrivalTable[i].get("code");
                Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0];
                int index = i;
                ApiCallback apiCallback = new ApiCallback() {
                    @Override
                    public void postSucceeded(UrlRequest request, UrlResponseInfo info, JSONObject respJo) {
                        try {
                            JSONArray busInfoJArr = (JSONArray) respJo.get("Services");
//                            String[] arrivalTimeArr;
                            List<Map<String, String>> arrivalList;
                            JSONObject busServiceJo;
                            BusInfoItem busInfoItem;

                            if(busInfoJArr.length() == 0){
                                busServiceJo = null;
//                                arrivalList = new ArrayList<>(0);
//                                busInfoItem = new BusInfoItem(busNo, arrivalList, busNo, busStopMetadata);
//                                busInfoItem.setPrimaryBusMetadata(null,null);
                            }else{
                                busServiceJo = busInfoJArr.getJSONObject(0);
//                                arrivalTimeArr = Utils.ExtractArrivalTime(busServiceJo);
//                                arrivalList = Utils.ExtractArrival(busServiceJo);
//                                busInfoItem = new BusInfoItem(busNo, arrivalList, busNo, busStopMetadata);
//                                busInfoItem.setPrimaryBusMetadata(busServiceJo.getJSONObject("NextBus").getString("Type"), busServiceJo.getJSONObject("NextBus").getString("Load"));
                            }
                            arrivalList = Utils.ExtractArrival(busServiceJo);
                            busInfoItem = new BusInfoItem(busNo, arrivalList, busNo, busStopMetadata);
                            savedArrivalItems.add(busInfoItem);

                            if(savedArrivalItems.size() ==arrivalTable.length){

                                for(int i = 0; i < savedArrivalItems.size(); i++){
                                    Utils.localLogging(savedArrivalItems.get(i).getBusStopName());
                                    Utils.logToFile(savedArrivalItems.get(i).getBusStopName());
                                }
//                                Collections.sort(savedArrivalItems, new Comparator<BusInfoItem>(){
//                                    public int compare(BusInfoItem b1, BusInfoItem b2){
//                                        Utils.localLogging(b1);
//                                        Utils.localLogging(b2);
//                                        return b1.getBusStopName().compareTo(b2.getBusStopName())*10 + b1.getBusNo().compareTo(b2.getBusNo()) ;
//                                    }
//                                });
                                Utils.logToFile("Completed loading widget data");
                                Utils.localLogging("Completed loading widget data");
                                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                                int appWidgetIds[] = appWidgetManager.getAppWidgetIds(new ComponentName(context, BusTimingWidget.class));
                                appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list);

                                Intent intent = new Intent(context, BusTimingWidget.class);
                                intent.setAction(INTENT_UPDATE_REFRESH_DATE);
                                String currentDateTime = Utils.GetCurrentSgtTime("dd-MM-yyyy' 'HH:mm");
                                String lastUpdateStr = "Last update " + currentDateTime;
                                intent.putExtra(EXTRA_DATE_STRING, lastUpdateStr);
                                if(appWidgetIds != null && appWidgetIds.length > 0) {
                                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
                                    context.sendBroadcast(intent);
                                }
                            }
                        } catch (JSONException | ParseException e) {
                            e.printStackTrace();
                        }
                    }
                };
                Utils.localLogging("__________PAGING FOR " + arrivalTable[i].get("code") + "---"+ arrivalTable[i].get("busno"));
                networkEngine.GetBusArrival(arrivalTable[i].get("code"), arrivalTable[i].get("busno"), apiCallback );
            }
        }
    }

}