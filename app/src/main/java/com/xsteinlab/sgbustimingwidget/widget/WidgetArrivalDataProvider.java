package com.xsteinlab.sgbustimingwidget.widget;

import android.content.Context;
import android.content.Intent;
import android.view.Gravity;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.xsteinlab.sgbustimingwidget.R;
import com.xsteinlab.sgbustimingwidget.adapter.BusInfoItem;
import com.xsteinlab.sgbustimingwidget.utils.Utils;

import org.json.JSONException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class WidgetArrivalDataProvider implements RemoteViewsService.RemoteViewsFactory  {

    Context context;
    Intent intent;
    private int widgetWidthCell;
//    private BroadcastReceiver mIntentListener;


    private ArrayList<BusInfoItem> savedArrivalItems;

    public WidgetArrivalDataProvider(Context context, Intent intent) {
        this.context = context;
        this.intent = intent;
        savedArrivalItems = new ArrayList<>();
//        setupIntentListener();

    }

//    private void setupIntentListener() {
//        if (mIntentListener == null) {
//            mIntentListener = new BroadcastReceiver() {
//                @Override
//                public void onReceive(Context context, Intent intent) {
//                    // Update mUrl through BroadCast Intent
//                    Utils.localLogging("-------------------WHATTTTTT");
//                    savedArrivalItems = (ArrayList<BusInfoItem>) intent.getSerializableExtra(BusTimingWidget.EXTRA_SAVED_ARRIVAL);
//                }
//            };
//            IntentFilter filter = new IntentFilter();
//            filter.addAction(BusTimingWidget.INTENT_SAVED_ARRIVAL);
//            context.registerReceiver(mIntentListener, filter);
//        }
//    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
        if(BusTimingWidget.savedArrivalItems == null){
            try {
                savedArrivalItems = BusTimingWidget.getErrorArrivalItems(context);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }else{
            savedArrivalItems = (ArrayList<BusInfoItem>) BusTimingWidget.savedArrivalItems.clone();
            savedArrivalItems = Utils.removeBusInfoListDup(savedArrivalItems);
            Collections.sort(savedArrivalItems, new Comparator<BusInfoItem>(){
                public int compare(BusInfoItem b1, BusInfoItem b2){
                    Utils.localLogging(b1.toString());
                    Utils.localLogging(b2.toString());
                    return b1.getBusStopName().compareTo(b2.getBusStopName())*10 + b1.getBusNo().compareTo(b2.getBusNo()) ;
                }
            });
        }
    }

    @Override
    public void onDestroy() {
//        if (mIntentListener != null) {
//            context.unregisterReceiver(mIntentListener);
//            mIntentListener = null;
//        }
    }

    @Override
    public int getCount() {
        Utils.localLogging("SIZE OF ARR" + savedArrivalItems.size());
        return savedArrivalItems.size();
    }

    @Override
    public RemoteViews getViewAt(int position) {

        Utils.localLogging("WORKIUDBNG MY ASS FOFF" + position);

        widgetWidthCell = Integer.valueOf(intent.getData().getSchemeSpecificPart());

        RemoteViews views;
        Boolean narrow;


        if(widgetWidthCell <340){
            narrow = true;
            views = new RemoteViews(context.getPackageName(), R.layout.widget_arrival_item_narrow);
            Utils.localLogging("NARROW ASS");
            Utils.localLogging(String.valueOf(widgetWidthCell));

            if(savedArrivalItems.get(position).getBusNo().equals("NODATA")){
                Utils.localLogging("No data detected");
                views.setTextViewText(R.id.textBusNo, context.getString(R.string.no_data_prompt));
                views.setFloat(R.id.textBusNo, "setTextSize", 15);
                views.setInt(R.id.textBusNo, "setGravity", Gravity.CENTER);
                views.setViewPadding(R.id.textBusNo, 0, 50, 0, 50);
                views.setViewVisibility(R.id.layoutRight, View.GONE);
                return views;
            }

            if(savedArrivalItems.get(position).getBusNo().equals("ERR")){
                Utils.localLogging("Error");
                views.setTextViewText(R.id.textBusNo, context.getString(R.string.err_data_prompt));
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
            Utils.localLogging("WIDE ASS");
            Utils.localLogging(String.valueOf(widgetWidthCell));

            if(savedArrivalItems.get(position).getBusNo().equals("NODATA")){
                Utils.localLogging("No data detected");
                views.setTextViewText(R.id.textBusNo, context.getString(R.string.no_data_prompt));
                views.setFloat(R.id.textBusNo, "setTextSize", 15);
                views.setInt(R.id.textBusNo, "setGravity", Gravity.CENTER);
                views.setViewPadding(R.id.textBusNo, 0, 50, 0, 50);
                views.setViewVisibility(R.id.layoutRight, View.GONE);
                return views;
            }

            if(savedArrivalItems.get(position).getBusNo().equals("ERR")){
                Utils.localLogging("Error");
                views.setTextViewText(R.id.textBusNo, context.getString(R.string.err_data_prompt));
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
                views.setViewVisibility(R.id.layoutNextBus3, View.VISIBLE);
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
