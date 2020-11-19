package org.service;

import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.control.DataController;
import org.fog.application.Application;
import org.fog.entities.Actuator;
import org.fog.entities.FogBroker;
import org.fog.entities.FogDevice;
import org.fog.entities.Sensor;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;
import net.sf.json.JSONObject;
import org.service.Impl.JsonDataController;

import java.util.ArrayList;
import java.util.Calendar;

import static org.fog.application.Application.createApplication;

/**
 * @author Z_HAO 2020/11/18
 */
public class RunService {
    DataController dataController;
    FogData fogData;
    JSONObject object;

    private ArrayList<FogDevice> fogDevices;
    private ArrayList<Sensor> sensors;
    private ArrayList<Actuator> actuators;

    String data = "{\"linkData\":[{\"targetNodeName\":\"m-1-1\",\"latency\":1,\"startNodeName\":\"a-1-1\"},{\"targetNodeName\":\"m-0-0\",\"latency\":1,\"startNodeName\":\"a-0-0\"},{\"targetNodeName\":\"m-0-1\",\"latency\":1,\"startNodeName\":\"a-0-1\"},{\"targetNodeName\":\"m-1-0\",\"latency\":1,\"startNodeName\":\"a-1-0\"},{\"targetNodeName\":\"m-0-0\",\"latency\":6,\"startNodeName\":\"s-0-0\"},{\"targetNodeName\":\"m-0-1\",\"latency\":6,\"startNodeName\":\"s-0-1\"},{\"targetNodeName\":\"m-1-0\",\"latency\":6,\"startNodeName\":\"s-1-0\"},{\"targetNodeName\":\"m-1-1\",\"latency\":6,\"startNodeName\":\"s-1-1\"},{\"targetNodeName\":\"d-0\",\"latency\":4,\"startNodeName\":\"m-0-0\"},{\"targetNodeName\":\"d-1\",\"latency\":4,\"startNodeName\":\"m-1-1\"},{\"targetNodeName\":\"d-0\",\"latency\":4,\"startNodeName\":\"m-0-1\"},{\"targetNodeName\":\"d-1\",\"latency\":4,\"startNodeName\":\"m-1-0\"},{\"targetNodeName\":\"proxy-server\",\"latency\":6,\"startNodeName\":\"d-1\"},{\"targetNodeName\":\"proxy-server\",\"latency\":6,\"startNodeName\":\"d-0\"},{\"targetNodeName\":\"cloud\",\"latency\":100,\"startNodeName\":\"proxy-server\"}],\"nodes\":[{\"nodes\":[{\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"m-1-1\",\"type\":\"FOG_DEVICE\",\"mips\":1000,\"ram\":1000,\"downBw\":270},{\"name\":\"a-1-1\",\"type\":\"ACTUATOR\",\"actuatorType\":\"DISPLAY\"},{\"level\":1,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"proxy-server\",\"type\":\"FOG_DEVICE\",\"mips\":2800,\"ram\":4000,\"downBw\":10000},{\"level\":2,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"d-1\",\"type\":\"FOG_DEVICE\",\"mips\":2800,\"ram\":4000,\"downBw\":10000},{\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"m-0-1\",\"type\":\"FOG_DEVICE\",\"mips\":1000,\"ram\":1000,\"downBw\":270},{\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"m-1-0\",\"type\":\"FOG_DEVICE\",\"mips\":1000,\"ram\":1000,\"downBw\":270},{\"level\":2,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"d-0\",\"type\":\"FOG_DEVICE\",\"mips\":2800,\"ram\":4000,\"downBw\":10000},{\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"name\":\"m-0-0\",\"type\":\"FOG_DEVICE\",\"mips\":1000,\"ram\":1000,\"downBw\":270},{\"name\":\"a-0-0\",\"type\":\"ACTUATOR\",\"actuatorType\":\"DISPLAY\"},{\"name\":\"a-0-1\",\"type\":\"ACTUATOR\",\"actuatorType\":\"DISPLAY\"},{\"name\":\"a-1-0\",\"type\":\"ACTUATOR\",\"actuatorType\":\"DISPLAY\"},{\"sensorType\":\"EEG\",\"name\":\"s-0-1\",\"type\":\"SENSOR\",\"distribution\":2,\"value\":4},{\"sensorType\":\"EEG\",\"name\":\"s-1-0\",\"type\":\"SENSOR\",\"distribution\":2,\"value\":4},{\"level\":0,\"upBw\":100,\"ratePerMips\":0.01,\"name\":\"cloud\",\"type\":\"FOG_DEVICE\",\"mips\":44800,\"ram\":40000,\"downBw\":10000},{\"sensorType\":\"EEG\",\"name\":\"s-0-0\",\"type\":\"SENSOR\",\"distribution\":2,\"value\":4},{\"sensorType\":\"EEG\",\"name\":\"s-1-1\",\"type\":\"SENSOR\",\"distribution\":2,\"value\":4}],\"links\":[{\"latency\":4,\"destination\":\"d-1\",\"source\":\"m-1-1\"},{\"latency\":1,\"destination\":\"m-1-1\",\"source\":\"a-1-1\"},{\"latency\":100,\"destination\":\"cloud\",\"source\":\"proxy-server\"},{\"latency\":6,\"destination\":\"proxy-server\",\"source\":\"d-1\"},{\"latency\":4,\"destination\":\"d-0\",\"source\":\"m-0-1\"},{\"latency\":4,\"destination\":\"d-1\",\"source\":\"m-1-0\"},{\"latency\":6,\"destination\":\"proxy-server\",\"source\":\"d-0\"},{\"latency\":4,\"destination\":\"d-0\",\"source\":\"m-0-0\"},{\"latency\":1,\"destination\":\"m-0-0\",\"source\":\"a-0-0\"},{\"latency\":1,\"destination\":\"m-0-1\",\"source\":\"a-0-1\"},{\"latency\":1,\"destination\":\"m-1-0\",\"source\":\"a-1-0\"},{\"latency\":6,\"destination\":\"m-0-1\",\"source\":\"s-0-1\"},{\"latency\":6,\"destination\":\"m-1-0\",\"source\":\"s-1-0\"},{\"latency\":6,\"destination\":\"m-0-0\",\"source\":\"s-0-0\"},{\"latency\":6,\"destination\":\"m-1-1\",\"source\":\"s-1-1\"}]}],\"actuatorData\":[{\"name\":\"a-0-0\",\"actuatorType\":\"DISPLAY\"},{\"name\":\"a-0-1\",\"actuatorType\":\"DISPLAY\"},{\"name\":\"a-1-0\",\"actuatorType\":\"DISPLAY\"},{\"name\":\"a-1-1\",\"actuatorType\":\"DISPLAY\"}],\"fogDeviceData\":[{\"nodeName\":\"cloud\",\"busyPower\":87.53,\"level\":0,\"upBw\":100,\"ratePerMips\":0.01,\"mips\":44800,\"idlePower\":82.44,\"ram\":40000,\"downBw\":10000},{\"nodeName\":\"proxy-server\",\"busyPower\":87.53,\"level\":1,\"upBw\":10000,\"ratePerMips\":0,\"mips\":2800,\"idlePower\":82.44,\"ram\":4000,\"downBw\":10000},{\"nodeName\":\"d-0\",\"busyPower\":87.53,\"level\":2,\"upBw\":10000,\"ratePerMips\":0,\"mips\":2800,\"idlePower\":82.44,\"ram\":4000,\"downBw\":10000},{\"nodeName\":\"d-1\",\"busyPower\":87.53,\"level\":2,\"upBw\":10000,\"ratePerMips\":0,\"mips\":2800,\"idlePower\":82.44,\"ram\":4000,\"downBw\":10000},{\"nodeName\":\"m-0-0\",\"busyPower\":87.53,\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"mips\":1000,\"idlePower\":82.44,\"ram\":1000,\"downBw\":270},{\"nodeName\":\"m-0-1\",\"busyPower\":87.53,\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"mips\":1000,\"idlePower\":82.44,\"ram\":1000,\"downBw\":270},{\"nodeName\":\"m-1-0\",\"busyPower\":87.53,\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"mips\":1000,\"idlePower\":82.44,\"ram\":1000,\"downBw\":270},{\"nodeName\":\"m-1-1\",\"busyPower\":87.53,\"level\":3,\"upBw\":10000,\"ratePerMips\":0,\"mips\":1000,\"idlePower\":82.44,\"ram\":1000,\"downBw\":270}],\"sensorData\":[{\"distributionType\":\"Deterministic\",\"min\":-1,\"max\":-1,\"mean\":-1,\"sensorType\":\"EEG\",\"name\":\"s-0-0\",\"deterministicValue\":4,\"stdDev\":-1},{\"distributionType\":\"Deterministic\",\"min\":-1,\"max\":-1,\"mean\":-1,\"sensorType\":\"EEG\",\"name\":\"s-0-1\",\"deterministicValue\":4,\"stdDev\":-1},{\"distributionType\":\"Deterministic\",\"min\":-1,\"max\":-1,\"mean\":-1,\"sensorType\":\"EEG\",\"name\":\"s-1-0\",\"deterministicValue\":4,\"stdDev\":-1},{\"distributionType\":\"Deterministic\",\"min\":-1,\"max\":-1,\"mean\":-1,\"sensorType\":\"EEG\",\"name\":\"s-1-1\",\"deterministicValue\":4,\"stdDev\":-1}]}";

