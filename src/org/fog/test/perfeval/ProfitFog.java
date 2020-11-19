package org.fog.test.perfeval;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.MyFog.MyRandom;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.entities.Tuple;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ProfitPlacement;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;

/**
 * @author Z_HAO 2020/3/26
 */
public class ProfitFog {
    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();

    static int numOfDepth = 1;
    static int numOFMobiles = 4;

    private static FogDevice createFogDevice(String nodeName , long mips , int ram , long upBw , long downBw , int level , double ratePerMips , double busyPower , double idlePower) {
        List<Pe> peList = new ArrayList<>();
        //Pe: Processing Element 处理元件，单位百万
        //对应MIPS，百万条指令每秒
        peList.add(new Pe(0 , new PeProvisionerOverbooking(mips)));

        int hostId = FogUtils.generateEntityId();
        //这个类就是定义了一堆静态变量以供使用
        long storage = 1000000;
        int bw = 10000;

        PowerHost host = new PowerHost(hostId , new RamProvisionerSimple(ram) , new BwProvisionerOverbooking(bw) , storage , peList , new StreamOperatorScheduler(peList) , new FogLinearPowerModel(busyPower , idlePower));
        List<Host> hostList = new ArrayList<>();//host 主机
        hostList.add(host);

        //以下偏向模拟的特性
        String arch = "x86"; // system architecture
        String os = "Linux"; // operating system
        String vmm = "Xen";
        double time_zone = 8.0; // time zone this resource located
        double cost = new MyRandom(0.1 , 2.7 , 4.5).getDoubleRandom(); // the cost of using processing in this resource
        double costPerMem = new MyRandom(0.1 , 0.03 , 1.0).getDoubleRandom(); // the cost of using memory in this resource
        double costPerStorage = new MyRandom(0.1 , 0.0008 , 0.003).getDoubleRandom(); // the cost of using storage in this resource
        double costPerBw = 0.0; // the cost of using bw in this resource
        LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN devices by now
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch , os , vmm , host , time_zone , cost , costPerMem , costPerStorage , costPerBw);

        //以下为正式创建Fog device
        FogDevice fogDevice = null;
        try {
            int schedulingInterval = 10;
            fogDevice = new FogDevice(nodeName , characteristics , new AppModuleAllocationPolicy(hostList) , storageList , schedulingInterval , upBw , downBw , 0 , ratePerMips);
            //scheduling interval = 10
            //up link latency = 0
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        assert fogDevice != null;
        fogDevice.setLevel(level);
        return fogDevice;
    }

    private static FogDevice addMobile(String id , int userId , String appId , int parentId) {
        FogDevice mobile = createFogDevice("mobile-" + id , 0 , 1000 , 10000 , 270 , 5 , 0 , 87.53 , 82.44);
        mobile.setParentId(parentId);
        return mobile;
    }

    private static void addGw(String id , int userId , String appId , int parentId) {
        //Gw gateway
        FogDevice dept = createFogDevice("gateway" + id, 0 , 4000 , 10000 , 10000 , 4 , 0.0 , 107.339 , 83.4333);
        //dept 应该就是gateway
        fogDevices.add(dept);
        dept.setParentId(parentId);
        dept.setUplinkLatency(0);
        for(int i = 0;i < numOFMobiles;i ++) {
            String iD = id + "-" + i;
            FogDevice mobile = addMobile(iD , userId , appId , parentId);
            mobile.setUplinkLatency(0);
            fogDevices.add(mobile);
        }
    }

    private static void createFogDevices(int userId , String appId) throws Exception {
        FogDevice cloud = createFogDevice("cloud", 193 , 40000 , 845 , 845 , 0 , 0.01 , 38.6 , 38.6);
        cloud.setParentId(-1);//云属于最上层
        FogDevice proxy = createFogDevice("proxy-server", 162, 4000, 820, 820, 0, 0.0, 36, 36);
        //云代理
        proxy.setParentId(cloud.getId());
        proxy.setUplinkLatency(0);//单位ms
        fogDevices.add(cloud);
        fogDevices.add(proxy);

        FogDevice instance1 = createFogDevice("Instance1" , 190 , 1000 , 840 , 840 , 1 , 0.0 , 38 , 38);
        instance1.setUplinkLatency(0);
        instance1.setParentId(proxy.getId());
        fogDevices.add(instance1);
        FogDevice instance2 = createFogDevice("Instance2" , 167 , 1000 , 824 , 824 , 1 , 0.0 , 36.4 , 36.4);
        instance2.setUplinkLatency(0);
        instance2.setParentId(instance1.getId());
        fogDevices.add(instance2);

        for(int i = 0;i < numOfDepth;i ++) {
            addGw(String.valueOf(i) , userId , appId , instance2.getId());
        }
    }

    private static Application createApplication(String appId, int userId){
        Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)=
        /*
         * Adding modules (vertices) to the application model (directed graph)
         */
        application.addAppModule("client", 10); // adding module Client to the application model
        application.addAppModule("concentration_calculator", 10); // adding module Concentration Calculator to the application model
        application.addAppModule("connector", 10); // adding module Connector to the application model

        final AppLoop loop1 = new AppLoop(new ArrayList<String>(){{add("EEG");add("client");add("concentration_calculator");add("client");add("DISPLAY");}});
        List<AppLoop> loops = new ArrayList<AppLoop>(){{add(loop1);}};
        application.setLoops(loops);
        return application;
    }

    public static void main(String []args) {
        Log.disable();
        try {
            int numUser = 1;
            Calendar calendar = Calendar.getInstance();//程序运行开始时间
            //boolean traceFlag = false;
            CloudSim.init(numUser , calendar , false);
            String APPId = "Profit Placement";
            FogBroker broker = new FogBroker("Broker");//FogBroker本身没有用，用的是它已经写好的父类
            //感觉broker的存在就像用户的终端，相当于用户
            Application application = createApplication(APPId , broker.getId());//id初始化为-1
            application.setUserId(broker.getId());
            createFogDevices(broker.getId() , APPId);
            //至此，结构中的所有基本设备的创建完成
            //接下来为抽象模块的创建
            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping();//模块映射
            moduleMapping.addModuleToDevice("connector" , "cloud");
            Controller controller = new Controller("master-controller" , fogDevices , sensors , actuators);
            //唯一指定方式
            controller.submitApplication(application , 0 , new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping));
            //同唯一

            //开始运行
            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());
            CloudSim.startSimulation();
            CloudSim.stopSimulation();
        }catch (Exception e) {
            e.printStackTrace();
        }
        Log.printLine("Profit Placement finished!");
    }
}














