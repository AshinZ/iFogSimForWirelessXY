package org.fog.entities;

import java.util.*;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.*;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.*;

/**
 * Created by Z_HAO on 2020/2/12
 */
public class FogDevice extends PowerDatacenter {
    private int mips;

    protected Queue<Tuple> northTupleQueue;
    protected Queue<Pair<Tuple , Integer>> southTupleQueue;
    protected List<Integer> childrenIds;

    protected List<String> activeApplications;
    protected Map<String , Application> applicationMap;
    protected Map<String , List<String>> appToModulesMap;

    protected Map<Integer , Double> childToLatencyMap;
    protected Map<Integer , Integer> cloudTrafficMap;
    protected Map<Integer , List<String>> childToOperatorsMap;

    protected double lockTime;
    protected int parentId;
    protected int controllerId;

    protected boolean isSouthLinkBusy;
    protected boolean isNorthLinkBusy;

    protected double upLinkBandwidth;
    protected double downLinkBandwidth;
    protected double upLinkLatency;
    protected List<Pair<Integer , Double>> associatedActuatorIds;//First: actuatorId Second: delay

    protected double energyConsumption;
    protected double lastUtilizationUpdateTime;
    protected double lastUtilization;

    private int level;
    protected double ratePerMips;
    protected double totalCost;
    protected Map<String , Map<String , Integer>> moduleInstanceCount;

    protected int bandWidth;
    protected int rate;
    protected double costPerSecond;
    protected double calCost;
    protected double transCost;
    protected boolean allocated;
    protected int cloud;
    protected double busyTime;
    protected double maxBufferSize; //最大buffer
    protected double usedBufferSize; //已使用buffer
    protected Queue<Pair<Double,Double>> buffer; //存储传来的buffer的队列
    protected PowerHost host;
    protected double idlePower;//用来获取数据进行文件存储
    protected double busyPower;//

    public void setFogDevice(String name , int bandWidth , int rate , double costPerSecond , double calCost , double transCost ,
                     boolean allocated , int cloud ) throws Exception {
        this.bandWidth = bandWidth;
        this.rate = rate;
        this.costPerSecond = costPerSecond;
        this.calCost = calCost;
        this.transCost = transCost;
        this.allocated = allocated;
        this.cloud = cloud;
        busyTime = 0.0;
    }

    public void setFogDeviceBuffer(double bufferSize) {
        this.maxBufferSize = bufferSize;
        this.usedBufferSize = 0.0;
        this.buffer = new LinkedList<>();
    }

    public double getUnusedBufferSize(){ //得到剩下的buffer空间
        return this.maxBufferSize-this.usedBufferSize;
    }
    public void addBuffer(double bufferSize,double time) {
        this.usedBufferSize = this.usedBufferSize + bufferSize; //改变已使用的buffer值
        Pair <Double,Double> bufferWork = new Pair <> (bufferSize,time);
        this.buffer.add(bufferWork); //将传入的buffer大小和该任务的结束时间放入队列
    }

    public void delBuffer(double time) {
        if(!this.buffer.isEmpty()) {
            for (Pair<Double, Double> q : this.buffer) {  //从前往后遍历
                if (q.getValue() < time) {//已经结束了该缓存的任务
                    this.usedBufferSize = this.usedBufferSize - q.getKey();
                    this.buffer.remove(q);
                } else
                    break;
            }
        }
    }


