/*
    author : AshinZ
    date 9.22
  存储模拟sensor输入时的数据
*/
package org.fog.gui.core;

import org.json.simple.JSONObject;

public class SensorGuiData {
    private String name ;  //传感器名字
    private String sensorType ;  //传感器类型
    private String distributionType;  //分布方式
    private double mean;  //
    private double stdDev; //
    private double min;  //
    private double max;  //
    private double deterministicValue; //

    public SensorGuiData(){

    }

    public SensorGuiData(String name, String sensorType, String distributionType, double mean,
                         double stdDev, double min, double max,
                         double deterministicValue){
        this.name = name;
        this.sensorType = sensorType;
        this.distributionType = distributionType;
        this.mean = mean;
        this.stdDev = stdDev;
        this.min = min;
        this.max = max;
        this.deterministicValue = deterministicValue;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setSensorType(String sensorType) {
        this.sensorType = sensorType;
    }

    public void setDistributionType(String distributionType) {
        this.distributionType = distributionType;
    }

    public void setMean(double mean) {
        this.mean = mean;
    }

    public void setMin(double min) {
        this.min = min;
    }

    public void setMax(double max) {
        this.max = max;
    }

    public void setStdDev(double stdDev) {
        this.stdDev = stdDev;
    }

    public void setDeterministicValue(double deterministicValue) {
        this.deterministicValue = deterministicValue;
    }

    public double getDeterministicValue() {
        return deterministicValue;
    }

    public String getName() {
        return name;
    }

    public String getSensorType() {
        return sensorType;
    }

    public String getDistributionType() {
        return distributionType;
    }

    public double getStdDev() {
        return stdDev;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getMean() {
        return mean;
    }

    public JSONObject toJson(){
        JSONObject sensorJson = new JSONObject();
        sensorJson.put("name",getName());
        sensorJson.put("sensorType",getSensorType());
        sensorJson.put("distributionType",getDistributionType());
        sensorJson.put("mean",getMean());
        sensorJson.put("stdDev",getStdDev());
        sensorJson.put("min",getMin());
        sensorJson.put("max",getMax());
        sensorJson.put("deterministicValue",getDeterministicValue());
        return sensorJson;
    }
}
