package uni.fmi.masters.gui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

import uni.fmi.masters.agent.BuyerAgent;

public class BuyerAgentGUI extends JFrame {

	BuyerAgent agent;
	
	public BuyerAgentGUI(BuyerAgent agent){
		
		JButton buttonSearch = new JButton ("search");
		JTextField ingridientName = new JTextField();
		
buttonSearch.addActionListener(new ActionListener() {
			
	//търсене на торта
			@Override
			public void actionPerformed(ActionEvent e) {
				String toppingName = ingridientName.getText();
				agent.searchedTopping = toppingName;			
			}
		});
		
		
		Box content = Box.createHorizontalBox();
		content.add(Box.createRigidArea(new Dimension(5,1)));
		content.add(ingridientName);
		content.add(Box.createRigidArea(new Dimension(5,1)));
		content.add(buttonSearch);
		
		add(content);
		
				
		setSize(400, 150);
		setVisible(true);
	}	
}
