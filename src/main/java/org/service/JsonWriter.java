package org.service;

import net.sf.json.JSONObject;
import net.sf.json.JSONArray;

/**
 * @author Z_HAO 2020/11/18
 */
public class JsonWriter {
    public static void writePlaceMessage(JSONArray jsonArray , String placeName , String placeOnName , String status) {
        JSONObject object = new JSONObject();
        object.put("placeName" , placeName);
        object.put("placeOnName" , placeOnName);
        object.put("status" , status);
        jsonArray.add(object);
    }

    public static void writeCreateMessage(JSONArray jsonArray , String createName , String createOnName , String status) {
        JSONObject object = new JSONObject();
        object.put("createName" , createName);
        object.put("createOnName" , createOnName);
        object.put("status" , status);
        jsonArray.add(object);
    }

    public static void writeExecTime(JSONObject jsonObject , long time) {
        jsonObject.put("execTime" , time);
    }

    public static void writeAppLoopDelay(JSONObject jsonObject , String loopName , double delay) {
        jsonObject.put(loopName , delay);
    }

    public static void writeTuple(JSONObject jsonObject , String tupleName , double value) {
        jsonObject.put(tupleName , value);
    }

    public static void writePlayerGameState(JSONObject jsonObject , double playerGameState) {
        jsonObject.put("playerGameState" , playerGameState);
    }

    public static void writeEEG(JSONObject jsonObject , double EEG) {
        jsonObject.put("EEG" , EEG);
    }

    public static void writeConcentration(JSONObject jsonObject , double concentration) {
        jsonObject.put("concentration" , concentration);
    }

    public static void writeSensor(JSONObject jsonObject , double sensor) {
        jsonObject.put("sensor" , sensor);
    }

    public static void writeGlobalGameState(JSONObject jsonObject , double globalGameState) {
        jsonObject.put("globalGameState" , globalGameState);
    }

    public static void writeCost(JSONObject jsonObject , double cost) {
        jsonObject.put("costInCloud" , cost);
    }

    public static void writeTotal(JSONObject jsonObject , double total) {
        jsonObject.put("totalNetworkUsage" , total);
    }

    public static void writeDevice(JSONArray jsonObject , String deviceName , double cost) {
        JSONObject object = new JSONObject();
        object.put("deviceName" , deviceName);
        object.put("cost" , cost);
        jsonObject.add(object);
    }
}







