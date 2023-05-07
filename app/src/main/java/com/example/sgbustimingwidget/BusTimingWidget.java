package com.example.sgbustimingwidget;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.RemoteViews;

import androidx.viewpager.widget.ViewPager;

import com.example.sgbustimingwidget.adapter.WidgetPagerAdapter;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;


public class BusTimingWidget extends AppWidgetProvider {
//    https://www.digitalocean.com/community/tutorials/android-viewpager-example-tutorial
    private WidgetPagerAdapter widgetPagerAdapter;
    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    private ViewPager viewPager;


    public void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

//        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bus_timing_widget);




//        widgetPagerAdapter = new WidgetPagerAdapter(context, dbHandler, networkEngine );
//        views.setTextViewText(R.id.appwidget_text, widgetText);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        dbHandler = new DBHandler(context);
        networkEngine = new NetworkEngine(context, dbHandler);


    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}