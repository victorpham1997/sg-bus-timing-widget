package com.example.sgbustimingwidget.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.viewpager.widget.PagerAdapter;

import com.example.sgbustimingwidget.R;
import com.example.sgbustimingwidget.database.DBHandler;
import com.example.sgbustimingwidget.network.NetworkEngine;

import java.util.Map;

public class WidgetPagerAdapter extends PagerAdapter {
    private Context context;
    private LayoutInflater layoutInflater;
    private DBHandler dbHandler;
    private NetworkEngine networkEngine;

    public WidgetPagerAdapter(Context context, DBHandler dbHandler, NetworkEngine networkEngine) {
        this.context = context;
        this.layoutInflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.dbHandler = dbHandler;
        this.networkEngine = networkEngine;
    }

//    https://codetheory.in/android-image-slideshow-using-viewpager-pageradapter/

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        int index_left = position*2;
        int index_right = position*2 + 1;
        View itemView = layoutInflater.inflate(R.layout.bus_info_widget_item, container, false);
        Map<String, String>[] arrivalTable = this.dbHandler.GetTable("savedbusarrival");
        Map<String, String> busStopMetadataLeft = dbHandler.FindBusStop(arrivalTable[index_left].get("code"))[0];

        TextView textBusStopNameLeft =  itemView.findViewById(R.id.textBusStopNameLeft);
        TextView textBusNumLeft =  itemView.findViewById(R.id.textBusNumLeft);
        TextView textBusArrivalLeft =  itemView.findViewById(R.id.textBusArrivalLeft);


        textBusStopNameLeft.setText(busStopMetadataLeft.get("description"));
        textBusNumLeft.setText(arrivalTable[index_left].get("busno"));
        networkEngine.GetWidgetBusArrival(arrivalTable[index_left].get("code"), arrivalTable[index_left].get("busno"), textBusArrivalLeft );

        if(index_right < arrivalTable.length) {
            Map<String, String> busStopMetadataRight = dbHandler.FindBusStop(arrivalTable[index_right].get("code"))[0];
            TextView textBusStopNameRight = itemView.findViewById(R.id.textBusStopNameRight);
            TextView textBusNumRight = itemView.findViewById(R.id.textBusNumRight);
            TextView textBusArrivalRight = itemView.findViewById(R.id.textBusArrivalRight);

            textBusStopNameRight.setText(busStopMetadataRight.get("description"));
            textBusNumRight.setText(arrivalTable[index_right].get("busno"));
            networkEngine.GetWidgetBusArrival(arrivalTable[index_right].get("code"), arrivalTable[index_right].get("busno"), textBusArrivalRight );
        }


        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, Object view) {
        collection.removeView((View) view);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public int getCount() {
        return (int) Math.ceil(dbHandler.CountDbRows()/2);
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

}
