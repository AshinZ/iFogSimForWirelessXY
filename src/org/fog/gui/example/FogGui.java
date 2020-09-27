package org.fog.gui.example;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.fog.gui.core.*;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.core.CloudSim;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.Application;
import org.fog.application.selectivity.FractionalSelectivity;
import org.fog.entities.*;
import org.fog.gui.dialog.AddActuator;
import org.fog.gui.dialog.AddFogDevice;
import org.fog.gui.dialog.AddLink;
import org.fog.gui.dialog.AddPhysicalEdge;
import org.fog.gui.dialog.AddPhysicalNode;
import org.fog.gui.dialog.AddSensor;
import org.fog.gui.dialog.SDNRun;
import org.fog.placement.Controller;
import org.fog.placement.ModuleMapping;
import org.fog.placement.ModulePlacementEdgewards;
import org.fog.placement.ModulePlacementMapping;
import org.fog.utils.TimeKeeper;

// read me
//在原有基础上为了符合ifogsim的平台数据要求，我们重写了数据文件，因此原有的graph等都变成了gui专用数据
//真正传入到ifogsim运算的是我们重写的数据
//todo 有机会可以尝试把两个数据统一一下 直接在图上显示传入的数据格式
//

public class FogGui extends JFrame {
	private static final long serialVersionUID = -2238414769964738933L;
	
	private JPanel contentPane;
	
	/** Import file names */
	private String physicalTopologyFile = "";  //physical
	private String deploymentFile = "";        //virtual
	private String workloads_background = "";  //workload
	private String workloads = "";             //workload

	private JPanel panel;
	private JPanel graph;
	
	private Graph physicalGraph;
	//private Graph virtualGraph;
	private GraphView physicalCanvas;
	//private GraphView virtualCanvas;
	
	private JButton btnRun;
	
	private String mode;  //'m':manual; 'i':import
	private static List<FogDevice> fogDevices = new ArrayList<FogDevice>();
	private static List<Sensor> sensors = new ArrayList<Sensor>();
	private static List<Actuator> actuators = new ArrayList<Actuator>();
	private  List<FogDeviceGuiData> fogDeviceGuiDataList = new ArrayList<FogDeviceGuiData>();
	private  List<SensorGuiData> sensorGuiDataList = new ArrayList<SensorGuiData>();
	private  List<ActuatorGuiData> actuatorGuiDataList = new ArrayList<ActuatorGuiData>();
	private  List<LinkGuiData> linkGuiDataList = new ArrayList<LinkGuiData>();
	private static boolean CLOUD = false;

	static int numOfDepts = 2;
	static int numOfMobilesPerDept = 2;
	static double EEG_TRANSMISSION_TIME = 5.1;
	public FogGui() {
        setDefaultCloseOperation(EXIT_ON_CLOSE);  //退出时的操作
        setPreferredSize(new Dimension(1280, 800)); //设置大小
        setLocationRelativeTo(null);//设定位于图形的位置 null为在中间
        //setResizable(false);
        
        setTitle("iFogSim图形操作demo"); //设置图形标题
        contentPane = new JPanel(); //建立新的面板类容器
		setContentPane(contentPane); //获得面板
		contentPane.setLayout(new BorderLayout()); //设置布局模式
		
		initUI(); //调用initUI方法
		initGraph(); //调用initGraph方法
		//windows类方法
		pack();//windows方法 自适应大小
		setVisible(true); //设置为可见
	}
	
