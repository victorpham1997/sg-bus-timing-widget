package com.example.sgbustimingwidget.widget;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;
import android.widget.RemoteViews;

import com.example.sgbustimingwidget.ArrivalSelectionActivity;
import com.example.sgbustimingwidget.MainActivity;
import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.ApiCallback;
import com.example.sgbustimingwidget.network.NetworkEngine;
import com.example.sgbustimingwidget.utils.Utils;

import org.chromium.net.UrlRequest;
import org.chromium.net.UrlResponseInfo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

/**
 * Implementation of App Widget functionality.
 */
public class BusTimingWidgetSingle extends AppWidgetProvider {

    static String RELOAD_WIDGET = "RELOAD_WIDGET";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            DBHandler dbHandler = new DBHandler(context);

            String query = "SELECT * FROM " + DBHandler.SAVED_BUS_ARV_TABLE + " WHERE " + DBHandler.WIDGET_ID_COL + " = '" + String.valueOf(appWidgetId) + "'";
            Map<String, String>[] respArr = dbHandler.Query(query);
            if(respArr.length>0){
                Map<String, String> resp = respArr[0];
                String busStopCode = resp.get(DBHandler.CODE_COL);
                String busNo = resp.get(DBHandler.BUS_NO_COL);
                updateWidgetSingle(context, dbHandler, busStopCode, busNo, appWidgetId );
            }else{
                System.out.println("No bus value exist for this widget, please check DB");
                Map<String, String>[] table = dbHandler.GetTable(DBHandler.SAVED_BUS_ARV_TABLE);
                for(Map<String, String> row : table){
                    System.out.println(row);
                }
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(RELOAD_WIDGET)) {
            System.out.println("Reloading BusTimingWidgetSingle");
            ComponentName thisWidget = new ComponentName(context, BusTimingWidgetSingle.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(thisWidget));
        }

        if ("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS".equals(intent.getAction())){
            System.out.println("BusTimingWidgetSingle size changed");
            ComponentName thisWidget = new ComponentName(context, BusTimingWidgetSingle.class);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            onUpdate(context, appWidgetManager, appWidgetManager.getAppWidgetIds(thisWidget));
        }

    }

    public static void updateWidgetSingle(Context context, DBHandler dbHandler, String busStopCode, String busNo, int appWidgetId){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_arrival_single);

        views.setViewVisibility(R.id.reloadIcon, View.VISIBLE);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, views);

        NetworkEngine networkEngine = new NetworkEngine(context, dbHandler);

        Map<String, String> busStopMetadata = dbHandler.FindBusStop(busStopCode)[0];

        views.setTextViewText(R.id.textBusNo, busNo);
        views.setTextViewText(R.id.textBusStopName, busStopMetadata.get("description"));
//
//        Intent intent_bypass_bgres = new Intent(Settings.ACTION_IGNORE_BACKGROUND_DATA_RESTRICTIONS_SETTINGS, Uri.parse("package:" + context.getPackageName()));
//        intent_bypass_bgres.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//        context.startActivity(intent_bypass_bgres);

        Intent reload_intent = new Intent(context, BusTimingWidgetSingle.class);
        reload_intent.setAction(RELOAD_WIDGET);
        PendingIntent reload_pending_intent = PendingIntent.getBroadcast(context, 0, reload_intent, PendingIntent.FLAG_UPDATE_CURRENT| PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.layoutWidgetArrivalSingle, reload_pending_intent);

        ApiCallback apiCallback = new ApiCallback() {
            @Override
            public void postSucceeded(UrlRequest request, UrlResponseInfo info, JSONObject respJo) {
                try {
                    JSONArray busInfoJArr = (JSONArray) respJo.get("Services");
                    String[] arrivalTimeArr;
                    JSONObject busServiceJo;
                    if(busInfoJArr.length() == 0){
                        views.setTextViewText(R.id.textBusArrivalPrimary, "N/A");
                        views.setTextViewText(R.id.textBusArrivalSecondary, "");
                        views.setInt(R.id.iconBusStatus, "setBackgroundColor", context.getResources().getColor(R.color.abs_red));
                        views.setInt(R.id.iconBusStatus, "setColorFilter", context.getResources().getColor(R.color.abs_red));

                    }else{
                        busServiceJo = busInfoJArr.getJSONObject(0);
                        arrivalTimeArr = Utils.ExtractArrivalTime(busServiceJo);
                        views.setTextViewText(R.id.textBusArrivalPrimary, arrivalTimeArr[0] + " min");
                        views.setTextViewText(R.id.textBusArrivalSecondary, getArrivalTimeSecondaryStr(arrivalTimeArr));

                        String primaryBusType = busServiceJo.getJSONObject("NextBus").getString("Type");
                        String primaryBusCapacity = busServiceJo.getJSONObject("NextBus").getString("Load");
                        Integer busIcon;
                        Integer busIconColor;
                        Integer statusBackgroundColor = Integer.parseInt(arrivalTimeArr[0]);


                        if(primaryBusType.equals("DD")) {
                            busIcon = R.drawable.icon_double;
                        } else {
                            busIcon =  R.drawable.icon_single;
                        }

                        if (Integer.parseInt(arrivalTimeArr[0]) <= 5) {
                            statusBackgroundColor =  R.color.positive_green;
                        } else {
                            statusBackgroundColor =  R.color.dark_blue;
                        }

                        if (primaryBusCapacity.equals("SEA")) {
                            busIconColor = R.color.abs_green;
                        }else if (primaryBusCapacity.equals("LSD")) {
                            busIconColor = R.color.abs_red;
                        }else{
                            busIconColor = R.color.abs_orange;
                        }
                        views.setImageViewResource(R.id.iconBusStatus, busIcon);
                        views.setInt(R.id.iconBusStatus, "setBackgroundColor", context.getResources().getColor(statusBackgroundColor));
                        views.setInt(R.id.iconBusStatus, "setColorFilter", context.getResources().getColor(busIconColor));
                        views.setViewVisibility(R.id.reloadIcon, View.INVISIBLE);

                    }
                    appWidgetManager.updateAppWidget(appWidgetId, views);

                } catch (JSONException | ParseException e) {
                    e.printStackTrace();
                }
            }
        };
        networkEngine.GetBusArrival(busStopCode, busNo, apiCallback );
    }

    public static Integer getPrimaryLayoutColor(String[] arrivalTimeArr) {
        if (arrivalTimeArr.length == 0) {
            return R.color.error_red;
        }
        Integer primary_arrival_time_int = Integer.parseInt(arrivalTimeArr[0]);
        if (primary_arrival_time_int <= 5) {
            return R.color.positive_green;
        } else {
            return R.color.dark_blue;
        }
    }

    public static String getArrivalTimeSecondaryStr( String[] arrivalTimeArr){
        String out = "and\n ";

        if(arrivalTimeArr[1]==""){
            return "";
        }

        for(int i=1; i <3; i++){
            if(arrivalTimeArr[i] != "" ){
                out += arrivalTimeArr[i] + ",";
            }
        }
        out = out.trim().replaceAll(",$", "");
        return out;
    }


    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        DBHandler dbHandler = new DBHandler(context);
        for(int appWidgetId : appWidgetIds){
            dbHandler.WriteDataToTable(DBHandler.SAVED_BUS_ARV_TABLE, "widgetid=''", "widgetid='"+String.valueOf(appWidgetId)+"'");
        }

    }
}