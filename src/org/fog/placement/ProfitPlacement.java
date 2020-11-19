package org.fog.placement;

import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.application.selectivity.SelectivityModel;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.utils.Logger;

/**
 * @author Z_HAO 2020/4/6
 */

public class ProfitPlacement extends ModulePlacement{

    protected ModuleMapping moduleMapping;
    protected List<Sensor> sensors;
    protected List<Actuator> actuators;
    protected String moduleToPlace;
    protected Map<Integer, Integer> deviceMipsInfo;
    protected double time;
    protected List<Application> application;

    /**
     * Stores the current mapping of application modules to fog devices
     */
    protected Map<Integer, List<String>> currentModuleMap;
    protected Map<Integer, Map<String, Double>> currentModuleLoadMap;
    protected Map<Integer, Map<String, Integer>> currentModuleInstanceNum;

    protected int QoSMeetNumber;

    public ProfitPlacement(List<FogDevice> fogDevices, List<Sensor> sensors, List<Actuator> actuators,
                           List<Application> application, ModuleMapping moduleMapping, String moduleToPlace , double time) {
        this.setFogDevices(fogDevices);
        this.setApplication(application);
        this.setModuleMapping(moduleMapping);
        this.setModuleToDeviceMap(new HashMap<String, List<Integer>>());
        this.setDeviceToModuleMap(new HashMap<Integer, List<AppModule>>());
        setSensors(sensors);
        setActuators(actuators);
        this.moduleToPlace = moduleToPlace;
        this.deviceMipsInfo = new HashMap<Integer, Integer>();
        this.time = time;
        QoSMeetNumber = 0;
        mapModules();
    }