	public final void initUI() {
		setUIFont (new javax.swing.plaf.FontUIResource("Serif",Font.BOLD,18)); //设置字体

        panel = new JPanel(); //创建新的容器
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS)); //设置容器的布局
        
        graph = new JPanel(new java.awt.GridLayout(1, 2));  //网格布局
        
		initBar(); //设置菜单
		doPosition(); //放置窗口
	}
	
	/** position window */
	//根据系统屏幕大小设置位置
	private void doPosition() {

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); //获得屏幕大小
		int height = screenSize.height;
		int width = screenSize.width;

		int x = (width / 2 - 1280 / 2);
		int y = (height / 2 - 800 / 2);
		// One could use the dimension of the frame. But when doing so, one have to call this method !BETWEEN! pack and
		// setVisible. Otherwise the calculation will go wrong.

		this.setLocation(x, y);
	}
	//初始化系统菜单和工具按钮  可以认为是这个文件的核心函数
	/** Initialize project menu and tool bar */
    private final void initBar() {
    	//---------- Start ActionListener ----------
		//设置事件监听器
    	ActionListener readPhyTopoListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	physicalTopologyFile = importFile("json");
            	checkImportStatus();
		    }
		};
		ActionListener readVirTopoListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	deploymentFile = importFile("json");
            	checkImportStatus();
		    }
		};
		ActionListener readWorkloadBkListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	workloads_background = importFile("cvs");
		    	checkImportStatus();
		    }
		};
		ActionListener readWorkloadListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	workloads = importFile("cvs");
		    	checkImportStatus();
		    }
		};
		
		ActionListener addFogDeviceListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	openAddFogDeviceDialog();
		    }
		};
		
		ActionListener addPhysicalNodeListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	openAddPhysicalNodeDialog();
		    }
		};

		ActionListener addPhysicalEdgeListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	openAddPhysicalEdgeDialog();
		    }
		};
		
		ActionListener addLinkListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	openAddLinkDialog();
		    }
		};
		
		ActionListener addActuatorListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	openAddActuatorDialog();
		    }
		};
		
		ActionListener addSensorListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	openAddSensorDialog();
		    }
		};
		ActionListener importPhyTopoListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	String fileName = importFile("json");
		    	Graph phyGraph= Bridge.jsonToGraph(fileName, 0);
		    	physicalGraph = phyGraph;
		    	physicalCanvas.setGraph(physicalGraph);
		    	physicalCanvas.repaint();
		    }
		};
		
		ActionListener savePhyTopoListener = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {
		    	try {
					saveFile("json", physicalGraph);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
		    }
		};
				
		//---------- End ActionListener ----------
    	
        //---------- Start Creating project tool bar ----------
		//设置按钮栏
        JToolBar toolbar = new JToolBar();
        //设置图标
        ImageIcon iSensor = new ImageIcon(
                getClass().getResource("/images/sensor.png"));
        ImageIcon iActuator = new ImageIcon(
                getClass().getResource("/images/actuator.png"));
        ImageIcon iFogDevice = new ImageIcon(
                getClass().getResource("/images/dc.png"));
        ImageIcon iLink = new ImageIcon(
                getClass().getResource("/images/hline2.png"));
        ImageIcon iHOpen = new ImageIcon(
                getClass().getResource("/images/openPhyTop.png"));
        ImageIcon iHSave = new ImageIcon(
                getClass().getResource("/images/savePhyTop.png"));
        
        ImageIcon run = new ImageIcon(
                getClass().getResource("/images/play.png"));
        ImageIcon exit = new ImageIcon(
                getClass().getResource("/images/exit.png"));
        //建立按钮
        final JButton btnSensor = new JButton(iSensor); //建立按钮
        btnSensor.setToolTipText("添加传感器"); //设置鼠标放到按钮上的显示
        final JButton btnActuator = new JButton(iActuator);
        btnActuator.setToolTipText("添加触发器");
        
        final JButton btnFogDevice = new JButton(iFogDevice);
        btnFogDevice.setToolTipText("添加雾设备");
        final JButton btnLink = new JButton(iLink);
        btnLink.setToolTipText("添加连接");
        
        
        final JButton btnHopen = new JButton(iHOpen);
        btnHopen.setToolTipText("导入物理拓扑");
        final JButton btnHsave = new JButton(iHSave);
        btnHsave.setToolTipText("导出物理拓扑");

		JButton btnRun = new JButton(run);
        btnRun.setToolTipText("开始模拟");
        JButton btnExit = new JButton(exit);
        btnExit.setToolTipText("退出模拟");
        toolbar.setAlignmentX(0);
        
        //增加按钮监听
        btnSensor.addActionListener(addSensorListener);
        btnActuator.addActionListener(addActuatorListener);
        btnFogDevice.addActionListener(addFogDeviceListener);
        btnLink.addActionListener(addLinkListener);
        btnHopen.addActionListener(importPhyTopoListener);
        btnHsave.addActionListener(savePhyTopoListener);
        //运行函数
        btnRun.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
          /*  	if("i"==mode){  //import模式
            		if(physicalTopologyFile==null || physicalTopologyFile.isEmpty()){
            			JOptionPane.showMessageDialog(panel, "Please select physicalTopologyFile", "Error", JOptionPane.ERROR_MESSAGE);
            			return;
            		}
            		if(deploymentFile==null || deploymentFile.isEmpty()){
            			JOptionPane.showMessageDialog(panel, "Please select deploymentFile", "Error", JOptionPane.ERROR_MESSAGE);
            			return;
            		}
            		if(workloads_background==null || workloads_background.isEmpty()){
            			JOptionPane.showMessageDialog(panel, "Please select workloads_background", "Error", JOptionPane.ERROR_MESSAGE);
            			return;
            		}
            		if(workloads==null || workloads.isEmpty()){
            			JOptionPane.showMessageDialog(panel, "Please select workloads", "Error", JOptionPane.ERROR_MESSAGE);
            			return;
            		}
            		// run simulation
            		SDNRun run = new SDNRun(physicalTopologyFile, deploymentFile, 
            								workloads_background, workloads, FogGui.this);

		        }else if("m"==mode){ //manual模式
					if(physicalTopologyFile==null || physicalTopologyFile.isEmpty()){
						JOptionPane.showMessageDialog(panel, "Please select physicalTopologyFile", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(deploymentFile==null || deploymentFile.isEmpty()){
						JOptionPane.showMessageDialog(panel, "Please select deploymentFile", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(workloads_background==null || workloads_background.isEmpty()){
						JOptionPane.showMessageDialog(panel, "Please select workloads_background", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					if(workloads==null || workloads.isEmpty()){
						JOptionPane.showMessageDialog(panel, "Please select workloads", "Error", JOptionPane.ERROR_MESSAGE);
						return;
					}
					// run simulation
					SDNRun run = new SDNRun(physicalTopologyFile, deploymentFile,
							workloads_background, workloads, FogGui.this);

		        }*/
            	guiRun();
            }
        });
        btnExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }

        });       
	//在图形界面上增加按钮
        toolbar.add(btnSensor);
        toolbar.add(btnActuator);
        toolbar.add(btnFogDevice);
        toolbar.add(btnLink);
        toolbar.add(btnHopen);
        toolbar.add(btnHsave);
        
        toolbar.addSeparator();
        
        /*toolbar.add(btnSensorModule);
        toolbar.add(btnActuatorModule);
        toolbar.add(btnModule);
        toolbar.add(btnAppEdge);*/
        
        toolbar.addSeparator();
        
        toolbar.add(btnRun);
        toolbar.add(btnExit);

        panel.add(toolbar);
        
        contentPane.add(panel, BorderLayout.NORTH);
        //---------- End Creating project tool bar ----------
        
        
        //创建菜单
    	//---------- Start Creating project menu bar ----------
    	//1-1
        JMenuBar menubar = new JMenuBar();
        //ImageIcon iconNew = new ImageIcon(getClass().getResource("/src/new.png"));

        //2-1
        JMenu graph = new JMenu("图");
        graph.setMnemonic(KeyEvent.VK_G);
        
        //Graph by importing json and cvs files
        final JMenuItem MiPhy = new JMenuItem("物理拓扑");
        final JMenuItem MiVir = new JMenuItem("虚拟拓扑");
        final JMenuItem MiWl1 = new JMenuItem("负载背景");
        final JMenuItem MiWl2 = new JMenuItem("负载");
        //Graph drawing elements
        final JMenu MuPhy = new JMenu("物理");
        JMenuItem MiFogDevice = new JMenuItem("添加雾设备");
        JMenuItem MiPhyEdge = new JMenuItem("增加边缘");
        JMenuItem MiPhyOpen = new JMenuItem("导入物理拓扑");
        JMenuItem MiPhySave = new JMenuItem("导出物理拓扑");
        MuPhy.add(MiFogDevice);
        MuPhy.add(MiPhyEdge);
        MuPhy.add(MiPhyOpen);
        MuPhy.add(MiPhySave);

        //读取数据
        MiPhy.addActionListener(readPhyTopoListener);
        MiVir.addActionListener(readVirTopoListener);
        MiWl1.addActionListener(readWorkloadBkListener);
        MiWl2.addActionListener(readWorkloadListener);
        //监听
        MiFogDevice.addActionListener(addFogDeviceListener);
        MiPhyEdge.addActionListener(addPhysicalEdgeListener);
        MiPhyOpen.addActionListener(importPhyTopoListener);
        MiPhySave.addActionListener(savePhyTopoListener);

        graph.add(MuPhy);//加physical菜单
        //graph.add(MuVir);
        graph.add(MiPhy);
        //graph.add(MiVir);
        graph.add(MiWl1);
        graph.add(MiWl2);
        //view菜单
        //2-2
        JMenu view = new JMenu("功能");
        view.setMnemonic(KeyEvent.VK_F);

        JMenu module = new JMenu("模块关系");
		module.setMnemonic(KeyEvent.VK_E);

        //switch mode between manual mode (to create graph by hand) and import mode (to create graph from file)
		ActionListener actionSwitcher = new ActionListener() {
		    public void actionPerformed(ActionEvent e) {//canvas界面上部菜单 也即画图模式
		        try {
		    	    String cmd = e.getActionCommand();
		    	    if("图" == cmd){
		    	    	btnSensor.setVisible(true);
		    	    	btnActuator.setVisible(true);
		    	    	btnFogDevice.setVisible(true);
		    	    	btnLink.setVisible(true);
		    	    	btnHopen.setVisible(true);
		    	    	btnHsave.setVisible(true);
		    	    	
		    	    	MiPhy.setVisible(false);
		    	    	MiVir.setVisible(false);
		    	    	MiWl1.setVisible(false);
		    	    	MiWl2.setVisible(false);
		    	    	MuPhy.setVisible(true);
		    	    	//MuVir.setVisible(true);

                        btnExit.setVisible(false);
		    	    	btnRun.setVisible(false);
		    	    	btnRun.setEnabled(false);
		    	    	
		    	    	mode = "m";
		    	    	
		    	    }else if("执行" == cmd){ //运行模式下菜单
		    	    	btnSensor.setVisible(false);
		    	    	btnActuator.setVisible(false);
		    	    	btnFogDevice.setVisible(false);
		    	    	btnLink.setVisible(false);
		    	    	btnHopen.setVisible(false);
		    	    	btnHsave.setVisible(false);
		    	    	
		    	    	MiPhy.setVisible(true);
		    	    	MiVir.setVisible(true);
		    	    	MiWl1.setVisible(true);
		    	    	MiWl2.setVisible(true);
		    	    	MuPhy.setVisible(false);
		    	    	//MuVir.setVisible(false);
		    	    	
		    	    	btnRun.setVisible(true);
		    	    	btnRun.setEnabled(true);
		    	    	
		    	    	mode = "i";
		    	    }
		    	    //System.out.println(e.getActionCommand());
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		    }
		};
		//可以认为这里是去添加下拉菜单选项
		//加入canvas菜单
        JRadioButtonMenuItem manualMode = new JRadioButtonMenuItem("图");
        //设置快捷键
        manualMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
        //设置监听
        manualMode.addActionListener(actionSwitcher);
        //加入execution
        JRadioButtonMenuItem importMode = new JRadioButtonMenuItem("执行");
        //设置菜单位置
        importMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
        //设置监听
        importMode.addActionListener(actionSwitcher);

		// 建立下拉菜单组
        ButtonGroup group = new ButtonGroup();
        //加入刚才设置的两个按钮
        group.add(manualMode);
        group.add(importMode);

        //设置退出按钮
        JMenuItem fileExit = new JMenuItem("退出");
        fileExit.setMnemonic(KeyEvent.VK_C);
        fileExit.setToolTipText("退出模拟");
        fileExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		//设置监听
        fileExit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                System.exit(0);
            }

        });


		//加入canvas菜单
		JRadioButtonMenuItem moduleType1 = new JRadioButtonMenuItem("模块关系1");
		//设置快捷键
		manualMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_U, ActionEvent.CTRL_MASK));
		//设置监听
		manualMode.addActionListener(actionSwitcher);
		//加入execution
		JRadioButtonMenuItem moduleType2 = new JRadioButtonMenuItem("模块关系2");
		//设置菜单位置
		importMode.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_I, ActionEvent.CTRL_MASK));
		//设置监听
		importMode.addActionListener(actionSwitcher);

		// 建立下拉菜单组
		ButtonGroup moduleTypeGroup = new ButtonGroup();
		//加入刚才设置的两个按钮
		moduleTypeGroup.add(moduleType1);
		moduleTypeGroup.add(moduleType2);

		module.add(moduleType1);
		module.add(moduleType2);
        //给view添加下拉菜单
        view.add(manualMode);
        view.add(importMode);
        view.addSeparator();
        view.add(fileExit);        

        
        //3-1
		//添加上部菜单栏到容器中
        menubar.add(view);
        menubar.add(graph);
        menubar.add(module);

        //4-1
        setJMenuBar(menubar);
        //----- End Creating project menu bar -----
        
        
        
        //----- Start Initialize menu and tool bar -----
        manualMode.setSelected(true);
        mode = "m";
        
        //btnHost.setVisible(true);
        btnSensor.setVisible(true);
        btnActuator.setVisible(true);
        btnFogDevice.setVisible(true);
        btnLink.setVisible(true);
    	btnHopen.setVisible(true);
    	btnHsave.setVisible(true);
    	
    	MiPhy.setVisible(false);
    	MiVir.setVisible(false);
    	MiWl1.setVisible(false);
    	MiWl2.setVisible(false);
    	MuPhy.setVisible(true);
    	//MuVir.setVisible(true);
    	
    	btnRun.setVisible(false);
    	btnRun.setEnabled(false);
        //----- End Initialize menu and tool bar -----

    }

	protected void openAddActuatorDialog() {
		AddActuator actuator = new AddActuator(physicalGraph, FogGui.this  , actuators,actuatorGuiDataList);
		physicalCanvas.repaint();
        System.out.print("act个数："+actuatorGuiDataList.size()+"\n");
	}

    protected void openAddSensorDialog() {
        AddSensor sensor = new AddSensor(physicalGraph, FogGui.this  ,  sensors, sensorGuiDataList);
        physicalCanvas.repaint();
        System.out.print("sensor个数："+sensorGuiDataList.size()+"\n");
    }

	protected void openAddLinkDialog() {
		AddLink phyEdge = new AddLink(physicalGraph, FogGui.this  ,  fogDevices , sensors , actuators,linkGuiDataList);
    	physicalCanvas.repaint();
        System.out.print("link个数："+linkGuiDataList.size()+"\n");
	}

	protected void openAddFogDeviceDialog() {
		AddFogDevice fogDevice = new AddFogDevice(physicalGraph, FogGui.this , fogDevices,fogDeviceGuiDataList);
    	physicalCanvas.repaint();
	/*	for (int i = 0 ; i < fogDevices.size() ; ++ i){
            System.out.print("fogDevice输出\n");
            System.out.print(fogDevices.get(i).getRate());
		    System.out.print("\nfogDevice输出\n");
        }*/
	}

	/** initialize Canvas */
    private void initGraph(){
    	physicalGraph = new Graph();
    	//virtualGraph = new Graph();
    	
    	physicalCanvas = new GraphView(physicalGraph);
    	//virtualCanvas = new GraphView(virtualGraph);
    	
		graph.add(physicalCanvas);
		//graph.add(virtualCanvas);
		contentPane.add(graph, BorderLayout.CENTER);
    }
    
    
    /** dialog opening */
    private void openAddPhysicalNodeDialog(){
    	AddPhysicalNode phyNode = new AddPhysicalNode(physicalGraph, FogGui.this);
    	physicalCanvas.repaint();
    }
    private void openAddPhysicalEdgeDialog(){
    	AddPhysicalEdge phyEdge = new AddPhysicalEdge(physicalGraph, FogGui.this);
    	physicalCanvas.repaint();
    }


    
    /** common utility */
    //加载文件
    private String importFile(String type){
        JFileChooser fileopen = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter(type.toUpperCase()+" Files", type);
        fileopen.addChoosableFileFilter(filter);

        int ret = fileopen.showDialog(panel, "Import file");

        if (ret == JFileChooser.APPROVE_OPTION) {
            File file = fileopen.getSelectedFile();
            return file.getPath();
        }
        return "";
    }
    
    /** save network topology */
    //导出保存文件
    private void saveFile(String type, Graph graph) throws IOException{
    	JFileChooser fileopen = new JFileChooser();
        FileFilter filter = new FileNameExtensionFilter(type.toUpperCase()+" Files", type);
        fileopen.addChoosableFileFilter(filter);

        int ret = fileopen.showSaveDialog(panel);

        if (ret == JFileChooser.APPROVE_OPTION) {
        	//直接传入数据进行调用
        	String jsonText =Bridge.graphToJson(graph,fogDeviceGuiDataList,sensorGuiDataList ,actuatorGuiDataList, linkGuiDataList);
        	System.out.println(jsonText);
            String path = fileopen.getSelectedFile().toString();
            File file = new File(path);
    		FileOutputStream out = new FileOutputStream(file);
			out.write(jsonText.getBytes());
			out.close();
        }
    }
    
    private static void setUIFont(javax.swing.plaf.FontUIResource f){
        java.util.Enumeration keys = UIManager.getDefaults().keys();
        while (keys.hasMoreElements()) {
          Object key = keys.nextElement();
          Object value = UIManager.get (key);
          if (value != null && value instanceof javax.swing.plaf.FontUIResource)
            UIManager.put (key, f);
          }
    }
    
    private void checkImportStatus(){
    	if((physicalTopologyFile!=null && !physicalTopologyFile.isEmpty()) &&
    	   (deploymentFile!=null && !deploymentFile.isEmpty()) &&
           (workloads_background!=null && !workloads_background.isEmpty()) &&
    	   (workloads!=null && !workloads.isEmpty())){
    		btnRun.setEnabled(true);
    	}else{
    		btnRun.setEnabled(false);
    	}
    }
    
    
    
    /** Application entry point */
	public static void main(String args[]) throws InterruptedException {
		Log.disable();
		int num_user = 1; // number of cloud users
		Calendar calendar = Calendar.getInstance();
		boolean trace_flag = false; // mean trace events
		CloudSim.init(num_user, calendar, trace_flag);
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
            	FogGui sdn = new FogGui();
                sdn.setVisible(true);
            }
        });
	}

	private static void guiRun(){
		Log.printLine("Starting Simulation...");

		try {


			//CloudSim.init(num_user, calendar, trace_flag);

			String appId = "test_gui"; // identifier of the application

			FogBroker broker = new FogBroker("broker");

			Application application = createApplication(appId, broker.getId());
			application.setUserId(broker.getId());

			for(int i=0;i<fogDevices.size();++i){
			    fogDevices.get(i).showInfo();
            }
			System.out.print("\n");
			for(int i =0 ;i<sensors.size();++i){
			    System.out.print(sensors.get(i).getName());
                System.out.print(sensors.get(i).getId());
                System.out.print("\n");
				sensors.get(i).setUserId(broker.getId());
				sensors.get(i).setAppId(appId);
			}
            System.out.print("\n");
			for(int i=0;i<actuators.size();++i){
			    System.out.print(actuators.get(i).getName());
                System.out.print("\n");
				actuators.get(i).setUserId(broker.getId());
				actuators.get(i).setAppId(appId);
			}
            System.out.print("\n");
			ModuleMapping moduleMapping = ModuleMapping.createModuleMapping(); // initializing a module mapping
			if(CLOUD){
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

			Controller controller = new Controller("master-controller", fogDevices, sensors,
					actuators);


			controller.submitApplication(application, 0,
					(CLOUD)?(new ModulePlacementMapping(fogDevices, application, moduleMapping))
							:(new ModulePlacementEdgewards(fogDevices, sensors, actuators, application, moduleMapping)));

			TimeKeeper.getInstance().setSimulationStartTime(Calendar.getInstance().getTimeInMillis());

			CloudSim.startSimulation();

			CloudSim.stopSimulation();

			Log.printLine("finished!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.printLine("Unwanted errors happen");
		}
	}


	private static Application createApplication(String appId, int userId){

		Application application = Application.createApplication(appId, userId); // creates an empty application model (empty directed graph)

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
}