    public RunService() {
        dataController = new JsonDataController();
        dataController.readData(data);
        fogData = (FogData)dataController.getData();
        object = (JSONObject)dataController.getWriter();
        fogDevices = fogData.getFogDevices();
        sensors = fogData.getSensors();
        actuators = fogData.getActuators();
    }

    public void run() {
        try {
            String appId = fogData.getAppId(); // identifier of the application

            FogBroker broker = fogData.getFogBroker();

            Application application = createApplication(appId, broker.getId(), fogData.getEEG_TRANSMISSION_TIME());
            application.setUserId(broker.getId());

            ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping

            if(fogData.isCLOUD()){
                // if the mode of deployment is cloud-based
				/*moduleMapping.addModuleToDevice("connector", "cloud", numOfDepts*numOfMobilesPerDept); // fixing all instances of the Connector module to the Cloud
				moduleMapping.addModuleToDevice("concentration_calculator", "cloud", numOfDepts*numOfMobilesPerDept); // fixing all instances of the Concentration Calculator module to the Cloud
*/				moduleMapping.addModuleToDevice("connector", "cloud"); // fixing all instances of the Connector module to the Cloud
                moduleMapping.addModuleToDevice("concentration_calculator", "cloud"); // fixing all instances of the Concentration Calculator module to the Cloud
                for(FogDevice device : fogDevices){
                    if(device.getName().startsWith("m")){
                        //moduleMapping.addModuleToDevice("client", device.getName(), 1);  // fixing all instances of the Client module to the Smartphones
                        moduleMapping.addModuleToDevice("client", device.getName());  // fixing all instances of the Client module to the Smartphones
                    }
                }
            }else{
                // if the mode of deployment is cloud-based
                //moduleMapping.addModuleToDevice("connector", "cloud", numOfDepts*numOfMobilesPerDept); // fixing all instances of the Connector module to the Cloud
                moduleMapping.addModuleToDevice("connector", "cloud"); // fixing all instances of the Connector module to the Cloud
                // rest of the modules will be placed by the Edge-ward placement policy
            }


            Controller controller = new Controller("master-controller", fogDevices, sensors, actuators, object);

            controller.submitApplication(application, 0,
                    (fogData.isCLOUD())?(new ModulePlacementMapping(fogDevices, application, moduleMapping))
                            :(new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping, object)));

            TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

            CloudSim.startSimulation();

            CloudSim.stopSimulation();

        } catch (Exception e) {
            e.printStackTrace();
            Log.printLine("Unwanted errors happen");
        }
        System.out.println(object.toString());
    }

    public static void main(String[] args) {
        Log.disable();
        int num_user = 1; // number of cloud users
        Calendar calendar = Calendar.getInstance();
        boolean trace_flag = false; // mean trace events
        CloudSim.init(num_user, calendar, trace_flag);
        RunService service = new RunService();
        service.run();
    }
}







