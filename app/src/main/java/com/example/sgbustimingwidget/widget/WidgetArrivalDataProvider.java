package com.example.sgbustimingwidget.widget;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoItem;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;

import java.util.ArrayList;

public class WidgetArrivalDataProvider implements RemoteViewsService.RemoteViewsFactory {

    Context context;
    Intent intent;
    private int widgetWidthCell;

    private ArrayList<BusInfoItem> savedArrivalItems;

    public WidgetArrivalDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;

    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        savedArrivalItems = BusTimingWidget.savedArrivalItems;
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public int getCount() {
        return savedArrivalItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        System.out.println("WORKIUDBNG MY ASS FOFF");

        widgetWidthCell = Integer.valueOf(intent.getData().getSchemeSpecificPart());

        RemoteViews views;
        Boolean narrow;


        if(widgetWidthCell <340){
            narrow = true;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_arrival_item_narrow);
            System.out.println("NARROW ASS");
            System.out.println(widgetWidthCell);

        }else{
            narrow = false;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_arrival_item_wide);
            System.out.println("WIDE ASS");
            System.out.println(widgetWidthCell);

        }

        if(savedArrivalItems.get(position).getBusNo().equals("NODATA")){
            System.out.println("No data detected");
            views.setTextViewText(R.id.textBusNo, context.getString(R.string.no_data_prompt));
            views.setFloat(R.id.textBusNo, "setTextSize", 15);
            views.setInt(R.id.textBusNo, "setGravity", Gravity.CENTER);
            views.setViewPadding(R.id.textBusNo, 0, 50, 0, 50);
            views.setViewVisibility(R.id.layoutRight, View.GONE);
            return views;
        }



        String[] arrivalTimeArr = savedArrivalItems.get(position).getArrivalTimeArr();

        views.setTextViewText(R.id.textBusStopName, savedArrivalItems.get(position).getBusStopName());
        views.setTextViewText(R.id.textBusNo, savedArrivalItems.get(position).getBusNo());
        views.setInt(R.id.layoutNextBus, "setBackgroundColor", context.getResources().getColor(getPrimaryLayoutColor(arrivalTimeArr)));
        views.setTextViewText(R.id.textBusArrivalPrimary, savedArrivalItems.get(position).getArrivalTimePrimaryStr());
        views.setTextViewText(R.id.textBusArrivalSecondary, savedArrivalItems.get(position).getArrivalTimeSecondaryStr(narrow));
        setBusIconColor(savedArrivalItems.get(position).getPrimaryBusCapacity(), views);
        getPrimaryBusIcon(savedArrivalItems.get(position).getPrimaryBusType(), views);

        return views;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    public Integer getPrimaryLayoutColor(String[] arrivalTimeArr) {
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

    public void getPrimaryBusIcon(String primaryBusType, RemoteViews rv) {
        Integer busIcon;
        if(primaryBusType == null){
            rv.setViewVisibility(R.id.iconBusStatus, View.GONE);
            rv.setViewVisibility(R.id.textPrimaryMin, View.GONE);
            return;
        }
        rv.setViewVisibility(R.id.iconBusStatus, View.VISIBLE);
        rv.setViewVisibility(R.id.textPrimaryMin, View.VISIBLE);

        if(primaryBusType.equals("DD")) {
            busIcon = R.drawable.icon_double;
        } else {
            busIcon =  R.drawable.icon_single;
        }
        rv.setImageViewResource(R.id.iconBusStatus, busIcon);
    }

    public void setBusIconColor(String primaryBusCapacity, RemoteViews rv) {
        Integer busIconColor;
        if( primaryBusCapacity == null){
            rv.setViewVisibility(R.id.iconBusStatus, View.GONE);
            rv.setViewVisibility(R.id.textPrimaryMin, View.GONE);
            return;
        }
        rv.setViewVisibility(R.id.iconBusStatus, View.VISIBLE);
        rv.setViewVisibility(R.id.textPrimaryMin, View.VISIBLE);
        if (primaryBusCapacity.equals("SEA")) {
            busIconColor = R.color.abs_green;
        }else if (primaryBusCapacity.equals("LSD")) {
            busIconColor = R.color.abs_red;
        }else{
            busIconColor = R.color.abs_orange;
        }
        rv.setInt(R.id.iconBusStatus, "setColorFilter", context.getResources().getColor(busIconColor));
    }
}
