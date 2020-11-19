package org.service;

import javafx.util.Pair;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;

import java.util.ArrayList;
import java.util.HashMap;


public class FogData {
    private ArrayList<FogDevice> fogDevices;
    private ArrayList<Sensor> sensors;
    private ArrayList<Actuator> actuators;
    private HashMap<Pair<String, String> , Double> links;

    private HashMap<String , FogDevice> fogDeviceMap;
    private HashMap<String , Sensor> sensorMap;
    private HashMap<String , Actuator> actuatorMap;
    private HashMap<String , String> typeMap;

    private FogBroker fogBroker;

    private String appId = "test";
    private String brokerName = "broker";
    private double EEG_TRANSMISSION_TIME = 5.1;
    private boolean CLOUD = false;

    public FogData() {
        fogDevices = new ArrayList<>();
        sensors = new ArrayList<>();
        actuators = new ArrayList<>();
        links = new HashMap<>();
        fogDeviceMap = new HashMap<>();
        sensorMap = new HashMap<>();
        actuatorMap = new HashMap<>();
        typeMap = new HashMap<>();
        try {
            fogBroker = new FogBroker("brokerName");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ArrayList<FogDevice> getFogDevices() {
        return fogDevices;
    }

    public void setFogDevices(ArrayList<FogDevice> fogDevices) {
        this.fogDevices = fogDevices;
    }

    public ArrayList<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(ArrayList<Sensor> sensors) {
        this.sensors = sensors;
    }

    public ArrayList<Actuator> getActuators() {
        return actuators;
    }

    public void setActuators(ArrayList<Actuator> actuators) {
        this.actuators = actuators;
    }

    public HashMap<Pair<String, String>, Double> getLinks() {
        return links;
    }

    public void setLinks(HashMap<Pair<String, String>, Double> links) {
        this.links = links;
    }

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public FogDevice getFogDeviceByName(String name) {
        return fogDeviceMap.get(name);
    }

    public Sensor getSensorByName(String name) {
        return sensorMap.get(name);
    }

    public Actuator getActuatorByName(String name) {
        return actuatorMap.get(name);
    }

    public HashMap<String, FogDevice> getFogDeviceMap() {
        return fogDeviceMap;
    }

    public void setFogDeviceMap(HashMap<String, FogDevice> fogDeviceMap) {
        this.fogDeviceMap = fogDeviceMap;
    }

    public HashMap<String, Sensor> getSensorMap() {
        return sensorMap;
    }

    public void setSensorMap(HashMap<String, Sensor> sensorMap) {
        this.sensorMap = sensorMap;
    }

    public HashMap<String, Actuator> getActuatorMap() {
        return actuatorMap;
    }

    public void setActuatorMap(HashMap<String, Actuator> actuatorMap) {
        this.actuatorMap = actuatorMap;
    }

    public HashMap<String, String> getTypeMap() {
        return typeMap;
    }

    public void setTypeMap(HashMap<String, String> typeMap) {
        this.typeMap = typeMap;
    }

    public FogBroker getFogBroker() {
        return fogBroker;
    }

    public void setFogBroker(FogBroker fogBroker) {
        this.fogBroker = fogBroker;
    }

    public double getEEG_TRANSMISSION_TIME() {
        return EEG_TRANSMISSION_TIME;
    }

    public void setEEG_TRANSMISSION_TIME(double EEG_TRANSMISSION_TIME) {
        this.EEG_TRANSMISSION_TIME = EEG_TRANSMISSION_TIME;
    }

    public String getBrokerName() {
        return brokerName;
    }

    public void setBrokerName(String brokerName) {
        this.brokerName = brokerName;
    }

    public boolean isCLOUD() {
        return CLOUD;
    }

    public void setCLOUD(boolean CLOUD) {
        this.CLOUD = CLOUD;
    }
}