    @Override
    protected void mapModules() {
        for (FogDevice fogDevice: getFogDevices()) {
            fogDevice.delBuffer(time);
           // if(!fogDevice.isAllocated()) {
            if(true) {
                Application X = null;
                double T = Double.MAX_VALUE;
                double max = 0;
                for(Application application: getApplications()) {
                    if(!application.isPlaced()) {
                        double processTime = (double)application.getInstrNum() / (double)fogDevice.getRate();
                        double propagationTime = (double)application.getPackageSize() / (double)fogDevice.getBandWidth();
                        double grade = propagationTime / processTime;
                        double epTime = application.getArrivalTime() + application.getTimeLimit() - time; //能留下的时间
                        double totalTime = processTime + fogDevice.isCloud() * propagationTime;//运行时间
                        if(fogDevice.getBusyTime()+totalTime>application.getArrivalTime() + application.getTimeLimit()) continue;
                        if(fogDevice.getBusyTime()>application.getArrivalTime()&&application.getPackageSize()>fogDevice.getUnusedBufferSize()) continue;
                        double charge = totalTime * (fogDevice.getCostPerSecond() + (1 - fogDevice.isCloud()) * (grade * (fogDevice.getCostPerSecond() - fogDevice.getTransCost()) + 0.005));
                        double cost = fogDevice.getCalCost() * processTime + fogDevice.isCloud() * fogDevice.getTransCost() * propagationTime;
                        double profit = charge - cost;
                        double mrc = profit / epTime;
                        if(max < mrc && totalTime <= epTime) {
                            max = mrc;
                            X = application;
                            T = totalTime;
                        }
                    }
                }
                if(X != null) {
                    //System.out.println("Assign " + X.getAppId() + " to device: " + fogDevice.getName());
                    X.setPlaced(true);
                    fogDevice.setAllocated(true);
                    fogDevice.setBusyTime(T + time);
                    double alltime = T + time;
                    fogDevice.addBuffer(X.getPackageSize(), alltime);
                    QoSMeetNumber ++;
                }
            }
        }
        /*for(String deviceName: getModuleMapping().getModuleMapping().keySet()){
            for(String moduleName: getModuleMapping().getModuleMapping().get(deviceName)){
                int deviceId = CloudSim.getEntityId(deviceName);
                AppModule appModule = getApplication().getModuleByName(moduleName);
                if(!getDeviceToModuleMap().containsKey(deviceId)) {
                    List<AppModule>placedModules = new ArrayList<AppModule>();
                    placedModules.add(appModule);
                    getDeviceToModuleMap().put(deviceId, placedModules);
                    System.out.println("First place " + moduleName + " to " + deviceName);
                }
                else {
                    List<AppModule>placedModules = getDeviceToModuleMap().get(deviceId);
                    placedModules.add(appModule);
                    getDeviceToModuleMap().put(deviceId, placedModules);
                    System.out.println("First place " + moduleName + " to " + deviceName);
                }
            }
        }
        for(FogDevice device: getFogDevices()) {
            int deviceParent = -1;
            List<Integer>children = new ArrayList<Integer>();
            if(device.getLevel() == 1) {
                if(!deviceMipsInfo.containsKey(device.getId())) {
                    deviceMipsInfo.put(device.getId(), 0);
                }
                deviceParent = device.getParentId();
                for(FogDevice deviceChild:getFogDevices()) {
                    if(deviceChild.getParentId()==device.getId()) {
                        children.add(deviceChild.getId());
                    }
                }
                Map<Integer, Double>childDeadline = new HashMap<Integer, Double>();
                for(int childId:children) {
                    childDeadline.put(childId,getApplication().getDeadlineInfo().get(childId).get(moduleToPlace));
                }
                List<Integer> keys = new ArrayList<Integer>(childDeadline.keySet());
                for(int i = 0; i < keys.size() - 1; i ++) {
                    for(int j = 0;j < keys.size() - i - 1;j ++) {
                        if(childDeadline.get(keys.get(j)) > childDeadline.get(keys.get(j + 1))) {
                            int tempJ = keys.get(j);
                            int tempJn = keys.get(j + 1);
                            keys.set(j, tempJn);
                            keys.set(j + 1, tempJ);
                        }
                    }
                }
                int baseMipsOfPlacingModule = (int)getApplication().getModuleByName(moduleToPlace).getMips();
                for(int key:keys) {
                    int currentMips = deviceMipsInfo.get(device.getId());
                    AppModule appModule = getApplication().getModuleByName(moduleToPlace);
                    int additionalMips = getApplication().getAdditionalMipsInfo().get(key).get(moduleToPlace);
                    if(currentMips+baseMipsOfPlacingModule + additionalMips < device.getMips()) {
                        currentMips = currentMips + baseMipsOfPlacingModule + additionalMips;
                        deviceMipsInfo.put(device.getId(), currentMips);
                        if(!getDeviceToModuleMap().containsKey(device.getId())) {
                            List<AppModule>placedModules = new ArrayList<AppModule>();
                            placedModules.add(appModule);
                            getDeviceToModuleMap().put(device.getId(), placedModules);
                            System.out.println("Second place " + appModule.getName() + " to " + device.getName());
                        }
                        else {
                            List<AppModule>placedModules = getDeviceToModuleMap().get(device.getId());
                            placedModules.add(appModule);
                            getDeviceToModuleMap().put(device.getId(), placedModules);
                            System.out.println("Second place " + appModule.getName() + " to " + device.getName());
                        }
                    }
                    else {
                        List<AppModule>placedModules = getDeviceToModuleMap().get(deviceParent);
                        placedModules.add(appModule);
                        getDeviceToModuleMap().put(deviceParent, placedModules);
                        System.out.println("Last place " + appModule.getName() + " to cloud");
                    }
                }
            }
        }*/
    }


    public ModuleMapping getModuleMapping() {
        return moduleMapping;
    }

    public void setModuleMapping(ModuleMapping moduleMapping) {
        this.moduleMapping = moduleMapping;
    }

    public List<Sensor> getSensors() {
        return sensors;
    }

    public void setSensors(List<Sensor> sensors) {
        this.sensors = sensors;
    }

    public List<Actuator> getActuators() {
        return actuators;
    }

    public void setActuators(List<Actuator> actuators) {
        this.actuators = actuators;
    }

    public void setApplication(List<Application> application) {
        this.application = application;
    }

    public List<Application> getApplications() {
        return this.application;
    }

    public int getQoSMeetNumber() {
        return QoSMeetNumber;
    }
}












