package com.example.sgbustimingwidget.adapter;

import org.json.JSONException;

import java.util.Map;

public class BusInfoItem {
    private String busNo;
    private String busStopCode;
    private String busStopName;
    private String [] arrivalTimeArr;
    private Map<String, String> busStopMetadata;

    private String primaryBusType;
    private String primaryBusCapacity;
    private String widgetId;

    public BusInfoItem(String busNo, String[] arrivalTimeArr, String busStopCode,  Map<String, String> busStopMetadata) throws JSONException {
        this.busNo = busNo;
        this.arrivalTimeArr = arrivalTimeArr;
        this.busStopCode = busStopCode;
        this.busStopMetadata = busStopMetadata;
        widgetId = null;
    }



    public String getBusNo(){
        return busNo;
    }
    public String getPrimaryBusType(){
        return primaryBusType;
    }
    public String getPrimaryBusCapacity(){
        return primaryBusCapacity;
    }
    public String[] getArrivalTimeArr(){
        return arrivalTimeArr;
    }

    public String getWidgetId(){
        return widgetId;
    }

    public String getArrivalTimeStr(){
        if(arrivalTimeArr == null){
            return "not available";
        }
        String out = "";
        for(int i=0; i <3; i++){
            if(arrivalTimeArr[i] != "" ){
                out += arrivalTimeArr[i] + ", ";
            }
        }
        out = out.trim().replaceAll(",$", "") + " min";
        return out;
    }

    public String getArrivalTimePrimaryStr(){
        if(arrivalTimeArr.length == 0){
            return "N/A";
        }
        return arrivalTimeArr[0];
    }

    public String getArrivalTimeSecondaryStr(Boolean narrow){
        if(arrivalTimeArr.length == 0){
            return "not available";
        }else if(arrivalTimeArr[1] == ""){
            return "";
        }
        String out = "";
        if(!narrow){
            out+="and in ";
        }
        for(int i=1; i <3; i++){
            if(arrivalTimeArr[i] != "" ){
                out += arrivalTimeArr[i] + ", ";
            }
        }
        out = out.trim().replaceAll(",$", "") + " min";
        return out;
    }

    public String getBusStopCode(){
        return busStopCode;
    }
    public String getBusStopName(){
        return busStopMetadata.get("description");
    }
    public Map<String, String> getBusStopMetadata(){
        return busStopMetadata;
    }
    public void setPrimaryBusMetadata(String primaryBusType, String primaryBusCapacity){
        this.primaryBusType = primaryBusType;
        this.primaryBusCapacity = primaryBusCapacity;
    }

    public void setWidgetId(String widgetId){
        this.widgetId = widgetId;
    }
}
