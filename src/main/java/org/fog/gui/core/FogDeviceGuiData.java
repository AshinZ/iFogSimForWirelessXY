/*
    auther:  AshinZ
    date:    2020/9/21
 */

// 模拟gui输入的数据 将其作为一个data数据留存  主要用于gui的文件功能 (fogdevice)
package org.fog.gui.core;

import org.json.simple.JSONObject;

import java.awt.dnd.DragSourceMotionListener;

public class FogDeviceGuiData {
    private String nodeName;
    private long   mips;
    private int ram;
    private long upBw;
    private long downBw;
    private int level;
    private double ratePerMips;
    private double busyPower;
    private double idlePower;

    public FogDeviceGuiData(){

    }

    public FogDeviceGuiData(String nodeName, long mips,	int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower){
        this.nodeName = nodeName;
        this.mips = mips;
        this.ram  = ram;
        this.upBw = upBw;
        this.downBw = downBw;
        this.level = level;
        this.ratePerMips = ratePerMips;
        this.busyPower = busyPower;
        this.idlePower = idlePower;
    }

    public void setNodeName(String nodeName){
        this.nodeName = nodeName;
    }

    public String getNodeName(){
        return this.nodeName;
    }

    public void setMips(long mips){
        this.mips = mips;
    }

    public long getMips(){
        return this.mips;
    }

    public void setRam(int ram){
        this.ram = ram;
    }

    public int getRam(){
        return this.ram;
    }

    public void setUpBw(long upBw){
        this.upBw = upBw;
    }

    public long getUpBw(){
        return this.upBw;
    }

    public void setDownBw(long downBw){
        this.downBw = downBw;
    }

    public long getDownBw(){
        return this.downBw;
    }

    public void setLevel(int level){
        this.level = level;
    }

    public int getLevel(){
        return this.level;
    }

    public void setRatePerMips(double ratePerMips){
        this.ratePerMips = ratePerMips;
    }

    public double getRatePerMips(){
        return this.ratePerMips;
    }

    public void setBusyPower(double busyPower){
        this.busyPower = busyPower;
    }

    public double getBusyPower() {
        return this.busyPower;
    }

    public void setIdlePower(double idlePower){
        this.idlePower = idlePower;
    }

    public double getIdlePower() {
        return this.idlePower;
    }

    public void showInfo(){
        System.out.print(this.nodeName);
    }

    public JSONObject toJson(){
        JSONObject fogDeviceJson  = new JSONObject();
        fogDeviceJson.put("nodeName",getNodeName());
        fogDeviceJson.put("mips",getMips());
        fogDeviceJson.put("ram",getRam());
        fogDeviceJson.put("upBw",getUpBw());
        fogDeviceJson.put("downBw",getDownBw());
        fogDeviceJson.put("level",getLevel());
        fogDeviceJson.put("ratePerMips",getRatePerMips());
        fogDeviceJson.put("busyPower",getBusyPower());
        fogDeviceJson.put("idlePower",getIdlePower());
        return fogDeviceJson;
    }
}
