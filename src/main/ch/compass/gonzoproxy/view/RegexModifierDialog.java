package ch.compass.gonzoproxy.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import ch.compass.gonzoproxy.relay.modifier.FieldRule;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTextField;
import javax.swing.JButton;

public class RegexModifierDialog extends JDialog {

	private static final long serialVersionUID = 9047578530331858262L;
	private JPanel contentPane;
	protected PacketRule editPacketRule;
	protected FieldRule editFieldRule;
	private JTextField textFieldRegex;
	private JTextField textFieldReplace;
	private JPanel panel;
	private JButton btnSaveAndClose;
	private JButton btnCancel;

	public RegexModifierDialog() {
		initGui();
	}

	private void initGui() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Pre-Parser Modifier");
		setBounds(100, 100, 460, 130);
		setMinimumSize(new Dimension(460,130));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblFindPatternjava = new JLabel("Find pattern (Java regex): ");
		GridBagConstraints gbc_lblFindPatternjava = new GridBagConstraints();
		gbc_lblFindPatternjava.anchor = GridBagConstraints.EAST;
		gbc_lblFindPatternjava.insets = new Insets(0, 0, 5, 5);
		gbc_lblFindPatternjava.gridx = 0;
		gbc_lblFindPatternjava.gridy = 0;
		contentPane.add(lblFindPatternjava, gbc_lblFindPatternjava);
		
		textFieldRegex = new JTextField();
		GridBagConstraints gbc_textFieldRegex = new GridBagConstraints();
		gbc_textFieldRegex.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldRegex.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldRegex.gridx = 1;
		gbc_textFieldRegex.gridy = 0;
		contentPane.add(textFieldRegex, gbc_textFieldRegex);
		textFieldRegex.setColumns(10);
		
		JLabel lblReplaceWith = new JLabel("Replace with: ");
		GridBagConstraints gbc_lblReplaceWith = new GridBagConstraints();
		gbc_lblReplaceWith.insets = new Insets(0, 0, 5, 5);
		gbc_lblReplaceWith.anchor = GridBagConstraints.WEST;
		gbc_lblReplaceWith.gridx = 0;
		gbc_lblReplaceWith.gridy = 1;
		contentPane.add(lblReplaceWith, gbc_lblReplaceWith);
		
		textFieldReplace = new JTextField();
		GridBagConstraints gbc_textFieldReplace = new GridBagConstraints();
		gbc_textFieldReplace.insets = new Insets(0, 0, 5, 0);
		gbc_textFieldReplace.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFieldReplace.gridx = 1;
		gbc_textFieldReplace.gridy = 1;
		contentPane.add(textFieldReplace, gbc_textFieldReplace);
		textFieldReplace.setColumns(10);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.EAST;
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 2;
		contentPane.add(panel, gbc_panel);
		
		btnSaveAndClose = new JButton("Save and Close");
		panel.add(btnSaveAndClose);
		
		btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		panel.add(btnCancel);

	}

}
