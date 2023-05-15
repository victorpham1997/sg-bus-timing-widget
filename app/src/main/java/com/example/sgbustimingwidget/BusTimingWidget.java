package com.example.sgbustimingwidget;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.viewpager.widget.ViewPager;

import com.example.sgbustimingwidget.adapter.WidgetPagerAdapter;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;

import java.util.Map;


public class BusTimingWidget extends AppWidgetProvider {
    private WidgetPagerAdapter widgetPagerAdapter;

    static String ACTION_MY_BUTTON = "CLICKED";
    static int arrival_page_index;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i=0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            SQLiteDatabase db = MainActivity.getDb();
            Map<String, String>[] arrivalTable = DBHandler.GetTable("savedbusarrival", db);

            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bus_timing_widget);
//            SharedPreferences sharedPreferences = context.getSharedPreferences("WIDGET_SP", Context.MODE_PRIVATE);
//            int test = sharedPreferences.getInt("WIDGET_INDEX", 00);
//            views.setTextViewText(R.id.textBusNumLeft, Integer.toString(test));

            Map<String, String> arrival_l = arrivalTable[arrival_page_index*2];
            Map<String, String> arrival_r = arrivalTable[arrival_page_index*2 + 1];
            Map<String, String> busStopMetadata_l = DBHandler.FindBusStop(arrival_l.get("busno"), db)[0];
            Map<String, String> busStopMetadata_r = DBHandler.FindBusStop(arrival_r.get("busno"), db)[0];

            views.setTextViewText(R.id.textBusNumLeft, arrival_l.get("busno"));
            views.setTextViewText(R.id.textBusStopNameLeft, busStopMetadata_l.get("description") );
//
            views.setTextViewText(R.id.textBusNumRight, arrival_r.get("busno"));
            views.setTextViewText(R.id.textBusStopNameRight, busStopMetadata_r.get("description") );


            // Create an Intent to launch ExampleActivity

//            Intent intent = new Intent(context, BusTimingWidget.class);
//            intent.setAction(ACTION_MY_BUTTON);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0,intent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
//            views.setOnClickPendingIntent(R.id.buttonScrollLeft, pendingIntent);
//
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        System.out.println("BOI BOI BOI");
        final String action = intent.getAction();
        System.out.println(action);

        if (ACTION_MY_BUTTON.equals(action)){
            System.out.println("OOI CLICKED ISNT IT?");


//            Toast.makeText(context, "WOHOO my Button Clicked", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putInt("WIDGET_INDEX", 0);
//        editor.apply();
        arrival_page_index = 0;
        System.out.println("I AM HERE");

    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}