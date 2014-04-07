package jPhone;

import java.io.*;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

/**
 * this class is the GUI of the main window
 * @author terry
 *
 */
public class JPhone extends JFrame implements ActionListener, WindowListener
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;  // to suppress warning

	public static final int RTP_PORT = 15000;
	
	/**
	 * the message shown on the GUI when the participant list is empty
	 */
	private final String NULL_PARTICIPANT = "empty participant list";
	/**
	 * the command name associated with the "+" button
	 */
	private final String COMMAND_ADD = "add";
	/**
	 * the command name associated with the "-" button
	 */
	private final String COMMAND_REMOVE = "remove";
	/**
	 * the command name associated with the "call" button
	 */
	private final String COMMAND_CALL = "call";	
	/**
	 * the list auto saver, please refer to the ListSaver class
	 */
	private ListSaver saver;
	/**
	 * the window which shows when calling, please refer to the JPhoneCall class
	 */
	private JPhoneCall callPanel;
	/**
	 * the call status manager, please refer to the class JPhoneStatus
	 */
	private JPhoneStatus phoneStatus;	
	/**
	 * container of the participant list
	 */
	private Vector<String> list;
	
	
	/**
	 * GUI control for the participant list
	 */
	private JList participantList;
	/**
	 * the input text field for the IP address of a new participant
	 */
	private JTextField participantIPAddr;
	/**
	 * the "+" button
	 */
	private JButton addButton;
	/**
	 * the "-" button
	 */
	private JButton removeButton;
	/**
	 * the "call" button
	 */
	private JButton callButton;
	
	/**
	 * this function check if the input IP address is correct
	 * @param IPAddr
	 * @return correct or not
	 */
	public static boolean checkIPAddr(String IPAddr)
	{
		return IPAddr.matches("^[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}$");
	}
	
	/**
	 * this is the main entry of the whole program
	 * @param args
	 */
	public static void main(String[] args)
	{
		JPhone phone = new JPhone();
	}
	
	/**
	 * get the current participant list
	 * @return the current participant list
	 */
	public final Vector<String> getParticipantList()
	{
		return list;
	}
	
	/**
	 * get the call status manager
	 * @return the call status manager
	 */
	public JPhoneStatus getPhoneStatus()
	{
		return phoneStatus;
	}

	/**
	 * this is the constructor of class JPhone
	 */
	public JPhone()
	{		
		// init its super class JFrame
		super();
		
		callPanel = new JPhoneCall(this);
		phoneStatus = new JPhoneStatus(this, callPanel);
		phoneStatus.start();		
		saver = new ListSaver();

		// load the stored participant list
		list = saver.readList();
		if(list.size() == 0) list.add(NULL_PARTICIPANT); 
		participantList = new JList();
		participantList.setListData(list);
		participantList.setLayoutOrientation(JList.VERTICAL);

		// create the GUI
		participantIPAddr = new JTextField();
		participantIPAddr.setColumns(20);
		participantIPAddr.setText("You need to add your IP address 1st!");

		addButton = new JButton("Add");
		addButton.setActionCommand(COMMAND_ADD);
		addButton.addActionListener(this);
		
		removeButton = new JButton("Remove");
		removeButton.setActionCommand(COMMAND_REMOVE);
		removeButton.addActionListener(this);

		callButton = new JButton("Call");		
		callButton.setActionCommand(COMMAND_CALL);
		callButton.addActionListener(this);

		// draw the GUI
		Container c = this.getContentPane();
		c.setLayout(new BorderLayout());		
		c.add(participantList, BorderLayout.NORTH);		
		c.add(participantIPAddr, BorderLayout.WEST);
		c.add(addButton, BorderLayout.CENTER);
		c.add(removeButton, BorderLayout.EAST);
		c.add(callButton, BorderLayout.SOUTH);				
		this.pack();
		
		// put the window at the center of the desktop
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		Dimension screenSize = toolkit.getScreenSize();
		int x = (screenSize.width - this.getWidth()) / 2;
		int y = (screenSize.height - this.getHeight()) / 2;
		this.setLocation(x, y);
		this.setResizable(false);
		this.setTitle("jPhone");
		this.addWindowListener(this);
		this.setVisible(true);
	}

	/**
	 * this method is called whenever any button is pushed
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e)
	{
		// if the pushed button is the "+" button
		if(e.getActionCommand().equals(COMMAND_ADD))
		{
			String newIPAddr = participantIPAddr.getText();
			
			// check if a correct IP address?
			if(!checkIPAddr(newIPAddr))
			{
				JOptionPane.showMessageDialog(null, "Incorrect IP Address", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// check if the input IP address has been in the current list?
			boolean existed = false;
			for(String IPAddr : list)
			{
				if(IPAddr.equals(newIPAddr))
				{
					existed = true;
					break;
				}
			}
			if(existed)
			{
				JOptionPane.showMessageDialog(null, "The participant has been existed.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			// add the input IP address into current list and redraw the GUI
			if(list.size() == 1 && list.get(0).equals(NULL_PARTICIPANT)) list.clear();
			list.add(newIPAddr);
			participantList.setListData(list);
			this.pack();
		}
		// if the pushed button is the "-" button
		else if(e.getActionCommand().equals(COMMAND_REMOVE))
		{
			// if no one is selected
			if(participantList.getSelectedValue() == null)
			{
				JOptionPane.showMessageDialog(null, "No one is selected to be removed.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// get the selected IP address
			String removeIPAddr = participantList.getSelectedValue().toString();
			// if the participant list is empty
			if(removeIPAddr.equals(NULL_PARTICIPANT))
			{
				JOptionPane.showMessageDialog(null, "No one is selected to be removed.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			// else, remove the selected IP address from the current participant list
			list.remove(removeIPAddr);
			if(list.size() == 0) list.add(NULL_PARTICIPANT);
			// redraw the GUI
			participantList.setListData(list);
			this.pack();
		}
		// if the pushed button is the "call" button
		else if(e.getActionCommand().equals(COMMAND_CALL))
		{
			// if the participant list is empty
			if(list.size() == 1 && list.get(0).equals(NULL_PARTICIPANT))
			{
				JOptionPane.showMessageDialog(null, "Empty participant list.", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			
			// important: probe each participant in the list to see whether they are idle now
			for(int i = 0; i < list.size(); i++)  // iterate each participant
			{	
				
				if(i == 0) continue;
				String remoteAddr = list.get(i);
				int remoteStatus = JPhoneStatus.PHONE_STATUS_ERROR; // init the result (pessimistically)
				try
				{
					remoteStatus = JPhoneStatus.probe(remoteAddr); // probe it
				}
				catch(IOException ex)
				{
					// fail to probe, e.g., timeout
					JOptionPane.showMessageDialog(null, "Fail to probe " + remoteAddr, "Error", JOptionPane.ERROR_MESSAGE);
					return;
				}
				// check the probe result
				switch(remoteStatus)
				{
				// if the participant is idle
				case JPhoneStatus.PHONE_STATUS_IDLE: break;
				// if the participant is in call, break my call operation
				case JPhoneStatus.PHONE_STATUS_SESSION:
					JOptionPane.showMessageDialog(null, remoteAddr + " is in session.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				// if any error occurs
				case JPhoneStatus.PHONE_STATUS_ERROR:
					JOptionPane.showMessageDialog(null, remoteAddr + " is in error status.", "Error", JOptionPane.ERROR_MESSAGE);
					return;
				// unknown errors
				default:
					System.err.println("Unknown network error when probing " + remoteAddr);
					System.exit(1);
					break;
				}
			}
			
			
			// transmit my list to each participant
			JPhoneStatus.sendParticipantList(list);
			
			// set the main window non-visible
			this.setVisible(false);
			// set my phone status to in-session
			phoneStatus.setStatus(JPhoneStatus.PHONE_STATUS_SESSION);
			// start the call
			callPanel.startCall(new Vector<String>(list));
		}
	}	

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * when the main window is closing, we need to save the current participant list to file
	 * @see java.awt.event.WindowListener#windowClosing(java.awt.event.WindowEvent)
	 */
	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
		// dispose the call panel
		callPanel.dispose();
		// save the current list to file
		saver.writeList(list);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub		
	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}
}
