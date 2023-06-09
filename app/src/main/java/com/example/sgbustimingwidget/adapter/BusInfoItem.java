package com.example.sgbustimingwidget.adapter;

import org.json.JSONException;

import java.util.List;
import java.util.Map;

public class BusInfoItem {
    private String busNo;
    private String busStopCode;
    private String busStopName;
    private String [] arrivalTimeArr;
    private List<Map<String, String>> arrivalList;
    private Map<String, String> busStopMetadata;

    private String primaryBusType;
    private String primaryBusCapacity;
    private String widgetId;

    public BusInfoItem(String busNo, List<Map<String, String>> arrivalList, String busStopCode, Map<String, String> busStopMetadata) throws JSONException {
        this.busNo = busNo;
        this.arrivalList = arrivalList;
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
    public List<Map<String, String>> getArrivalList(){
        return arrivalList;
    }

    public String getWidgetId(){
        return widgetId;
    }

    public String getArrivalTimeStr(){
        String out = "";
        System.out.println(arrivalList);
        for(int i=0; i <arrivalList.size(); i++){
            if(!arrivalList.get(i).get("estimatedArrivalMin").equals("")){
                out += arrivalList.get(i).get("estimatedArrivalMin") + ", ";
            }
        }
        if(out.equals("")){
            return "not available";
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
        String out = "";
        System.out.println(arrivalList);
        for(int i=1; i <arrivalList.size(); i++){
            if(!arrivalList.get(i).get("estimatedArrivalMin").equals("")){
                out += arrivalList.get(i).get("estimatedArrivalMin") + ", ";
            }
        }
        if(out.equals("")){
            return "";
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
