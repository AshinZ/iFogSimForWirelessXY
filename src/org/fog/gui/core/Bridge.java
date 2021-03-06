package org.fog.gui.core;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;
import org.fog.utils.distribution.Distribution;
import org.fog.utils.distribution.NormalDistribution;
import org.fog.utils.distribution.UniformDistribution;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.swing.*;

public class Bridge {



	private static Node getNode(Graph graph, String name){
		for(Node node : graph.getAdjacencyList().keySet()){
			if(node!=null){
				if(node.getName().equals(name)){
					return node;
				}
			}
		}
		return null;
	}
	// 数据中转桥梁文件 用于json数据和我们使用的graph/fogdevices数据的输入输出
	// 考虑到graph和fogdevice数据的不相同，所以需要重写转换方法
	// convert from JSON object to Graph object0

	public static Graph jsonToGraph(String fileName, int type){
		
		Graph graph = new Graph();
		
		// type 0->physical topology 1->virtual topology
		if(0 == type){
			try {
				JSONObject doc = (JSONObject) JSONValue.parse(new FileReader(fileName));
			//	System.out.print(fileName+doc+"\n");
	    		JSONArray nodeArray = (JSONArray) doc.get("nodes");
	    		JSONObject nodeObject = (JSONObject) nodeArray.get(0);
	    		JSONArray nodes = (JSONArray) nodeObject.get("nodes");
	    	//	System.out.print("数据长度"+ nodes.size()+"测试输出\n"+nodes.toString()+"\n");
	    		@SuppressWarnings("unchecked")//告知编译器忽略 unchecked警告信息
				Iterator<JSONObject> iter =nodes.iterator(); 
				while(iter.hasNext()){
					JSONObject node = iter.next();
					String nodeType = (String) node.get("type");
					String nodeName = (String) node.get("name");
					
					if(nodeType.equalsIgnoreCase("host")){  //host
						long pes = (Long) node.get("pes");
						long mips = (Long) node.get("mips");
						int ram = new BigDecimal((Long)node.get("ram")).intValueExact();
						long storage = (Long) node.get("storage");
						long bw = new BigDecimal((Long)node.get("bw")).intValueExact();
						
						int num = 1;
						if (node.get("nums")!= null)
							num = new BigDecimal((Long)node.get("nums")).intValueExact();

						for(int n = 0; n< num; n++) {
							Node hNode = new HostNode(nodeName, nodeType, pes, mips, ram, storage, bw);
							graph.addNode(hNode);
						}
						
					} else if(nodeType.equals("FOG_DEVICE")){
						long mips = (Long) node.get("mips");
						int ram = new BigDecimal((Long)node.get("ram")).intValueExact();
						long upBw = new BigDecimal((Long)node.get("upBw")).intValueExact();
						long downBw = new BigDecimal((Long)node.get("downBw")).intValueExact();
						int level = new BigDecimal((Long)node.get("level")).intValue();
						double rate = new BigDecimal((Double)node.get("ratePerMips")).doubleValue();
						
						Node fogDevice = new FogDeviceGui(nodeName, mips, ram, upBw, downBw, level, rate);
						graph.addNode(fogDevice);
 
					} else if(nodeType.equals("SENSOR")){
						String sensorType = node.get("sensorType").toString();
						int distType = new BigDecimal((Long)node.get("distribution")).intValue();
						Distribution distribution = null;
						if(distType == Distribution.DETERMINISTIC)
							distribution = new DeterministicDistribution(new BigDecimal((Double)node.get("value")).doubleValue());
						else if(distType == Distribution.NORMAL){
							distribution = new NormalDistribution(new BigDecimal((Double)node.get("mean")).doubleValue(), 
									new BigDecimal((Double)node.get("stdDev")).doubleValue());
						} else if(distType == Distribution.UNIFORM){
							distribution = new UniformDistribution(new BigDecimal((Double)node.get("min")).doubleValue(), 
									new BigDecimal((Double)node.get("max")).doubleValue());
						}
						System.out.println("Sensor type : "+sensorType);
						Node sensor = new SensorGui(nodeName, sensorType, distribution);
						graph.addNode(sensor);
					} else if(nodeType.equals("ACTUATOR")){
						String actuatorType = node.get("actuatorType").toString(); 
						Node actuator = new ActuatorGui(nodeName, actuatorType);
						graph.addNode(actuator);
					} else {   //switch
						int bw = new BigDecimal((Long)node.get("bw")).intValueExact();
						long iops = (Long) node.get("iops");
						int upports =  new BigDecimal((Long)node.get("upports")).intValueExact();
						int downports = new BigDecimal((Long)node.get("downports")).intValueExact();
						
						Node sNode = new SwitchNode(nodeName, nodeType, iops, upports, downports, bw);
						graph.addNode(sNode);
					}
				}
					
				JSONArray links = (JSONArray) nodeObject.get("links");
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> linksIter =links.iterator(); 
				while(linksIter.hasNext()){
					JSONObject link = linksIter.next();
					String src = (String) link.get("source");  
					String dst = (String) link.get("destination");
					double lat = (Double) link.get("latency");
					
					Node source = (Node) getNode(graph, src);
					Node target = (Node) getNode(graph, dst);
					
					if(source!=null && target!=null){
						System.out.println("Adding edge between "+source.getName()+" & "+target.getName());
						Edge edge = new Edge(target, lat);
						graph.addEdge(source, edge);
					}
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
			
		}else if(1 == type){
			try {
				JSONObject doc = (JSONObject) JSONValue.parse(new FileReader(fileName));
	    		JSONArray nodes = (JSONArray) doc.get("nodes");
	    		@SuppressWarnings("unchecked")
				Iterator<JSONObject> iter = nodes.iterator(); 
				while(iter.hasNext()){
					JSONObject node = iter.next();
					
					String nodeType = (String) node.get("type");
					String nodeName = (String) node.get("name");
					int pes = new BigDecimal((Long)node.get("pes")).intValueExact();
					long mips = (Long) node.get("mips");
					int ram = new BigDecimal((Long)node.get("ram")).intValueExact();
					long size = (Long) node.get("size");
					
					Node vmNode = new VmNode(nodeName, nodeType, size, pes, mips, ram);
					graph.addNode(vmNode);
				}
				
				JSONArray links = (JSONArray) doc.get("links");
				
				@SuppressWarnings("unchecked")
				Iterator<JSONObject> linksIter = links.iterator(); 
				while(linksIter.hasNext()){
					JSONObject link = linksIter.next();
					String name = (String) link.get("name");
					String src = (String) link.get("source");  
					String dst = (String) link.get("destination");

					Object reqBw = link.get("bandwidth");
				
					long bw = 0;
					if(reqBw != null)
						bw = (Long) reqBw;
					
					Node source = getNode(graph, src);
					Node target = getNode(graph, dst);

					Edge edge = new Edge(target, name, bw);
					graph.addEdge(source, edge);
					
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		}
		System.out.println("############################");
		System.out.println(graph.getAdjacencyList());
		System.out.println("############################");
		return graph;
	}


	//读取json数据进行修改
	public  static Graph json2Graph(String fileName,
									int type,
									List<FogDevice> fogDevices,
									List<Sensor> sensors ,
									List <Actuator> actuators ,
									List <FogDeviceGuiData> fogDeviceGuiDataList,
									List <SensorGuiData> sensorGuiDataList,
									List <ActuatorGuiData> actuatorGuiDataList,
									List <LinkGuiData> linkGuiDataList
									){
		System.out.print("*****************");
		System.out.print("start parse json");
		System.out.print("*****************");
		try {
			JSONObject doc = (JSONObject) JSONValue.parse(new FileReader(fileName));
			//读取fogdevice
			JSONArray fogDeviceData = (JSONArray) doc.get("fogDeviceData");
			System.out.print("fogDeviceData:"+fogDeviceData.toString()+"\n");
			@SuppressWarnings("unchecked")//告知编译器忽略 unchecked警告信息
					Iterator<JSONObject> fogDeviceIter =fogDeviceData.iterator();
			while(fogDeviceIter.hasNext()) {
				JSONObject node = fogDeviceIter.next();
				//因为数据格式我们都是知道的，所以我们直接对数据进行解析
				String nodeName =(String) node.get("nodeName");
				long mips = (Long) node.get("mips");
				int ram = new BigDecimal((Long)node.get("ram")).intValueExact();
				long upBw = new BigDecimal((Long)node.get("upBw")).intValueExact();
				long downBw = new BigDecimal((Long)node.get("downBw")).intValueExact();
				int level = new BigDecimal((Long)node.get("level")).intValue();
				double ratePerMips = new BigDecimal((Double)node.get("ratePerMips")).doubleValue();
				double busyPower =  new BigDecimal((Double)node.get("busyPower")).doubleValue();
				double idlePower =  new BigDecimal((Double)node.get("idlePower")).doubleValue();

				FogDeviceGuiData addFogDeviceData = new FogDeviceGuiData(nodeName,mips,ram,upBw,downBw,level,ratePerMips,busyPower,idlePower);
				fogDeviceGuiDataList.add(addFogDeviceData);
				FogDevice addedfogdevide = createFogDevice(nodeName, mips, ram, upBw, downBw, level, ratePerMips, busyPower, idlePower);
				fogDevices.add(addedfogdevide);
				// TODO: 2020/9/28 将数据变成数据列表和fogdevice列表抽象出来，这样可以在addFogDevice和这里同时调用
				// TODO: 2020/10/15 已解决
			}

			//sensorData
			JSONArray sensorData = (JSONArray) doc.get("sensorData");
			Iterator <JSONObject> sensorIter = sensorData.iterator();
			while(sensorIter.hasNext()) {
				JSONObject node = sensorIter.next();
				String name = (String) node.get("name");  //传感器名字
				String sensorType = (String) node.get("sensorType");  //传感器类型
				String distributionType = (String) node.get("distributionType");  //分布方式
				double mean =  new BigDecimal((Double)node.get("mean")).doubleValue();;  //
				double stdDev =  new BigDecimal((Double)node.get("mean")).doubleValue();; //
				double min =  new BigDecimal((Double)node.get("min")).doubleValue();;  //
				double max =  new BigDecimal((Double)node.get("max")).doubleValue();;  //
				double deterministicValue =  new BigDecimal((Double)node.get("deterministicValue")).doubleValue();; //
				SensorGuiData addSensorData = new SensorGuiData(name, sensorType, distributionType, mean, stdDev, min, max, deterministicValue);
				sensorGuiDataList.add(addSensorData);
				Sensor newSensor = new Sensor(name, sensorType, -1, "test", new DeterministicDistribution(deterministicValue));
				sensors.add(newSensor);
				//		sensors.add(addsensorData.getSensor());
			}
			//actuator
			JSONArray actuatorData = (JSONArray) doc.get("actuatorData");
			Iterator <JSONObject> actuatorIter = actuatorData.iterator();
			while(actuatorIter.hasNext()){
				JSONObject node = actuatorIter.next();
				String name = (String)node.get("name") ;
				String actuatorType = (String) node.get("actuatorType");
				ActuatorGuiData addActuatorData = new ActuatorGuiData(name,actuatorType);
				actuatorGuiDataList.add(addActuatorData);
				Actuator newActuator = new Actuator(name, -1, "test", actuatorType);
				actuators.add(newActuator);
				//   actuator.add(addactuatorData.getActuator());
			}

			//Link
			JSONArray linkData = (JSONArray) doc.get("linkData");
			Iterator <JSONObject> linkIter = linkData.iterator();
			while(linkIter.hasNext()){
				JSONObject node= linkIter.next();
				String startNodeName = (String) node.get("startNodeName");
				String targetNodeName = (String) node.get("targetNodeName");
				double latency =  new BigDecimal((Double)node.get("latency")).doubleValue();;
				LinkGuiData addLinkData = new LinkGuiData(startNodeName,targetNodeName,latency);
				linkGuiDataList.add(addLinkData);
				// TODO: 2020/9/28 数据格式添加
				int sourceType = 0; //起始点类型 0 :fog 1:sensor   2: actuator
				int targetType = 0; //指向点类型
				int sourceNum  = 0;//起始点在相应集合中的序号
				int targetId   = 0;//m目标点在相应集合中的序号
				//	System.out.print(((Node) sourceNode.getSelectedItem()).getName());
				for(int i = 0; i < fogDevices.size() ; ++i){
					//		System.out.print(fogDevices.get(i).getName());
					if(Objects.equals(fogDevices.get(i).getName(), startNodeName)){//是出发点
						sourceType = 0;
						sourceNum = i;

					}
					else if(Objects.equals(fogDevices.get(i).getName(), targetNodeName)) {
						targetType = 0;
						targetId = fogDevices.get(i).getId();
					}
				}

				for(int i = 0; i < sensors.size() ; ++i){
					if(Objects.equals(sensors.get(i).getName(), startNodeName)){//是出发点
						sourceType = 1;
						sourceNum = i;
					}
					else if(Objects.equals(sensors.get(i).getName(), targetNodeName)) {
						targetType = 1;
						targetId = sensors.get(i).getId();
					}
				}
				for(int i = 0; i < actuators.size() ; ++i){
					if(Objects.equals(actuators.get(i).getName(), startNodeName)){//是出发点
						sourceType = 2;
						sourceNum = i;
					}
					else if(Objects.equals(actuators.get(i).getName(), targetNodeName)) {
						targetType = 2;
						targetId = actuators.get(i).getId();
					}
				}
				//设置id
				if(0 == sourceType){
					//		System.out.print("\n出发点id是"+sourceNum);
					//		System.out.print("\n延时为"+Double.parseDouble(tfLatency.getText()));
					//		System.out.print(fogDevices.get(sourceNum).getName());
					fogDevices.get(sourceNum).setParentId(targetId);
					fogDevices.get(sourceNum).setUplinkLatency(latency);
				}
				else if(1 == sourceType){
					sensors.get(sourceNum).setGatewayDeviceId(targetId);
					sensors.get(sourceNum).setLatency(latency);
					System.out.println();
					System.out.println("Set sensor to fog device");
					System.out.println();
				}
				else{
					actuators.get(sourceNum).setGatewayDeviceId(targetId);
					actuators.get(sourceNum).setLatency(latency);
				}
			}

			System.out.print("当前数据情况：fogdevice:"+fogDeviceGuiDataList.size()+
														"\n sensor:"+sensorGuiDataList.size()+
														"\n act:"+actuatorGuiDataList.size()+
														"\n link"+linkGuiDataList.size()
			);


		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return jsonToGraph(fileName,0);
	}

	// convert from Graph object to JSON object
	@SuppressWarnings("unchecked")
	public static JSONObject graphToJson(Graph graph){
		System.out.println();
		System.out.println("****************************");
		System.out.println(graph.getAdjacencyList());
		System.out.println("****************************");
	/*	if(graph.getAdjacencyList().size() < 1){
			return "Graph is Empty";
		}*/
		Map<Node, List<Node>> edgeList = new HashMap<Node, List<Node>>();
		
		JSONObject topo = new JSONObject();
		JSONArray nodes = new JSONArray();
		JSONArray links = new JSONArray();
		
		for (Entry<Node, List<Edge>> entry : graph.getAdjacencyList().entrySet()) {
			Node srcNode = entry.getKey();
			
			// add node
			JSONObject jobj = new JSONObject();
			switch(srcNode.getType()){
				case "ACTUATOR":
					ActuatorGui actuator = (ActuatorGui)srcNode;
					jobj.put("name", actuator.getName());
					jobj.put("type", actuator.getType());
					jobj.put("actuatorType", actuator.getActuatorType());
					break;
				case "SENSOR":
					SensorGui sensor = (SensorGui)srcNode;
					jobj.put("name", sensor.getName());
					jobj.put("sensorType", sensor.getSensorType());
					jobj.put("type", sensor.getType());
					jobj.put("distribution", sensor.getDistributionType());
					if(sensor.getDistributionType()==Distribution.DETERMINISTIC)
						jobj.put("value", ((DeterministicDistribution)sensor.getDistribution()).getValue());
					else if(sensor.getDistributionType()==Distribution.NORMAL){
						jobj.put("mean", ((NormalDistribution)sensor.getDistribution()).getMean());
						jobj.put("stdDev", ((NormalDistribution)sensor.getDistribution()).getStdDev());
					} else if(sensor.getDistributionType()==Distribution.UNIFORM){
						jobj.put("min", ((UniformDistribution)sensor.getDistribution()).getMin());
						jobj.put("max", ((UniformDistribution)sensor.getDistribution()).getMax());
					}
					break;
				case "FOG_DEVICE":
					FogDeviceGui fogDevice = (FogDeviceGui)srcNode;
					jobj.put("name", fogDevice.getName());
					jobj.put("type", fogDevice.getType());
					jobj.put("mips", fogDevice.getMips());
					jobj.put("ram", fogDevice.getRam());
					jobj.put("upBw", fogDevice.getUpBw());
					jobj.put("downBw", fogDevice.getDownBw());
					jobj.put("level", fogDevice.getLevel());
					jobj.put("ratePerMips", fogDevice.getRatePerMips());
					break;
				case "host":
					HostNode hNode = (HostNode)srcNode;
					jobj.put("name", hNode.getName());
					jobj.put("type", hNode.getType());
					jobj.put("pes", hNode.getPes());
					jobj.put("mips", hNode.getMips());
					jobj.put("ram", hNode.getRam());
					jobj.put("storage", hNode.getStorage());
					jobj.put("bw", hNode.getBw());
					break;
				case "core":
				case "edge":
				    SwitchNode sNode = (SwitchNode)srcNode;
					jobj.put("name", sNode.getName());
					jobj.put("type", sNode.getType());
					jobj.put("iops", sNode.getIops());
					jobj.put("upports", sNode.getDownports());
					jobj.put("downports", sNode.getDownports());
					jobj.put("bw", sNode.getBw());
					break;
				case "vm":
					VmNode vNode = (VmNode)srcNode;
					jobj.put("name", vNode.getName());
					jobj.put("type", vNode.getType());
					jobj.put("size", vNode.getSize());
					jobj.put("pes", vNode.getPes());
					jobj.put("mips", vNode.getMips());
					jobj.put("ram", vNode.getRam());
					break;
			}
			nodes.add(jobj);
			
			// add edge
			for (Edge edge : entry.getValue()) {
				Node destNode = edge.getNode();
				
				// check if edge exist (dest->src)
				if (edgeList.containsKey(destNode) && edgeList.get(destNode).contains(srcNode)) {
					continue;
				}
				
				JSONObject jobj2 = new JSONObject();
				jobj2.put("source", srcNode.getName());
				jobj2.put("destination", destNode.getName());
				if("host"==destNode.getType() || "core"==destNode.getType() || "edge"==destNode.getType() || 
						"FOG_DEVICE"==destNode.getType() || "SENSOR"==destNode.getType() || "ACTUATOR"==destNode.getType()){
					jobj2.put("latency", edge.getLatency());
				}else if("vm"==destNode.getName()){
					if(edge.getBandwidth()>0){
						jobj2.put("bandwidth", edge.getBandwidth());
					}
				}
				links.add(jobj2);
				
				// add exist edge to the edgeList
				if (edgeList.containsKey(entry.getKey())) {
					edgeList.get(entry.getKey()).add(edge.getNode());
				} else {
					List<Node> ns = new ArrayList<Node>();
					ns.add(edge.getNode());
					edgeList.put(entry.getKey(), ns);
				}
				
			}
		}
		topo.put("nodes", nodes);
		topo.put("links", links);
		return topo;
	/*	StringWriter out = new StringWriter();
		String jsonText = "";
		try {
			topo.writeJSONString(out);
			jsonText = out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		
		//System.out.println(jsonText);
		//return jsonText;
	}

	//重载graphToJson方法
	@SuppressWarnings("unchecked")
    public static String  graph2Json(Graph graph ,
									  List<FogDeviceGuiData> fogDevicesGuiDataList,
									  List<SensorGuiData> sensorsGuiDataList,
									  List<ActuatorGuiData> actuatorsGuiDataList,
									  List<LinkGuiData> linkGuiDataList){
        System.out.println("*****************************");
        System.out.println("start change");
        System.out.println("*****************************");
      //  String graphString = graphToJson(graph);
		JSONObject graphInfo = graphToJson(graph);
		JSONArray graphData = new JSONArray();
		graphData.add(graphInfo);
        //上面是graph的输出
        // 输出fogDeviceGuiDataList
        //四个array放数据
		JSONArray fogDeviceData = new JSONArray();
		JSONArray sensorData = new JSONArray();
		JSONArray actuatorData = new JSONArray();
		JSONArray linkData = new JSONArray();
		//数据转json传入array中
		for (FogDeviceGuiData fogDeviceGuiData :fogDevicesGuiDataList){
				fogDeviceData.add(fogDeviceGuiData.toJson());
		}
		for(SensorGuiData sensorGuiData :sensorsGuiDataList){
				sensorData.add(sensorGuiData.toJson());
		}
		for (ActuatorGuiData actuatorGuiData :actuatorsGuiDataList){
				actuatorData.add(actuatorGuiData.toJson());
		}
		for (LinkGuiData linkGuiData : linkGuiDataList){
				linkData.add(linkGuiData.toJson());
		}
		//用一个json对象存储整个数据 这样可以帮助读取的时候进行解析
		JSONObject allData = new JSONObject();
		allData.put("linkData",linkData);
		allData.put("actuatorData",actuatorData);
		allData.put("sensorData",sensorData);
		allData.put("fogDeviceData",fogDeviceData);
		allData.put("nodes",graphData);

		//jsonToString
		StringWriter out = new StringWriter();
		String jsonText = "";
		try {
			allData.writeJSONString(out);
			jsonText = out.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return jsonText;
    }

	private static FogDevice createFogDevice(String nodeName, long mips,
											 int ram, long upBw, long downBw, int level, double ratePerMips, double busyPower, double idlePower) {

		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				new FogLinearPowerModel(busyPower, idlePower)
		);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		String arch = "x86"; // system architecture
		String os = "Linux"; // operating system
		String vmm = "Xen";
		double time_zone = 10.0; // time zone this resource located
		double cost = 3.0; // the cost of using processing in this resource
		double costPerMem = 0.05; // the cost of using memory in this resource
		double costPerStorage = 0.001; // the cost of using storage in this
		// resource
		double costPerBw = 0.0; // the cost of using bw in this resource
		LinkedList<Storage> storageList = new LinkedList<Storage>(); // we are not adding SAN
		// devices by now

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		FogDevice fogdevice = null;
		try {
			fogdevice = new FogDevice(nodeName, characteristics,
					new AppModuleAllocationPolicy(hostList), storageList, 10, upBw, downBw, 0, ratePerMips);
		} catch (Exception e) {
			e.printStackTrace();
		}

		fogdevice.setLevel(level);
		return fogdevice;
	}
}
