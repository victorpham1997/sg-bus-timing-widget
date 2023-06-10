package com.example.sgbustimingwidget.widget;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.adapter.BusInfoItem;

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

            if(savedArrivalItems.get(position).getBusNo().equals("NODATA")){
                System.out.println("No data detected");
                views.setTextViewText(R.id.textBusNo, context.getString(R.string.no_data_prompt));
                views.setFloat(R.id.textBusNo, "setTextSize", 15);
                views.setInt(R.id.textBusNo, "setGravity", Gravity.CENTER);
                views.setViewPadding(R.id.textBusNo, 0, 50, 0, 50);
                views.setViewVisibility(R.id.layoutRight, View.GONE);
                return views;
            }

            views.setTextViewText(R.id.textBusArrivalSecondary, savedArrivalItems.get(position).getArrivalTimeSecondaryStr(narrow));

        }else{
            narrow = false;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_arrival_item_wide);
            System.out.println("WIDE ASS");
            System.out.println(widgetWidthCell);

            if(savedArrivalItems.get(position).getBusNo().equals("NODATA")){
                System.out.println("No data detected");
                views.setTextViewText(R.id.textBusNo, context.getString(R.string.no_data_prompt));
                views.setFloat(R.id.textBusNo, "setTextSize", 15);
                views.setInt(R.id.textBusNo, "setGravity", Gravity.CENTER);
                views.setViewPadding(R.id.textBusNo, 0, 50, 0, 50);
                views.setViewVisibility(R.id.layoutRight, View.GONE);
                return views;
            }

            if(!savedArrivalItems.get(position).getArrivalList().get(1).get("estimatedArrivalMin").equals("")){
                views.setViewVisibility(R.id.layoutNextBus2, View.VISIBLE);
                views.setInt(R.id.layoutNextBus2, "setBackgroundColor", context.getResources().getColor(getLayoutColor(savedArrivalItems.get(position).getArrivalList().get(1).get("estimatedArrivalMin"))));
                views.setTextViewText(R.id.textBusArrival2, savedArrivalItems.get(position).getArrivalList().get(1).get("estimatedArrivalMin"));
                views.setInt(R.id.iconBusStatus2, "setColorFilter", context.getResources().getColor(getBusIconColor(savedArrivalItems.get(position).getArrivalList().get(1).get("capacity"))));
                views.setImageViewResource(R.id.iconBusStatus2, getBusIcon(savedArrivalItems.get(position).getArrivalList().get(1).get("type")));
            }else{
                views.setViewVisibility(R.id.layoutNextBus2, View.INVISIBLE);
            }

            if(!savedArrivalItems.get(position).getArrivalList().get(2).get("estimatedArrivalMin").equals("")){
                views.setViewVisibility(R.id.layoutNextBus2, View.VISIBLE);
                views.setInt(R.id.layoutNextBus3, "setBackgroundColor", context.getResources().getColor(getLayoutColor(savedArrivalItems.get(position).getArrivalList().get(2).get("estimatedArrivalMin"))));
                views.setTextViewText(R.id.textBusArrival3, savedArrivalItems.get(position).getArrivalList().get(2).get("estimatedArrivalMin"));
                views.setInt(R.id.iconBusStatus3, "setColorFilter", context.getResources().getColor(getBusIconColor(savedArrivalItems.get(position).getArrivalList().get(2).get("capacity"))));
                views.setImageViewResource(R.id.iconBusStatus3, getBusIcon(savedArrivalItems.get(position).getArrivalList().get(2).get("type")));
            }else{
                views.setViewVisibility(R.id.layoutNextBus3, View.INVISIBLE);
            }
        }

        views.setTextViewText(R.id.textBusStopName, savedArrivalItems.get(position).getBusStopName());
        views.setTextViewText(R.id.textBusNo, savedArrivalItems.get(position).getBusNo());
        views.setInt(R.id.layoutNextBus, "setBackgroundColor", context.getResources().getColor(getLayoutColor(savedArrivalItems.get(position).getArrivalList().get(0).get("estimatedArrivalMin"))));

        String primaryArrivalTime = savedArrivalItems.get(position).getArrivalList().get(0).get("estimatedArrivalMin") ;
        if(primaryArrivalTime.equals("")){
            primaryArrivalTime = "N/A";
        }
        views.setTextViewText(R.id.textBusArrivalPrimary, primaryArrivalTime);
        setBusIconColorPrimary(savedArrivalItems.get(position).getArrivalList().get(0).get("capacity"), views);
        setPrimaryBusIcon(savedArrivalItems.get(position).getArrivalList().get(0).get("type"), views);


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

    public Integer getLayoutColor(String estimatedArrivalMin) {
        if (estimatedArrivalMin.equals("")) {
            return R.color.error_red;
        }
        Integer primary_arrival_time_int = Integer.parseInt(estimatedArrivalMin);
        if (primary_arrival_time_int <= 5) {
            return R.color.positive_green;
        } else {
            return R.color.dark_blue;
        }
    }

    public void setPrimaryBusIcon(String primaryBusType, RemoteViews rv) {
        Integer busIcon;
        if(primaryBusType == ""){
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

    public Integer getBusIcon(String primaryBusType){
        Integer busIcon;
        if(primaryBusType.equals("DD")) {
            busIcon = R.drawable.icon_double;
        } else {
            busIcon =  R.drawable.icon_single;
        }
        return busIcon;
    }

    public void setBusIconColorPrimary(String primaryBusCapacity, RemoteViews rv) {
        Integer busIconColor;
        if( primaryBusCapacity == ""){
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

    public Integer getBusIconColor(String busCapacity) {
        Integer busIconColor;

        if (busCapacity.equals("SEA")) {
            busIconColor = R.color.abs_green;
        }else if (busCapacity.equals("LSD")) {
            busIconColor = R.color.abs_red;
        }else{
            busIconColor = R.color.abs_orange;
        }
        return busIconColor;
    }
}
