/*
    author : AshinZ
    date 9.22
  存储模拟actuator输入时的数据
*/
package org.fog.gui.core;

public class ActuatorGuiData {
    private String name;
    //设计到数据划分的问题
  //  private int    appId;
  //  private String userId;
    private  String actuatorType;

    public ActuatorGuiData(){

    }

    public ActuatorGuiData(String name,String actuatorType){
        this.name = name;
        this.actuatorType = actuatorType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getActuatorType() {
        return actuatorType;
    }

    public void setActuatorType(String actuatorType) {
        this.actuatorType = actuatorType;
    }
}
