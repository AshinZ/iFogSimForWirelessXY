package org.fog.placement;

import java.util.*;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.Application;
import org.fog.entities.*;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;

/**
 * @author Z_HAO 2020/4/14
 */
public class TestApplication {
    static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
    static Map<Integer,FogDevice> deviceById = new HashMap<Integer,FogDevice>();
    static List<Sensor> sensors = new ArrayList<Sensor>();
    static List<Actuator> actuators = new ArrayList<Actuator>();
    static List<Integer> idOfEndDevices = new ArrayList<Integer>();
    static Map<Integer, Map<String, Double>> deadlineInfo = new HashMap<Integer, Map<String, Double>>();
    static Map<Integer, Map<String, Integer>> additionalMipsInfo = new HashMap<Integer, Map<String, Integer>>();
    static boolean CLOUD = false;
    static int numOfGateways = 1;
    static int numOfEndDevPerGateway = 4;
    static double sensingInterval = 0.1;

    static List<Application> remainedApplication = new ArrayList<>();

    public static void refresh(double time) {
        for(FogDevice fogDevice: fogDevices) {
            if(fogDevice.isAllocated() && time >= fogDevice.getBusyTime()) {
                fogDevice.setAllocated(false);
                //System.out.println(fogDevice.getName() + " is free");
            }
        }
        remainedApplication.removeIf(Application::isPlaced);
    }

    public static void main(String[] args) {
        Log.printLine("Starting TestApplication...");
        int totalNUmber = 20000;
        try {
            createFogDevices(50 , 80);
            int sum = 0;
            int satisfiedNUmber = 0;
            Random random = new Random();
            for(double time = 0;time < 100;) {
                refresh(time);
                int num = random.nextInt(40) + 10;
                createApplications(sum + num , sum , time , totalNUmber);
                if(sum + num < totalNUmber) {
                    sum += num;
                }
                else {
                    sum = totalNUmber;
                }
                ProfitPlacement placement = new ProfitPlacement(fogDevices , sensors , actuators , remainedApplication , null , "" , time);
                satisfiedNUmber += placement.getQoSMeetNumber();
                time += (double)getPossionVariable(11) / 100;
            }
            System.out.println("Application number: " + sum);
            System.out.println("QoS satisfied number: " + satisfiedNUmber);
        } catch (Exception e) {
            Log.printLine("Unwanted errors happen");
            e.printStackTrace();
        }
    }

    public static void createFogDevices(int number , int percent) throws Exception {
        Random random = new Random();
        int i = 0;
        int fogNumber = number * percent / 100;
        for(;i < fogNumber;i ++) {
            int bandWidth = random.nextInt(300) + 700;
            int rate = random.nextInt(100) + 120;
            double costPerSecond = random.nextDouble() * 0.035 + 0.025;
            double calCost = random.nextDouble() * 0.005 + 0.005;
            double transCost = random.nextDouble() * 0.005 + 0.002;
            createFogDevice("Fog_ins#" + i , bandWidth , rate , costPerSecond , calCost , transCost , 0);
        }
        for(;i < number;i ++) {
            int bandWidth = random.nextInt(300) + 700;
            int rate = random.nextInt(100) + 120;
            double costPerSecond = random.nextDouble() * 0.035 + 0.025;
            double calCost = random.nextDouble() * 0.005 + 0.005;
            double transCost = random.nextDouble() * 0.005 + 0.002;
            createFogDevice("Cloud_ins#" + i , bandWidth , rate , costPerSecond , calCost , transCost , 1);
        }
    }

    public static void createApplications(int sum , int number , double time , int totalNumber) {
        Random random = new Random();
        for(int i = number;i < totalNumber && i < sum;i ++) {
            int packageSize = random.nextInt(60) + 80;
            int instrNUm = random.nextInt(10) + 20;
            double price = random.nextDouble() * 0.2 + 0.8;
            double timeLimit = random.nextDouble() * 0.2 + 0.5;
            remainedApplication.add(new Application("app#" + i , packageSize , instrNUm , price , timeLimit , time));
        }
    }

    private static void createFogDevice(String name, int bandWidth, int rate, double costPerSecond, double calCost, double transCost,
                                        int cloud) throws Exception {
        List<Pe> peList = new ArrayList<>();
        //Pe: Processing Element 处理元件，单位百万
        //对应MIPS，百万条指令每秒
        peList.add(new Pe(0 , new PeProvisionerOverbooking(100)));

        int hostId = FogUtils.generateEntityId();
        //这个类就是定义了一堆静态变量以供使用
        long storage = 1000000;
        int bw = 10000;

        PowerHost host = new PowerHost(hostId , new RamProvisionerSimple(100) , new BwProvisionerOverbooking(bw) , storage , peList , new StreamOperatorScheduler(peList) , new FogLinearPowerModel(100.0 , 100.0));
        List<Host> hostList = new ArrayList<>();//host 主机
        hostList.add(host);

        //以下偏向模拟的特性
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 8.0; // time zone this resource located
        double cost = 3.0; // the cost of using processing in this resource
        double costPerMem = 0.05; // the cost of using memory in this resource
        double costPerStorage = 0.001; // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch , os , vmm , host , time_zone , cost , costPerMem , costPerStorage , costPerBw);

        //以下为正式创建Fog device
        FogDevice fogDevice = null;
        try {
            int schedulingInterval = 10;
            fogDevice = new FogDevice(name , characteristics , new AppModuleAllocationPolicy(hostList) , storageList , schedulingInterval , 100.0 , 100.0 , 0 , 100);
            //scheduling interval = 10
            //up link latency = 0
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assert fogDevice != null;
        fogDevice.setLevel(0);
        fogDevice.setFogDevice(name , bandWidth , rate , costPerSecond , calCost , transCost , false, cloud);
        fogDevice.setFogDeviceBuffer(180); //设定buffer大小
        fogDevices.add(fogDevice);
    }

    private static int getPossionVariable(double lamda) {
        int x = 0;
        double y = Math.random(), cdf = getPossionProbability(x, lamda);
        while (cdf < y) {
            x++;
            cdf += getPossionProbability(x, lamda);
        }
        return x;
    }

    private static double getPossionProbability(int k, double lamda) {
        double c = Math.exp(-lamda), sum = 1;
        for (int i = 1; i <= k; i++) {
            sum *= lamda / i;
        }
        return sum * c;
    }
}





