package org.fog.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.*;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.entities.Sensor;
import org.fog.gui.core.Graph;
import org.fog.gui.core.SensorGui;
import org.fog.gui.core.SensorGuiData;
import org.fog.gui.core.SpringUtilities;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;
import org.fog.utils.distribution.DeterministicDistribution;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class AddSensor extends JDialog {
	private static final long serialVersionUID = -511667786177319577L;
	
	private final Graph graph;
	List<Sensor> sensors = new ArrayList<Sensor>();
	List <SensorGuiData> sensorGuiDataList = new ArrayList<SensorGuiData>();
	private JTextField sensorName;
	private JTextField sensorType;
	private JComboBox distribution;
	private JTextField uniformLowerBound;
	private JTextField uniformUpperBound;
	private JTextField deterministicValue;
	private JTextField normalMean;
	private JTextField normalStdDev;
	

	/**
	 * Constructor.
	 * 
	 * @param frame the parent frame
	 */
	public AddSensor(final Graph graph, final JFrame frame , List<Sensor> sensors, List <SensorGuiData> sensorGuiDataList) {
		this.graph = graph;
		this.sensors = sensors;
		this.sensorGuiDataList = sensorGuiDataList;
		setLayout(new BorderLayout());

		add(createInputPanelArea(), BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.PAGE_END);
		// show dialog
		setTitle("Add Sensor");
		setModal(true);
		setPreferredSize(new Dimension(350, 400));
		setResizable(false);
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);

	}

	private JPanel createButtonPanel() {

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		
		JButton okBtn = new JButton("Ok");
		JButton cancelBtn = new JButton("Cancel");
		
		cancelBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
            	setVisible(false);
            }
        });

		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				boolean catchedError = false;
				if (sensorName.getText() == null || sensorName.getText().length() < 1) {
					prompt("Please type Sensor name", "Error");
				} else if (sensorType.getText() == null || sensorType.getText().length() < 1) {
					prompt("Please type Sensor Type", "Error");
				} else if (distribution.getSelectedIndex() < 0) {
					prompt("Please select Emission time distribution", "Error");
				} else {
					double normalMean_ = -1;
					double normalStdDev_ = -1;
					double uniformLow_ = -1;
					double uniformUp_ = -1;
					double deterministicVal_ = -1;
					String _sensorType = sensorType.getText();
					String dist = (String)distribution.getSelectedItem();
					if(dist.equals("Normal")){
						try {
							normalMean_ = Double.parseDouble(normalMean.getText());
							normalStdDev_ = Double.parseDouble(normalStdDev.getText());
						} catch (NumberFormatException e1) {
							catchedError = true;
							prompt("Input should be numerical character", "Error");
						}
						if(!catchedError){
							SensorGui sensor = new SensorGui(sensorName.getText().toString(), _sensorType, (String)distribution.getSelectedItem(),
											normalMean_, normalStdDev_, uniformLow_, uniformUp_, deterministicVal_);
							graph.addNode(sensor);
							setVisible(false);
						}
					} else if(dist.equals("Uniform")){
						try {
							uniformLow_ = Double.parseDouble(uniformLowerBound.getText());
							uniformUp_ = Double.parseDouble(uniformUpperBound.getText());
						} catch (NumberFormatException e1) {
							catchedError = true;
							prompt("Input should be numerical character", "Error");
						}
						if(!catchedError){

							SensorGui sensor = new SensorGui(sensorName.getText().toString(), _sensorType, (String)distribution.getSelectedItem(),
									normalMean_, normalStdDev_, uniformLow_, uniformUp_, deterministicVal_);
							graph.addNode(sensor);
							setVisible(false);
						}
					} else if(dist.equals("Deterministic")){
						try {
							deterministicVal_ = Double.parseDouble(deterministicValue.getText());
						} catch (NumberFormatException e1) {
							catchedError = true;
							prompt("Input should be numerical character", "Error");
						}
						if(!catchedError){
							SensorGui sensor = new SensorGui(sensorName.getText().toString(), _sensorType, (String)distribution.getSelectedItem(),
									normalMean_, normalStdDev_, uniformLow_, uniformUp_, deterministicVal_);
							graph.addNode(sensor);
							setVisible(false);
						}
					}
					Sensor newSensor = new Sensor(sensorName.getText().toString(), sensorType.getText(), -1, "test", new DeterministicDistribution(deterministicVal_));
					sensors.add(newSensor);
					SensorGuiData addSensorGuiData = new SensorGuiData(sensorName.getText().toString(), _sensorType, (String)distribution.getSelectedItem(),
							normalMean_, normalStdDev_, uniformLow_, uniformUp_, deterministicVal_);
					sensorGuiDataList.add(addSensorGuiData);
				}
			}
		});

		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(okBtn);
		buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));
		buttonPanel.add(cancelBtn);
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

		return buttonPanel;
	}

	private JPanel createInputPanelArea() {
	    String[] distributionType = {"Normal", "Uniform", "Deterministic"};
 
        //Create and populate the panel.
        JPanel springPanel = new JPanel(new SpringLayout());
        springPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		
		JLabel lName = new JLabel("Name: ");
		springPanel.add(lName);
		sensorName = new JTextField();
		lName.setLabelFor(sensorName);
		springPanel.add(sensorName);
		
		JLabel lType = new JLabel("Type: ");
		springPanel.add(lType);
		sensorType = new JTextField();
		lType.setLabelFor(sensorType);
		springPanel.add(sensorType);
				
		JLabel distLabel = new JLabel("Distribution Type: ", JLabel.TRAILING);
		springPanel.add(distLabel);	
		distribution = new JComboBox(distributionType);
		distLabel.setLabelFor(distribution);
		distribution.setSelectedIndex(-1);
		distribution.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				JComboBox ctype = (JComboBox)e.getSource();
				String item = (String)ctype.getSelectedItem();
				updatePanel(item);				
			}
		});
		
		
		springPanel.add(distribution);		
		
		JLabel normalMeanLabel = new JLabel("Mean: ");
		springPanel.add(normalMeanLabel);	
		normalMean = new JTextField();
		normalMeanLabel.setLabelFor(normalMean);
		springPanel.add(normalMean);
		
		JLabel normalStdDevLabel = new JLabel("StdDev: ");
		springPanel.add(normalStdDevLabel);	
		normalStdDev = new JTextField();
		normalStdDevLabel.setLabelFor(normalStdDev);
		springPanel.add(normalStdDev);
		
		JLabel uniformLowLabel = new JLabel("Min: ");
		springPanel.add(uniformLowLabel);	
		uniformLowerBound = new JTextField();
		uniformLowLabel.setLabelFor(uniformLowerBound);
		springPanel.add(uniformLowerBound);
		
		JLabel uniformUpLabel = new JLabel("Max: ");
		springPanel.add(uniformUpLabel);	
		uniformUpperBound = new JTextField();
		uniformUpLabel.setLabelFor(uniformUpperBound);
		springPanel.add(uniformUpperBound);
		
		JLabel deterministicValueLabel = new JLabel("Value: ");
		springPanel.add(deterministicValueLabel);	
		deterministicValue = new JTextField();
		uniformLowLabel.setLabelFor(deterministicValue);
		springPanel.add(deterministicValue);		
						
       //Lay out the panel.
        SpringUtilities.makeCompactGrid(springPanel,
                                        8, 2,        //rows, columns
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
		return springPanel;
	}
	
    protected void updatePanel(String item) {
		switch(item){
		case "Normal":
			normalMean.setVisible(true);
			normalStdDev.setVisible(true);
			uniformLowerBound.setVisible(false);
			uniformUpperBound.setVisible(false);
			deterministicValue.setVisible(false);
			break;
		case "Uniform":
			normalMean.setVisible(false);
			normalStdDev.setVisible(false);
			uniformLowerBound.setVisible(true);
			uniformUpperBound.setVisible(true);
			deterministicValue.setVisible(false);
			break;
		case "Deterministic":
			normalMean.setVisible(false);
			normalStdDev.setVisible(false);
			uniformLowerBound.setVisible(false);
			uniformUpperBound.setVisible(false);
			deterministicValue.setVisible(true);
			break;
		default:
			break;
		}
		
	}

	public static void setUIFont (javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
          Object key = keys.nextElement();
          Object value = UIManager.get (key);
          if (value != null && value instanceof javax.swing.plaf.FontUIResource)
            UIManager.put (key, f);
          }
    }
    
	private void prompt(String msg, String type){
		JOptionPane.showMessageDialog(AddSensor.this, msg, type, JOptionPane.ERROR_MESSAGE);
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
