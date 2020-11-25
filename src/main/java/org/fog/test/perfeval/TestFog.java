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
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.TimeKeeper;
import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.MyFog.MyRandom;

/**
 * Created by Z_HAO on 2020/2/9
 */
public class TestFog {
    static List<FogDevice> fogDevices = new ArrayList<>();
    static List<Sensor> sensors = new ArrayList<>();
    static List<Actuator> actuators = new ArrayList<>();

    static int numOfDepth = 4;
    static int numOFMobiles = 6;
    static double EEG_TRANSMISSION_TIME = 5.1;

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
            int schedulingInterval = new MyRandom(0.1).getIntRandom();
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
        FogDevice mobile = createFogDevice("mobile-" + id , 1000 , 1000 , 10000 , 270 , 3 , 0 , 87.53 , 82.44);
        mobile.setParentId(parentId);
        Sensor sensor = new Sensor("sensor-" + id , "EEG" , userId , appId , new DeterministicDistribution(EEG_TRANSMISSION_TIME));
        sensor.setLatency(new MyRandom(0.1 , 5.7 , 7.0).getDoubleRandom());
        sensor.setGatewayDeviceId(mobile.getId());
        sensors.add(sensor);
        Actuator actuator = new Actuator("actuator-" + id , userId , appId , "DISPLAY");
        actuator.setLatency(new MyRandom(0.1 , 0.08 , 2.0).getDoubleRandom());
        actuator.setGatewayDeviceId(mobile.getId());
        actuators.add(actuator);
        return mobile;
    }

    private static void addGw(String id , int userId , String appId , int parentId) {
        //Gw gateway
        FogDevice dept = createFogDevice("depth-" + id, 2800 , 4000 , 10000 , 10000 , 1 , 0.0 , 107.339 , 83.4333);
        fogDevices.add(dept);
        dept.setParentId(parentId);
        dept.setUplinkLatency(new MyRandom(0.1 , 3.8 , 5.0).getIntRandom());
        for(int i = 0;i < numOFMobiles;i ++) {
            String iD = id + "-" + i;
            FogDevice mobile = addMobile(iD , userId , appId , parentId);
            mobile.setUplinkLatency(new MyRandom(0.1 , 0.18 , 3.0).getIntRandom());
            fogDevices.add(mobile);
        }
    }

    private static void createFogDevices(int userId , String appId) throws Exception {
        FogDevice cloud = createFogDevice("cloud", 44800 , 40000 , 100 , 10000 , 0 , 0.01 , 16*103 , 16*83.25);
        cloud.setParentId(-1);//云属于最上层
        FogDevice proxy = createFogDevice("proxy-server", 2800, 4000, 10000, 10000, 1, 0.0, 107.339, 83.4333);
        //云代理
        proxy.setParentId(cloud.getId());
        proxy.setUplinkLatency(new MyRandom(0.1 , 80 , 150).getIntRandom());//单位ms
        fogDevices.add(cloud);
        fogDevices.add(proxy);
        for(int i = 0;i < numOfDepth;i ++) {
            addGw(String.valueOf(i) , userId , appId , proxy.getId());
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
        /*
         * Connecting the application modules (vertices) in the application model (directed graph) with edges
         */
        if(EEG_TRANSMISSION_TIME==10)
            application.addAppEdge("EEG", "client", 2000, 500, "EEG", Tuple.UP, AppEdge.SENSOR); // adding edge from EEG (sensor) to Client module carrying tuples of type EEG
        else
            application.addAppEdge("EEG", "client", 3000, 500, "EEG", Tuple.UP, AppEdge.SENSOR);
        application.addAppEdge("client", "concentration_calculator", 3500, 500, "_SENSOR", Tuple.UP, AppEdge.MODULE); // adding edge from Client to Concentration Calculator module carrying tuples of type _SENSOR
        application.addAppEdge("concentration_calculator", "connector", 100, 1000, 1000, "PLAYER_GAME_STATE", Tuple.UP, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Concentration Calculator to Connector module carrying tuples of type PLAYER_GAME_STATE
        application.addAppEdge("concentration_calculator", "client", 14, 500, "CONCENTRATION", Tuple.DOWN, AppEdge.MODULE);  // adding edge from Concentration Calculator to Client module carrying tuples of type CONCENTRATION
        application.addAppEdge("connector", "client", 100, 28, 1000, "GLOBAL_GAME_STATE", Tuple.DOWN, AppEdge.MODULE); // adding periodic edge (period=1000ms) from Connector to Client module carrying tuples of type GLOBAL_GAME_STATE
        application.addAppEdge("client", "DISPLAY", 1000, 500, "SELF_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type SELF_STATE_UPDATE
        application.addAppEdge("client", "DISPLAY", 1000, 500, "GLOBAL_STATE_UPDATE", Tuple.DOWN, AppEdge.ACTUATOR);  // adding edge from Client module to Display (actuator) carrying tuples of type GLOBAL_STATE_UPDATE
        /*
         * Defining the input-output relationships (represented by selectivity) of the application modules.
         */
        application.addTupleMapping("client", "EEG", "_SENSOR", new FractionalSelectivity(0.9)); // 0.9 tuples of type _SENSOR are emitted by Client module per incoming tuple of type EEG
        application.addTupleMapping("client", "CONCENTRATION", "SELF_STATE_UPDATE", new FractionalSelectivity(1.0)); // 1.0 tuples of type SELF_STATE_UPDATE are emitted by Client module per incoming tuple of type CONCENTRATION
        application.addTupleMapping("concentration_calculator", "_SENSOR", "CONCENTRATION", new FractionalSelectivity(1.0)); // 1.0 tuples of type CONCENTRATION are emitted by Concentration Calculator module per incoming tuple of type _SENSOR
        application.addTupleMapping("client", "GLOBAL_GAME_STATE", "GLOBAL_STATE_UPDATE", new FractionalSelectivity(1.0)); // 1.0 tuples of type GLOBAL_STATE_UPDATE are emitted by Client module per incoming tuple of type GLOBAL_GAME_STATE
        /*
         * Defining application loops to monitor the latency of.
         * Here, we add only one loop for monitoring : EEG(sensor) -> Client -> Concentration Calculator -> Client -> DISPLAY (actuator)
         */
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
            String APPId = "VRGame";
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
        Log.printLine("VRGame finished!");
    }
}

