    /**
     * Instantiates a new datacenter.
     *
     * @param name               the name
     * @param characteristics    the res config
     * @param vmAllocationPolicy the vm provisioner
     * @param storageList        the storage list
     * @param schedulingInterval the scheduling interval
     * @throws Exception the exception
     */
    public FogDevice(String name , FogDeviceCharacteristics characteristics , VmAllocationPolicy vmAllocationPolicy , List<Storage> storageList ,
                     double schedulingInterval , double upLinkBandwidth , double downLinkBandwidth , double upLinkLatency , double ratePerMips) throws Exception {
        super(name , characteristics , vmAllocationPolicy , storageList , schedulingInterval);
        /*setCharacteristics(characteristics);
        setVmAllocationPolicy(vmAllocationPolicy);
        setLastProcessTime(0.0);
        setStorageList(storageList);
        setVmList(new ArrayList<Vm>());
        setSchedulingInterval(schedulingInterval);*/
        //在super()中已经存在，不明白为什么要再设置一遍
        this.host = characteristics.getHost();
        setUpLinkBandwidth(upLinkBandwidth);
        setDownLinkBandwidth(downLinkBandwidth);
        setUpLinkLatency(upLinkLatency);
        setRatePerMips(ratePerMips);
        setAssociatedActuatorIds(new ArrayList<>());
        for(Host host: characteristics.getHostList()) {
            host.setDatacenter(this);
        }
        setActiveApplications(new ArrayList<>());
        if(getCharacteristics().getNumberOfPes() == 0) {
            throw new Exception(this.getName() + "has no Entity! ");
        }
        getCharacteristics().setId(super.getId());
        applicationMap = new HashMap<>();
        appToModulesMap = new HashMap<>();
        northTupleQueue = new LinkedList<>();
        southTupleQueue = new LinkedList<>();
        setNorthLinkBusy(false);
        setSouthLinkBusy(false);
        setChildrenIds(new ArrayList<>());
        setChildToOperatorsMap(new HashMap<>());
        cloudTrafficMap = new HashMap<>();
        lockTime = 0;
        energyConsumption = 0;
        lastUtilization = 0;
        setTotalCost(0);
        setModuleInstanceCount(new HashMap<>());
        setChildToLatencyMap(new HashMap<>());
    }

    public FogDevice(String name , long mips , int ram , double upLinkBandwidth , double downLinkBandwidth ,
                     double ratePerMips , PowerModel powerModel) throws Exception {
        super(name , null , null , new LinkedList<>() , 0);
        List<Pe> peList = new ArrayList<>();
        peList.add(new Pe(0 , new PeProvisionerOverbooking(mips)));
        int hostId = FogUtils.generateEntityId();
        //这个类就是定义了一堆静态变量以供使用
        long storage = 1000000;
        int bw = 10000;
        PowerHost host = new PowerHost(hostId , new RamProvisionerSimple(ram) , new BwProvisionerOverbooking(bw) , storage , peList , new StreamOperatorScheduler(peList) , powerModel);
        //PowerHost host = new PowerHost(hostId , new RamProvisionerSimple(ram) , new BwProvisionerOverbooking(bw) , storage , peList , new StreamOperatorScheduler(peList) , new FogLinearPowerModel(busyPower , idlePower));
        //二者对比，仅最后一个参数不同
        //PowerModel是一个接口，FogLinearPowerModel是一个实例
        List<Host> hostList = new ArrayList<>();//host 主机
        hostList.add(host);
        //以上和CreateFogDevice相同

        setVmAllocationPolicy(new AppModuleAllocationPolicy(hostList));
        //以下模拟特性和原来的相同
        String arch = Config.FOG_DEVICE_ARCH;
        String os = Config.FOG_DEVICE_OS;
        String vmm = Config.FOG_DEVICE_VMM;
        double time_zone = Config.FOG_DEVICE_TIMEZONE;
        double cost = Config.FOG_DEVICE_COST;
        double costPerMem = Config.FOG_DEVICE_COST_PER_MEMORY;
        double costPerStorage = Config.FOG_DEVICE_COST_PER_STORAGE;
        double costPerBw = Config.FOG_DEVICE_COST_PER_BW;
        FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(arch, os, vmm, host, time_zone,
                cost, costPerMem, costPerStorage, costPerBw);
        setCharacteristics(characteristics);

        setLastProcessTime(0.0);
        setVmList(new ArrayList<>());
        setUpLinkBandwidth(upLinkBandwidth);
        setDownLinkBandwidth(downLinkBandwidth);
        setUpLinkLatency(upLinkLatency);
        setAssociatedActuatorIds(new ArrayList<>());
        for(Host host_t: getCharacteristics().getHostList()) {
            host_t.setDatacenter(this);
        }
        setActiveApplications(new ArrayList<>());
        if(getCharacteristics().getNumberOfPes() == 0) {
            throw new Exception(getName() + "has no Processing element!");
        }
        getCharacteristics().setId(super.getId());
        applicationMap = new HashMap<>();
        appToModulesMap = new HashMap<String, List<String>>();
        northTupleQueue = new LinkedList<Tuple>();
        southTupleQueue = new LinkedList<Pair<Tuple, Integer>>();
        setNorthLinkBusy(false);
        setSouthLinkBusy(false);
        setChildrenIds(new ArrayList<Integer>());
        setChildToOperatorsMap(new HashMap<Integer, List<String>>());
        cloudTrafficMap = new HashMap<Integer, Integer>();
        lockTime = 0;
        energyConsumption = 0;
        lastUtilization = 0;
        setTotalCost(0);
        setChildToLatencyMap(new HashMap<Integer, Double>());
        setModuleInstanceCount(new HashMap<String, Map<String, Integer>>());
    }

