package ch.compass.gonzoproxy.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import ch.compass.gonzoproxy.controller.RelayController;
import javax.swing.JCheckBox;

public class NewSessionDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7042508576478156546L;
	private JPanel contentPane;
	private JTextField textFieldPortListen;
	private JTextField textFieldForwardIP;
	private JTextField textFieldForwardPort;
	private JComboBox comboBoxMode;

	private RelayController controller;
	private JCheckBox chckbxRunLocally;
	private JButton btnStart;

	public NewSessionDialog(RelayController controller) {
		this.controller = controller;
		initGui();
		loadFields();
	}
	

	private void loadFields() {
		textFieldPortListen.setText(Integer.toString(controller
				.getCurrentListenPort()));
		textFieldForwardIP.setText(controller.getCurrentRemoteHost());
		textFieldForwardPort.setText(Integer.toString(controller
				.getCurrentRemotePort()));
		enableCheckbox(controller.getPossibleRelayModes()[0]);
	}

	private void initGui() {
		setResizable(false);
		setBounds(100, 100, 294, 176);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 110, 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 0.0, 0.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0,
				Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);

		JLabel lblSelectInputMethod = new JLabel("Select input method: ");
		lblSelectInputMethod.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblSelectInputMethod = new GridBagConstraints();
		gbc_lblSelectInputMethod.insets = new Insets(0, 0, 5, 5);
		gbc_lblSelectInputMethod.anchor = GridBagConstraints.WEST;
		gbc_lblSelectInputMethod.gridx = 0;
		gbc_lblSelectInputMethod.gridy = 0;
		contentPane.add(lblSelectInputMethod, gbc_lblSelectInputMethod);

		comboBoxMode = new JComboBox();
		comboBoxMode.setModel(new DefaultComboBoxModel(controller
				.getPossibleRelayModes()));
		comboBoxMode.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				enableCheckbox((String) comboBoxMode.getSelectedItem());
			}
		});
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.insets = new Insets(0, 0, 5, 0);
		gbc_comboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 0;
		contentPane.add(comboBoxMode, gbc_comboBox);

		JLabel lblListen = new JLabel("Listen on port:");
		lblListen.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblListen = new GridBagConstraints();
		gbc_lblListen.anchor = GridBagConstraints.WEST;
		gbc_lblListen.insets = new Insets(0, 0, 5, 5);
		gbc_lblListen.gridx = 0;
		gbc_lblListen.gridy = 1;
		contentPane.add(lblListen, gbc_lblListen);

		textFieldPortListen = new JTextField();
		textFieldPortListen.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldPortListen.setText("1234");
		GridBagConstraints gbc_textFieldPortListen = new GridBagConstraints();
		gbc_textFieldPortListen.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldPortListen.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldPortListen.gridx = 1;
		gbc_textFieldPortListen.gridy = 1;
		contentPane.add(textFieldPortListen, gbc_textFieldPortListen);
		textFieldPortListen.setColumns(10);

		JLabel lblForwardHost = new JLabel("Forward to IP: ");
		GridBagConstraints gbc_lblForwardHost = new GridBagConstraints();
		gbc_lblForwardHost.anchor = GridBagConstraints.WEST;
		gbc_lblForwardHost.insets = new Insets(0, 0, 5, 5);
		gbc_lblForwardHost.gridx = 0;
		gbc_lblForwardHost.gridy = 2;
		contentPane.add(lblForwardHost, gbc_lblForwardHost);

		textFieldForwardIP = new JTextField();
		textFieldForwardIP.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldForwardIP.setText("127.0.0.1");
		GridBagConstraints gbc_textFieldForwardIP = new GridBagConstraints();
		gbc_textFieldForwardIP.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldForwardIP.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldForwardIP.gridx = 1;
		gbc_textFieldForwardIP.gridy = 2;
		contentPane.add(textFieldForwardIP, gbc_textFieldForwardIP);
		textFieldForwardIP.setColumns(10);

		JLabel lblForwardToPort = new JLabel("Forward to port: ");
		GridBagConstraints gbc_lblForwardToPort = new GridBagConstraints();
		gbc_lblForwardToPort.anchor = GridBagConstraints.WEST;
		gbc_lblForwardToPort.insets = new Insets(0, 0, 5, 5);
		gbc_lblForwardToPort.gridx = 0;
		gbc_lblForwardToPort.gridy = 3;
		contentPane.add(lblForwardToPort, gbc_lblForwardToPort);

		textFieldForwardPort = new JTextField();
		textFieldForwardPort.setHorizontalAlignment(SwingConstants.RIGHT);
		textFieldForwardPort.setText("1234");
		GridBagConstraints gbc_textFieldForwardPort = new GridBagConstraints();
		gbc_textFieldForwardPort.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldForwardPort.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldForwardPort.gridx = 1;
		gbc_textFieldForwardPort.gridy = 3;
		contentPane.add(textFieldForwardPort, gbc_textFieldForwardPort);
		textFieldForwardPort.setColumns(10);

		btnStart = new JButton("Start new session");
		this.getRootPane().setDefaultButton(btnStart);
		btnStart.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				NewSessionDialog.this.dispose();
				String portListen = textFieldPortListen.getText();
				String remoteHost = textFieldForwardIP.getText();
				String remotePort = textFieldForwardPort.getText();
				String mode = (String) comboBoxMode.getSelectedItem();
				if (chckbxRunLocally.isSelected()) {
					controller.newSession("1234", "localhost", "4321", mode,
							chckbxRunLocally.isSelected());
				} else {
					controller.newSession(portListen, remoteHost, remotePort,
							mode, chckbxRunLocally.isSelected());
				}
			}
		});

		chckbxRunLocally = new JCheckBox("run locally");
		chckbxRunLocally
				.setToolTipText("<html>Runs command: socat TCP:localhost:1234 \"EXEC:nfc-relay-picc -t,fdin=3,fdout=4\"<br>" +
						"and: socat TCP-LISTEN:4321 \"EXEC:nfc-relay-picc -i,fdin=3,fdout=4\"");
		chckbxRunLocally.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				if (chckbxRunLocally.isSelected()) {
					textFieldForwardIP.setEnabled(false);
					textFieldForwardPort.setEnabled(false);
					textFieldPortListen.setEnabled(false);
				} else {
					textFieldForwardIP.setEnabled(true);
					textFieldForwardPort.setEnabled(true);
					textFieldPortListen.setEnabled(true);
				}
			}
		});
		GridBagConstraints gbc_chckbxRunLocally = new GridBagConstraints();
		gbc_chckbxRunLocally.anchor = GridBagConstraints.WEST;
		gbc_chckbxRunLocally.insets = new Insets(0, 0, 0, 5);
		gbc_chckbxRunLocally.gridx = 0;
		gbc_chckbxRunLocally.gridy = 4;
		contentPane.add(chckbxRunLocally, gbc_chckbxRunLocally);
		GridBagConstraints gbc_btnStart = new GridBagConstraints();
		gbc_btnStart.gridx = 1;
		gbc_btnStart.gridy = 4;
		contentPane.add(btnStart, gbc_btnStart);

		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("New session");
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		setSize(338, 173);
	}

	protected void enableCheckbox(String selectedItem) {
		if (selectedItem.equals("libnfc")) {
			chckbxRunLocally.setEnabled(true);
		} else {
			chckbxRunLocally.setEnabled(false);
			chckbxRunLocally.setSelected(false);
		}
	}

}
