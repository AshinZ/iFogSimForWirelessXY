package org.fog.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.UIManager;

import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.entities.FogDevice;
import org.fog.entities.FogDeviceCharacteristics;
import org.fog.gui.core.FogDeviceGui;
import org.fog.gui.core.Graph;
import org.fog.gui.core.SpringUtilities;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.FogLinearPowerModel;
import org.fog.utils.FogUtils;

@SuppressWarnings({ "rawtypes" })
public class AddFogDevice extends JDialog {
	private static final long serialVersionUID = -5116677861770319577L;
	
	private final Graph graph;
	List<FogDevice> fogDevices = new ArrayList<FogDevice>();


	private JLabel deviceNameLabel;
	private JLabel upBwLabel;
	private JLabel downBwLabel;
	private JLabel mipsLabel;
	private JLabel ramLabel;
	private JLabel levelLabel;
	private JLabel rateLabel;
	
	private JTextField deviceName;
	private JTextField upBw;
	private JTextField downBw;
	private JTextField mips;
	private JTextField ram;
	private JTextField level;
	private JTextField rate;


	public AddFogDevice(final Graph graph, final JFrame frame, List<FogDevice> fogDevices) {
		this.graph = graph;
		this.fogDevices = fogDevices;
		setLayout(new BorderLayout());

		add(createInputPanelArea(), BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.PAGE_END);
		// show dialog
		setTitle("添加雾设备");
		setModal(true);
		setPreferredSize(new Dimension(350, 380));
		setResizable(false);
		pack();
		setLocationRelativeTo(frame);
		setVisible(true);

	}

	//操作部分和数据控制
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
				if (deviceName.getText() == null || deviceName.getText().length() < 1) {
					prompt("Please type VM name", "Error");
				} else if (upBw.getText() == null || upBw.getText().length() < 1) {
					prompt("Please enter uplink BW", "Error");				
				} else if (downBw.getText() == null || downBw.getText().length() < 1) {
					prompt("Please enter downlink BW", "Error");				
				} else if (mips.getText() == null || mips.getText().length() < 1) {
					prompt("Please enter MIPS", "Error");				
				} else if (ram.getText() == null || ram.getText().length() < 1) {
					prompt("Please enter RAM", "Error");				
				} else if (level.getText() == null || level.getText().length() < 1) {
					prompt("Please enter Level", "Error");
				} else if (rate.getText() == null || rate.getText().length() < 1) {
					prompt("Please enter Rate", "Error");
				}
				long upBw_=-1, downBw_=-1, mips_=-1; int ram_=-1, level_=-1;double rate_ = -1;
				try {
					upBw_ = Long.parseLong(upBw.getText());
					downBw_ = Long.parseLong(downBw.getText());
					mips_ = Long.parseLong(mips.getText());
					ram_ = Integer.parseInt(ram.getText());
					level_ = Integer.parseInt(level.getText());
					rate_ = Double.parseDouble(rate.getText());
					catchedError = false;
				} catch (NumberFormatException e1) {
					catchedError = true;
					prompt("Input should be numerical character", "Error");
				}
				if(!catchedError){
					FogDeviceGui fogDevice = new FogDeviceGui(deviceName.getText().toString(), mips_, ram_, upBw_, downBw_, level_, rate_);
					graph.addNode(fogDevice);
					setVisible(false);
					//todo: busyPower and idlepower havn't been setted
					//the name in list can't be dynamic changed
					FogDevice addedfogdevide = createFogDevice(deviceName.getText().toString(), mips_, ram_, upBw_, downBw_, level_, rate_, 87.53, 82.44);
					fogDevices.add(addedfogdevide);
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

	//输入框
	private JPanel createInputPanelArea() { 
        //Create and populate the panel.
        JPanel springPanel = new JPanel(new SpringLayout());
        //springPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));		
        
		deviceNameLabel = new JLabel("Name: ");
		springPanel.add(deviceNameLabel);
		deviceName = new JTextField();
		deviceNameLabel.setLabelFor(deviceName);
		springPanel.add(deviceName);
		
		levelLabel = new JLabel("Level: ");
		springPanel.add(levelLabel);
		level = new JTextField();
		levelLabel.setLabelFor(level);
		springPanel.add(level);
		
		upBwLabel = new JLabel("Uplink Bw: ");
		springPanel.add(upBwLabel);
		upBw = new JTextField();
		upBwLabel.setLabelFor(upBw);
		springPanel.add(upBw);
		
		downBwLabel = new JLabel("Downlink Bw: ");
		springPanel.add(downBwLabel);
		downBw = new JTextField();
		downBwLabel.setLabelFor(downBw);
		springPanel.add(downBw);
		
		/** switch and host  */
		
		mipsLabel = new JLabel("Mips: ");
		springPanel.add(mipsLabel);	
		mips = new JTextField();
		mipsLabel.setLabelFor(mips);
		springPanel.add(mips);		
		
		ramLabel = new JLabel("Ram: ");
		springPanel.add(ramLabel);
		ram = new JTextField();
		ramLabel.setLabelFor(ram);
		springPanel.add(ram);
		
		rateLabel = new JLabel("Rate/MIPS: ");
		springPanel.add(rateLabel);
		rate = new JTextField();
		rateLabel.setLabelFor(rate);
		springPanel.add(rate);

		
        //Lay out the panel.
        SpringUtilities.makeCompactGrid(springPanel,
                                        7, 2,        //rows, cols
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
        //updatePanel("core");
		return springPanel;
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
		JOptionPane.showMessageDialog(AddFogDevice.this, msg, type, JOptionPane.ERROR_MESSAGE);
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