    /**
     * Overrides this method when making a new and different type of resource. <br>
     * <b>NOTE:</b> You do not need to override method, if you use this method.
     *
     * @pre $none
     * @post $none
     */
    protected void registerOtherEntity() {
        //如之前所述，FogDevice是根据CloudSim中的DataCenter改写而来的
        //DataCenter中该函数也为空，表示不考虑不同类型的资源
    }

    protected void updateCloudTraffic() {
        int time = (int)CloudSim.clock() / 1000;
        if(!cloudTrafficMap.containsKey(time)) {
            cloudTrafficMap.put(time , 0);
        }
        cloudTrafficMap.put(time , cloudTrafficMap.get(time) + 1);
    }

    protected void sendUpFreeLink(Tuple tuple) {
        double networkDelay = tuple.getCloudletFileSize() / getUpLinkBandwidth();
        //时间 = 文件内容 / 下载速度
        setNorthLinkBusy(true);
        send(getId() , networkDelay , FogEvents.UPDATE_NORTH_TUPLE_QUEUE);
        send(parentId , networkDelay + getUplinkLatency() , FogEvents.TUPLE_ARRIVAL , tuple);
        NetworkUsageMonitor.sendingTuple(getUplinkLatency() , tuple.getCloudletFileSize());
    }
    //二者对比，说明一个FogDevice只能有一个parent，但是能有多个children，因此parent的latency可以直接获取
    //但是children的latency却要通过childToLatencyMap获取
    protected void sendDownFreeLink(Tuple tuple , int childId) {
        double networkDelay = tuple.getCloudletFileSize() / getDownLinkBandwidth();
        //时间 = 文件内容 / 下载速度
        setSouthLinkBusy(true);
        double latency = getChildToLatencyMap().get(childId);//在map中找到这个child的延迟
        send(getId() , networkDelay , FogEvents.UPDATE_SOUTH_TUPLE_QUEUE);
        send(childId , networkDelay + latency , FogEvents.TUPLE_ARRIVAL , tuple);
        NetworkUsageMonitor.sendingTuple(latency , tuple.getCloudletFileSize());
    }

    protected void sendUp(Tuple tuple) {
        if(parentId > 0) {
            if(!isNorthLinkBusy()) {
                sendUpFreeLink(tuple);
            }
            else {
                northTupleQueue.add(tuple);
            }
        }
    }

    protected void sendDown(Tuple tuple , int childId) {
        if(getChildrenIds().contains(childId)) {
            if(!isSouthLinkBusy()) {
                sendDownFreeLink(tuple , childId);//向下传的节点空闲
            }
            else {
                southTupleQueue.add(new Pair<Tuple , Integer>(tuple , childId));
            }
        }
    }

    protected void sendTupleToActuator(Tuple tuple) {
        //根据tuple的目标(Destination)在相关的Actuator中找到它进行传输，线性查找，O(n)
        for(Pair<Integer , Double> actuatorAssociation: getAssociatedActuatorIds()) {
            int actuatorId = actuatorAssociation.getFirst();
            String actuatorType = ((Actuator)CloudSim.getEntity(actuatorId)).getActuatorType();
            if(tuple.getDestModuleName().equals(actuatorType)) {
                //这个是向上传
                double delay = actuatorAssociation.getSecond();
                send(actuatorId , delay , FogEvents.TUPLE_ARRIVAL , tuple);
                return;
            }
        }
        for(int child: getChildrenIds()) {
            sendDown(tuple , child);
        }
    }

