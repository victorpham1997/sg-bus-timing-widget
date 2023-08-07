package com.xsteinlab.sgbustimingwidget.widget;

import android.content.Intent;
import android.widget.RemoteViewsService;

public class WidgetRemoteViewService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new WidgetArrivalDataProvider(this.getApplicationContext(), intent);
    }
}
