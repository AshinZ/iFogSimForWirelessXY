/*
    author : AshinZ
    date 9.22
  存储模拟link输入时的数据
*/
package org.fog.gui.core;

public class LinkGuiData {
    private String startNodeName;
    private String targetNodeName;
    private double latency;

    public LinkGuiData(){

    }

    public LinkGuiData(String startNodeName,String targetNodeName,double latency){
        this.startNodeName = startNodeName;
        this.targetNodeName = targetNodeName;
        this.latency = latency;
    }

    public double getLatency() {
        return latency;
    }

    public String getStartNodeName() {
        return startNodeName;
    }

    public String getTargetNodeName() {
        return targetNodeName;
    }

    public void setLatency(double latency) {
        this.latency = latency;
    }

    public void setStartNodeName(String startNodeName) {
        this.startNodeName = startNodeName;
    }

    public void setTargetNodeName(String targetNodeName) {
        this.targetNodeName = targetNodeName;
    }
}