    protected void updateTimingsOnReceipt(Tuple tuple) {
        Application app = getApplicationMap().get(tuple.getAppId());
        String srcModule = tuple.getSrcModuleName();
        String destModule = tuple.getDestModuleName();
        List<AppLoop> loops = app.getLoops();
        for(AppLoop loop: loops) {//同样还是顺序查找
            if(loop.hasEdge(srcModule , destModule) && loop.isEndModule(destModule)) {
                Double startTime = TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
                if(startTime == null) {
                    break;
                }
                if(!TimeKeeper.getInstance().getLoopIdToCurrentAverage().containsKey(loop.getLoopId())) {
                    TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), 0.0);
                    TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), 0);
                }
                double currentAverage = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop.getLoopId());
                int currentCount = TimeKeeper.getInstance().getLoopIdToCurrentNum().get(loop.getLoopId());
                double delay = CloudSim.clock() - TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
                TimeKeeper.getInstance().getEmitTimes().remove(tuple.getActualTupleId());
                double newAverage = (currentAverage * currentCount + delay) / (currentCount + 1);
                //重新计算平均值，可能延迟是根据平均值来的
                TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), newAverage);
                TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), currentCount+1);
                break;
            }
        }
    }

    private void updateEnergyConsumption() {
        double totalMipsAllocated = 0;
        for(final Vm vm: getHost().getVmList()) {
            AppModule operator = (AppModule)vm;
            operator.updateVmProcessing(CloudSim.clock() , getVmAllocationPolicy().getHost(operator).getVmScheduler().getAllocatedMipsForVm(operator));
            totalMipsAllocated += getHost().getTotalAllocatedMipsForVm(vm);
        }
        double timeNow = CloudSim.clock();
        double currentEnergyConsumption = getEnergyConsumption();
        double newEnergyConsumption = currentEnergyConsumption + (timeNow - lastUtilizationUpdateTime) * getHost().getPowerModel().getPower(lastUtilization);
        setEnergyConsumption(newEnergyConsumption);
        double currentCost = getTotalCost();
        double newCost = currentCost + (timeNow - lastUtilizationUpdateTime) * getRatePerMips() * lastUtilization*getHost().getTotalMips();
        setTotalCost(newCost);
        lastUtilization = Math.min(1 , totalMipsAllocated/getHost().getTotalMips());
        lastUtilizationUpdateTime = timeNow;
    }

    protected void updateAllocatedMips(String incomingOperator) {
        getHost().getVmScheduler().deallocatePesForAllVms();
        for(final Vm vm: getHost().getVmList()){
            if(vm.getCloudletScheduler().runningCloudlets() > 0 || ((AppModule)vm).getName().equals(incomingOperator)) {
                getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<>() {
                    protected static final long serialVersionUID = 1L; {
                        add((double) getHost().getTotalMips());
                    }
                });
            }
            else {
                getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<>() {
                    protected static final long serialVersionUID = 1L; {
                        add(0.0);
                    }
                });
            }
        }
        updateEnergyConsumption();
    }

    protected void executeTuple(SimEvent ev , String moduleName) {
        Logger.debug(getName(), "Executing tuple on module " + moduleName);
        Tuple tuple = (Tuple)ev.getData();
        AppModule module = getModuleByName(moduleName);
        if(tuple.getDirection() == Tuple.UP) {//向上传
            String srcModule = tuple.getSrcModuleName();
            if(!module.getDownInstanceIdsMaps().containsKey(srcModule)) {
                module.getDownInstanceIdsMaps().put(srcModule, new ArrayList<>());
            }
            if(!module.getDownInstanceIdsMaps().get(srcModule).contains(tuple.getSourceModuleId())) {
                module.getDownInstanceIdsMaps().get(srcModule).add(tuple.getSourceModuleId());
            }//Id map中不含srcModule的话就创建
            int instances = -1;
            for(String _moduleName: module.getDownInstanceIdsMaps().keySet()){
                instances = Math.max(module.getDownInstanceIdsMaps().get(_moduleName).size() , instances);
            }
            module.setNumInstances(instances);
        }
        TimeKeeper.getInstance().tupleStartedExecution(tuple);
        updateAllocatedMips(moduleName);
        processCloudletSubmit(ev , false);
        updateAllocatedMips(moduleName);
    }

    protected void processTupleArrival(SimEvent ev) {
        Tuple tuple = (Tuple)ev.getData();
        if(getName().equals("cloud")) {//如果是云端
            updateCloudTraffic();
        }
        Logger.debug(getName() , "Received tuple " + tuple.getCloudletId() + "with tupleType" + tuple.getTupleType() + "\t| Source : " + CloudSim.getEntityName(ev.getSource())+"|Dest : "+CloudSim.getEntityName(ev.getDestination()));
        send(ev.getSource() , CloudSim.getMinTimeBetweenEvents() , FogEvents.TUPLE_ACK);
        if(FogUtils.appIdToGeoCoverageMap.containsKey(tuple.getAppId())) {
            //System.out.println("Why this if sentence is empty?");
        }
        if(tuple.getDirection() == Tuple.ACTUATOR) {
            //如果是送到actuator的tuple
            sendTupleToActuator(tuple);
            return;
        }
        if(getHost().getVmList().size() > 0) {
            final AppModule operator = (AppModule)getHost().getVmList().get(0);
            if(CloudSim.clock() > 0) {
                getHost().getVmScheduler().deallocatePesForVm(operator);
                getHost().getVmScheduler().allocatePesForVm(operator , new ArrayList<>() {
                    protected static final long serialVersionUID = 1L; {
                        add((double)getHost().getTotalMips());
                    }
                });
            }
        }
        if(getName().equals("cloud") && tuple.getDestModuleName() == null) {
            sendNow(getControllerId() , FogEvents.TUPLE_FINISHED , null);
        }
        if(appToModulesMap.containsKey(tuple.getAppId())) {
            if(appToModulesMap.get(tuple.getAppId()).contains(tuple.getDestModuleName())) {
                int vmId = -1;
                for (Vm vm : getHost().getVmList()) {//还是一个顺序查找
                    if (((AppModule) vm).getName().equals(tuple.getDestModuleName())) {
                        vmId = vm.getId();
                    }
                }
                if (vmId < 0 || tuple.getModuleCopyMap().containsKey(tuple.getDestModuleName()) && tuple.getModuleCopyMap().get(tuple.getDestModuleName()) != vmId) {
                    //VmList中不存在tuple的Destination module或是Module copy map中的vm Id不对应
                    return;
                }
                tuple.setVmId(vmId);
                updateTimingsOnReceipt(tuple);//接收同时更新时间
                executeTuple(ev , tuple.getDestModuleName());
            }
            else if(tuple.getDestModuleName() != null) {
                if(tuple.getDirection() == Tuple.UP)
                    sendUp(tuple);
                else if(tuple.getDirection() == Tuple.DOWN) {
                    for(int childId: getChildrenIds())
                        sendDown(tuple, childId);
                }
            }
            else {
                sendUp(tuple);
            }
        }
        else {
            if(tuple.getDirection() == Tuple.UP) {
                sendUp(tuple);
            }
            else if(tuple.getDirection() == Tuple.DOWN) {
                for(int childId: getChildrenIds())
                    sendDown(tuple, childId);
            }
        }
    }

    private void initializePeriodicTuples(AppModule module) {
        String appId = module.getAppId();
        Application app = getApplicationMap().get(appId);
        List<AppEdge> periodicEdges = app.getPeriodicEdges(module.getName());
        for(AppEdge edge: periodicEdges) {
            send(getId() , edge.getPeriodicity() , FogEvents.SEND_PERIODIC_TUPLE , edge);
        }
    }

    protected void processModuleArrival(SimEvent ev) {
        AppModule module = (AppModule)ev.getData();
        String appId = module.getAppId();
        if(!appToModulesMap.containsKey(appId)) {
            appToModulesMap.put(appId , new ArrayList<>());
        }
        appToModulesMap.get(appId).add(module.getName());
        processVmCreate(ev , false);
        if(module.isBeingInstantiated()) {//如果被实例化
            module.setBeingInstantiated(false);
        }
        initializePeriodicTuples(module);
        module.updateVmProcessing(CloudSim.clock() , getVmAllocationPolicy().getHost(module).getVmScheduler().getAllocatedMipsForVm(module));
    }

    protected void processOperatorRelease(SimEvent ev) {
        processVmMigrate(ev , false);
    }

    protected void processSensorJoining(SimEvent ev) {
        send(ev.getSource() , CloudSim.getMinTimeBetweenEvents() , FogEvents.TUPLE_ACK);
    }

    protected void updateTimingsOnSending(Tuple resTuple) {
        //TODO ADD CODE FOR UPDATING TIMINGS WHEN A TUPLE IS GENERATED FROM A PREVIOUSLY RECEIVED TUPLE.
        // WILL NEED TO CHECK IF A NEW LOOP STARTS AND INSERT A UNIQUE TUPLE ID TO IT.
        String srcModule = resTuple.getSrcModuleName();
        String destModule = resTuple.getDestModuleName();
        for(AppLoop loop: getApplicationMap().get(resTuple.getAppId()).getLoops()) {
            if(loop.hasEdge(srcModule, destModule) && loop.isStartModule(srcModule)) {
                int tupleId = TimeKeeper.getInstance().getUniqueId();
                resTuple.setActualTupleId(tupleId);
                if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId())) {
                    TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId() , new ArrayList<>());
                }
                TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
                TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
            }
        }
    }

    protected void sendToSelf(Tuple tuple){
        send(getId(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ARRIVAL, tuple);
    }

    protected void sendPeriodicTuple(SimEvent ev) {
        AppEdge edge = (AppEdge)ev.getData();
        String srcModule = edge.getSource();
        AppModule module = getModuleByName(srcModule);
        if(module == null) {
            return;
        }
        int instanceCount = module.getNumInstances();
        int i = 0;
        while(true) {
            Tuple tuple = applicationMap.get(module.getAppId()).createTuple(edge , getId() , module.getId());
            updateTimingsOnSending(tuple);
            sendToSelf(tuple);
            if(edge.getDirection() == Tuple.UP) {
                if(i >= instanceCount) {
                    break;
                }
            }
            else if(i >= 1) {
                break;
            }
            i ++;
        }
    }

    protected void processAppSubmit(SimEvent ev) {
        Application app = (Application)ev.getData();
        applicationMap.put(app.getAppId() , app);
    }

    protected void updateNorthTupleQueue(SimEvent ev) {
        if(!getNorthTupleQueue().isEmpty()) {
            Tuple tuple = getNorthTupleQueue().poll();
            assert tuple != null;
            sendUpFreeLink(tuple);
        }
        else {
            setNorthLinkBusy(false);
        }
    }

    protected void updateSouthTupleQueue(SimEvent ev) {
        if(!getSouthTupleQueue().isEmpty()) {
            Pair<Tuple , Integer> pair = getSouthTupleQueue().poll();
            assert pair != null;
            sendDownFreeLink(pair.getFirst() , pair.getSecond());
        }
        else {
            setSouthLinkBusy(false);
        }
    }

    protected void updateActiveApplication(SimEvent ev) {
        Application app = (Application)ev.getData();
        getActiveApplications().add(app.getAppId());
    }

    protected void processActuatorJoined(SimEvent ev) {
        int actuatorId = ev.getSource();
        double delay = (double)ev.getData();
        getAssociatedActuatorIds().add(new Pair<>(actuatorId , delay));
    }

    protected void updateModuleInstanceCount(SimEvent ev) {
        ModuleLaunchConfig config = (ModuleLaunchConfig)ev.getData();
        String appId = config.getModule().getAppId();
        if(!moduleInstanceCount.containsKey(appId)) {
            moduleInstanceCount.put(appId , new HashMap<>());
        }
        moduleInstanceCount.get(appId).put(config.getModule().getAppId() , config.getInstanceCount());
        System.out.println(getName() + " Creating " + config.getInstanceCount() + " instances of module " + config.getModule().getName());
    }

    protected void manageResources(SimEvent ev) {
        updateEnergyConsumption();
        send(getId() , Config.RESOURCE_MGMT_INTERVAL , FogEvents.RESOURCE_MGMT);
    }

    @Override
    protected void processOtherEvent(SimEvent ev) {
        int tag = ev.getTag();
        if(tag == FogEvents.TUPLE_ARRIVAL) {
            processTupleArrival(ev);
        }
        else if(tag == FogEvents.LAUNCH_MODULE) {
            processModuleArrival(ev);
        }
        else if(tag == FogEvents.RELEASE_OPERATOR) {
            processOperatorRelease(ev);
        }
        else if(tag == FogEvents.SENSOR_JOINED) {
            processSensorJoining(ev);
        }
        else if(tag == FogEvents.SEND_PERIODIC_TUPLE) {
            sendPeriodicTuple(ev);//periodic周期的
        }
        else if(tag == FogEvents.APP_SUBMIT) {
            processAppSubmit(ev);
        }
        else if(tag == FogEvents.UPDATE_NORTH_TUPLE_QUEUE) {
            updateNorthTupleQueue(ev);
        }
        else if(tag == FogEvents.UPDATE_SOUTH_TUPLE_QUEUE) {
            updateSouthTupleQueue(ev);
        }
        else if(tag == FogEvents.ACTIVE_APP_UPDATE) {
            updateActiveApplication(ev);
        }
        else if(tag == FogEvents.ACTUATOR_JOINED) {
            processActuatorJoined(ev);
        }
        else if(tag == FogEvents.LAUNCH_MODULE_INSTANCE) {
            updateModuleInstanceCount(ev);
        }
        else if(tag == FogEvents.RESOURCE_MGMT) {
            manageResources(ev);
        }
    }

    protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
        double currentTime = CloudSim.clock();
        double minTime = Double.MAX_VALUE;
        double timeDiff = currentTime - getLastProcessTime();
        double timeFrameDatacenterEnergy = 0.0;
        for (PowerHost host: this.<PowerHost> getHostList()) {
            Log.printLine();
            double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
            if (time < minTime) {
                minTime = time;
            }
            Log.formatLine("%.2f: [Host #%d] utilization is %.2f%%" , currentTime , host.getId() , host.getUtilizationOfCpu() * 100);
        }
        if (timeDiff > 0) {
            Log.formatLine("\nEnergy consumption for the last time frame from %.2f to %.2f:" , getLastProcessTime() , currentTime);

            for (PowerHost host : this.<PowerHost> getHostList()) {
                double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
                double utilizationOfCpu = host.getUtilizationOfCpu();
                double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
                        previousUtilizationOfCpu,
                        utilizationOfCpu,
                        timeDiff);
                timeFrameDatacenterEnergy += timeFrameHostEnergy;

                Log.printLine();
                Log.formatLine(
                        "%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
                        currentTime,
                        host.getId(),
                        getLastProcessTime(),
                        previousUtilizationOfCpu * 100,
                        utilizationOfCpu * 100);
                Log.formatLine(
                        "%.2f: [Host #%d] energy is %.2f W*sec",
                        currentTime,
                        host.getId(),
                        timeFrameHostEnergy);
            }

            Log.formatLine(
                    "\n%.2f: Data center's energy is %.2f W*sec\n",
                    currentTime,
                    timeFrameDatacenterEnergy);
        }

        setPower(getPower() + timeFrameDatacenterEnergy);

        checkCloudletCompletion();

        /** Remove completed VMs **/
        /**
         * Change made by HARSHIT GUPTA
         */
		/*for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}*/

        Log.printLine();

        setLastProcessTime(currentTime);
        return minTime;
    }


    protected void checkCloudletCompletion() {
        boolean cloudletCompleted = false;
        List<? extends Host> list = getVmAllocationPolicy().getHostList();
        for (int i = 0; i < list.size(); i++) {
            Host host = list.get(i);
            for (Vm vm : host.getVmList()) {
                while (vm.getCloudletScheduler().isFinishedCloudlets()) {
                    Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
                    if (cl != null) {

                        cloudletCompleted = true;
                        Tuple tuple = (Tuple)cl;
                        TimeKeeper.getInstance().tupleEndedExecution(tuple);
                        Application application = getApplicationMap().get(tuple.getAppId());
                        Logger.debug(getName(), "Completed execution of tuple "+tuple.getCloudletId()+"on "+tuple.getDestModuleName());
                        List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName(), tuple, getId(), vm.getId());
                        for(Tuple resTuple : resultantTuples){
                            resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
                            resTuple.getModuleCopyMap().put(((AppModule)vm).getName(), vm.getId());
                            updateTimingsOnSending(resTuple);
                            sendToSelf(resTuple);
                        }
                        sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
                    }
                }
            }
        }
        if(cloudletCompleted)
            updateAllocatedMips(null);
    }

    private AppModule getModuleByName(String moduleName) {
        AppModule module = null;
        for(Vm vm: getHost().getVmList()){
            if(((AppModule)vm).getName().equals(moduleName)){
                module = (AppModule)vm;
                break;
            }
        }
        return module;
    }

    public PowerHost getHost() {
        return (PowerHost) getHostList().get(0);
    }
    public int getParentId() {
        return parentId;
    }
    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
    public void setUplinkLatency(double latency) {
        upLinkLatency = latency;
    }
    public List<Integer> getChildrenIds() {
        return childrenIds;
    }
    public void setChildrenIds(List<Integer> childrenIds) {
        this.childrenIds = childrenIds;
    }
    public double getUpLinkBandwidth() {
        return upLinkBandwidth;
    }
    public void setUpLinkBandwidth(double upLinkBandwidth) {
        this.upLinkBandwidth = upLinkBandwidth;
    }
    public double getUplinkLatency() {
        return upLinkLatency;
    }
    public void setUpLinkLatency(double upLinkLatency) {
        this.upLinkLatency = upLinkLatency;
    }
    public boolean isSouthLinkBusy() {
        return isSouthLinkBusy;
    }
    public boolean isNorthLinkBusy() {
        return isNorthLinkBusy;
    }
    public void setSouthLinkBusy(boolean isSouthLinkBusy) {
        this.isSouthLinkBusy = isSouthLinkBusy;
    }
    public void setNorthLinkBusy(boolean isNorthLinkBusy) {
        this.isNorthLinkBusy = isNorthLinkBusy;
    }
    public int getControllerId() {
        return controllerId;
    }
    public void setControllerId(int controllerId) {
        this.controllerId = controllerId;
    }
    public List<String> getActiveApplications() {
        return activeApplications;
    }
    public void setActiveApplications(List<String> activeApplications) {
        this.activeApplications = activeApplications;
    }
    public Map<Integer, List<String>> getChildToOperatorsMap() {
        return childToOperatorsMap;
    }
    public void setChildToOperatorsMap(Map<Integer, List<String>> childToOperatorsMap) {
        this.childToOperatorsMap = childToOperatorsMap;
    }
    public Map<String, Application> getApplicationMap() {
        return applicationMap;
    }
    public void setApplicationMap(Map<String, Application> applicationMap) {
        this.applicationMap = applicationMap;
    }
    public Queue<Tuple> getNorthTupleQueue() {
        return northTupleQueue;
    }
    public void setNorthTupleQueue(Queue<Tuple> northTupleQueue) {
        this.northTupleQueue = northTupleQueue;
    }
    public Queue<Pair<Tuple, Integer>> getSouthTupleQueue() {
        return southTupleQueue;
    }
    public void setSouthTupleQueue(Queue<Pair<Tuple, Integer>> southTupleQueue) {
        this.southTupleQueue = southTupleQueue;
    }
    public double getDownLinkBandwidth() {
        return downLinkBandwidth;
    }
    public void setDownLinkBandwidth(double downLinkBandwidth) {
        this.downLinkBandwidth = downLinkBandwidth;
    }
    public List<Pair<Integer, Double>> getAssociatedActuatorIds() {
        return associatedActuatorIds;
    }
    public void setAssociatedActuatorIds(List<Pair<Integer, Double>> associatedActuatorIds) {
        this.associatedActuatorIds = associatedActuatorIds;
    }
    public double getEnergyConsumption() {
        return energyConsumption;
    }
    public void setEnergyConsumption(double energyConsumption) {
        this.energyConsumption = energyConsumption;
    }
    public Map<Integer, Double> getChildToLatencyMap() {
        return childToLatencyMap;
    }
    public void setChildToLatencyMap(Map<Integer, Double> childToLatencyMap) {
        this.childToLatencyMap = childToLatencyMap;
    }
    public int getLevel() {
        return level;
    }
    public void setLevel(int level) {
        this.level = level;
    }
    public double getRatePerMips() {
        return ratePerMips;
    }
    public void setRatePerMips(double ratePerMips) {
        this.ratePerMips = ratePerMips;
    }
    public double getTotalCost() {
        return totalCost;
    }
    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    public Map<String, Map<String, Integer>> getModuleInstanceCount() {
        return moduleInstanceCount;
    }
    public void setModuleInstanceCount(Map<String, Map<String, Integer>> moduleInstanceCount) {
        this.moduleInstanceCount = moduleInstanceCount;
    }
    public int getMips() {
        return mips;
    }
    public void setMips(int mips) {
        this.mips = mips;
    }
    public int getBandWidth() {
        return bandWidth;
    }
    public int getRate() {
        return rate;
    }

    public double getCostPerSecond() {
        return costPerSecond;
    }
    public double getTransCost() {
        return transCost;
    }
    public double getCalCost() {
        return calCost;
    }
    public boolean isAllocated() {
        return allocated;
    }
    public int isCloud() {
        return cloud;
    }
    public void setAllocated(boolean flag) {
        allocated = flag;
    }
    public void setBusyTime(double time) {
        busyTime = time;
    }
    public double getBusyTime() {
        return busyTime;
    }
    public void showInfo(){
        System.out.print("\nname:"+getName()+
                        "\nId:"+getId()+
                        "\nlevel:"+getLevel()+
                        "\nupLink Bw:"+getUpLinkBandwidth()+
                        "\ndownLink Bw"+getDownLinkBandwidth()+
                        "\nmips:"+getMips()+
                        "\nrate:"+getRatePerMips());
    }
    public  void setID(int id){
        super.setId(id);
    }
    public  double getBusyPower(){
        return this.host.getPowerModel().getMaxPower();
    }

    public  double getIdlePower(){
        return this.host.getSt
    }
}




















