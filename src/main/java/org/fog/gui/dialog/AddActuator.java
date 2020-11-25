package org.fog.gui.dialog;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
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

import org.fog.entities.Actuator;
import org.fog.gui.core.ActuatorGui;
import org.fog.gui.core.ActuatorGuiData;
import org.fog.gui.core.Graph;
import org.fog.gui.core.SpringUtilities;

@SuppressWarnings({ "rawtypes" })
public class AddActuator extends JDialog {
	private static final long serialVersionUID = -511667786177319577L;
	
	private final Graph graph;
	List<Actuator> actuators = new ArrayList<Actuator>();
	List<ActuatorGuiData> actuatorGuiDataList = new ArrayList<ActuatorGuiData>();
	private JTextField actuatorName;
	private JTextField actuatorType;
	
	/**
	 * Constructor.
	 * 
	 * @param frame the parent frame
	 */
	public AddActuator(final Graph graph, final JFrame frame , List<Actuator> actuators, List <ActuatorGuiData> actuatorGuiDataList) {
		this.graph = graph;
		this.actuators = actuators ;
		this.actuatorGuiDataList = actuatorGuiDataList;
		setLayout(new BorderLayout());

		add(createInputPanelArea(), BorderLayout.CENTER);
		add(createButtonPanel(), BorderLayout.PAGE_END);
		// show dialog
		setTitle("Add Actuator");
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
				if (actuatorName.getText() == null || actuatorName.getText().length() < 1) {
					prompt("Please type Actuator name", "Error");
				} else {
					if(!catchedError){
						ActuatorGui actuator = new ActuatorGui(actuatorName.getText(), 
								actuatorType.getText().toString());
							graph.addNode(actuator);
							setVisible(false);
					}
					Actuator newActuator = new Actuator(actuatorName.getText(), -1, "test", actuatorType.getText());
					actuators.add(newActuator);
					ActuatorGuiData addActuatorGuiData = new ActuatorGuiData(actuatorName.getText(),actuatorType.getText());
					actuatorGuiDataList.add(addActuatorGuiData);
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
        //Create and populate the panel.
        JPanel springPanel = new JPanel(new SpringLayout());
        springPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
		
		JLabel lName = new JLabel("Name: ");
		springPanel.add(lName);
		actuatorName = new JTextField();
		lName.setLabelFor(actuatorName);
		springPanel.add(actuatorName);
		
		JLabel lType = new JLabel("Actuator Type : ");
		springPanel.add(lType);
		actuatorType = new JTextField();
		lName.setLabelFor(actuatorType);
		springPanel.add(actuatorType);
		
							
       //Lay out the panel.
        SpringUtilities.makeCompactGrid(springPanel,
                                        2, 2,        //rows, columns
                                        6, 6,        //initX, initY
                                        6, 6);       //xPad, yPad
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
		JOptionPane.showMessageDialog(AddActuator.this, msg, type, JOptionPane.ERROR_MESSAGE);
	}
}
