package org.service.Impl;

import javafx.util.Pair;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.control.DataController;
import org.fog.entities.Actuator;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.utils.distribution.DeterministicDistribution;
import org.service.FogData;

/**
 * @author Z_HAO 2020/11/17
 */
public class JsonDataController implements DataController {
    private FogData fogData;

    private JSONObject writeJson;

    public JsonDataController() {
        fogData = new FogData();
        writeJson = new JSONObject();

        JSONObject preMessage = new JSONObject();
        preMessage.put("placeMessage" , new JSONArray());
        preMessage.put("createMessage" , new JSONArray());

        JSONObject runMessage = new JSONObject();
        runMessage.put("execTime" , -1);
        runMessage.put("appLoopDelay" , new JSONObject());
        JSONObject tuple = new JSONObject();
        runMessage.put("tuple" , tuple);
        /*tuple.put("playerGameState" , -1);
        tuple.put("EEG" , -1);
        tuple.put("concentration" , -1);
        tuple.put("sensor" , -1);
        tuple.put("globalGameState" , -1);
        runMessage.put("tuple" , tuple);*/

        JSONObject resultMessage = new JSONObject();
        resultMessage.put("cost" , -1);
        resultMessage.put("total" , -1);
        resultMessage.put("device" , new JSONArray());

        writeJson.put("preMessage" , preMessage);
        writeJson.put("runMessage" , runMessage);
        writeJson.put("resultMessage" , resultMessage);
    }

    @Override
    public void readData(String jsonData) {
        JSONObject jsonObject = JSONObject.fromObject(jsonData);
        JSONArray fogDevices = jsonObject.getJSONArray("fogDeviceData");
        JSONArray sensors = jsonObject.getJSONArray("sensorData");
        JSONArray actuators = jsonObject.getJSONArray("actuatorData");
        JSONArray links = jsonObject.getJSONArray("linkData");

        addFogDevice(fogDevices , fogData);
        addSensor(sensors , fogData);
        addActuator(actuators , fogData);
        addLink(links , fogData);

        System.out.println("json data read success");
    }

    @Override
    public String writeData() {
        return writeJson.toString();
    }

    @Override
    public FogData getData() {
        return fogData;
    }

    public void setFogData(FogData fogData) {
        this.fogData = fogData;
    }

    private static void addFogDevice(JSONArray nodes , FogData fogData) {
        for(int i = 0;i < nodes.size();i ++) {
            JSONObject node = nodes.getJSONObject(i);
            String name = node.get("nodeName").toString();
            long mips = Long.parseLong(node.get("mips").toString());
            int ram = (int)node.get("ram");
            long upLinkBandwidth = Long.parseLong(node.get("upBw").toString());
            long downLinkBandwidth = Long.parseLong(node.get("downBw").toString());
            int level = (int)node.get("level");
            double ratePerMips = Double.parseDouble(node.get("ratePerMips").toString());
            double busyPower = Double.parseDouble(node.get("busyPower").toString());
            double idlePower = Double.parseDouble(node.get("idlePower").toString());
            FogDevice fogDevice = FogDevice.createFogDevice(name , mips , ram , upLinkBandwidth , downLinkBandwidth , level , ratePerMips , busyPower , idlePower);
            fogData.getFogDevices().add(fogDevice);
            fogData.getFogDeviceMap().put(name , fogDevice);
            fogData.getTypeMap().put(name , "FogDevice");
        }
    }

    private static void addSensor(JSONArray nodes , FogData fogData) {
        for(int i = 0;i < nodes.size();i ++) {
            JSONObject node = nodes.getJSONObject(i);
            String name = node.get("name").toString();
            String sensorType = node.get("sensorType").toString();
            String distributionType = node.get("distributionType").toString();
            double mean = Double.parseDouble(node.get("mean").toString());
            double stdDev = Double.parseDouble(node.get("stdDev").toString());
            double min = Double.parseDouble(node.get("min").toString());
            double max = Double.parseDouble(node.get("max").toString());
            double deterministicValue = Double.parseDouble(node.get("deterministicValue").toString());
            Sensor sensor = new Sensor(name , sensorType, fogData.getFogBroker().getId(), fogData.getAppId(), new DeterministicDistribution(deterministicValue));
            fogData.getSensors().add(sensor);
            fogData.getSensorMap().put(name , sensor);
            fogData.getTypeMap().put(name , "Sensor");
        }
    }

    private static void addActuator(JSONArray nodes , FogData fogData) {
        for(int i = 0;i < nodes.size();i ++) {
            JSONObject node = nodes.getJSONObject(i);
            String name = node.get("name").toString();
            String actuatorType = node.get("actuatorType").toString();
            Actuator actuator = new Actuator(name , fogData.getFogBroker().getId() , fogData.getAppId() , actuatorType);
            fogData.getActuators().add(actuator);
            fogData.getActuatorMap().put(name , actuator);
            fogData.getTypeMap().put(name , "Actuator");
        }
    }

    private static void addLink(JSONArray nodes, FogData fogData) {
        for(int i = 0;i < nodes.size();i ++) {
            JSONObject node = nodes.getJSONObject(i);
            String startName = node.get("startNodeName").toString();
            String targetName = node.get("targetNodeName").toString();
            double latency = Double.parseDouble(node.get("latency").toString());
            fogData.getLinks().put(new Pair<>(startName , targetName) , latency);

            String startType = fogData.getTypeMap().get(startName);
            String targetType = fogData.getTypeMap().get(targetName);
            FogDevice target = fogData.getFogDeviceByName(targetName);
            switch (startType) {
                case "FogDevice": {
                    FogDevice start = fogData.getFogDeviceByName(startName);
                    start.setParentId(target.getId());
                    start.setUplinkLatency(latency);
                    break;
                }
                case "Sensor": {
                    Sensor start = fogData.getSensorByName(startName);
                    start.setGatewayDeviceId(target.getId());
                    start.setLatency(latency);
                    break;
                }
                case "Actuator": {
                    Actuator start = fogData.getActuatorByName(startName);
                    start.setGatewayDeviceId(target.getId());
                    start.setLatency(latency);
                    break;
                }
            }
        }
    }

    @Override
    public JSONObject getWriter() {
        return writeJson;
    }

}
