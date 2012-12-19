package ch.compass.gonzoproxy.view;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableColumn;

import ch.compass.gonzoproxy.controller.RelayController;
import ch.compass.gonzoproxy.model.modifier.PacketRegex;
import ch.compass.gonzoproxy.model.ui.RegexTableModel;

public class RegexDialog extends JDialog {

	private static final long serialVersionUID = 9047578530331858262L;
	private JPanel contentPane;
	private JTable tableRegex;
	private RegexTableModel regexTableModel;
	private PacketRegex dummyRegex;
	private PacketRegex editRegex;
	private ArrayList<PacketRegex> packetRegex;
	private RelayController controller;
	private JPanel panel;
	private JButton btnAddNewRegex;
	private JButton btnDeleteSelectedRegex;
	private JButton btnSaveAndClose;

	public RegexDialog(RelayController controller) {
		this.controller = controller;
		this.packetRegex = controller.getPacketRegex();
		this.dummyRegex = new PacketRegex("","");
		initGui();
		configureTable(tableRegex);
	}

	private void initGui() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setResizable(true);
		setModalityType(ModalityType.APPLICATION_MODAL);
		setTitle("Regex modifier pre-parse");
		setBounds(100, 100, 850, 225);
		setMinimumSize(new Dimension(780, 150));
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[] { 0, 0 };
		gbl_contentPane.rowHeights = new int[] { 0, 0, 0 };
		gbl_contentPane.columnWeights = new double[] { 1.0,
				Double.MIN_VALUE };
		gbl_contentPane.rowWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		contentPane.setLayout(gbl_contentPane);


		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		contentPane.add(scrollPane, gbc_scrollPane);

		regexTableModel = new RegexTableModel(packetRegex);
		tableRegex = new JTable(regexTableModel);
		tableRegex.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				JTable target = (JTable) e.getSource();
				
				int row = target.getSelectedRow();
				if (row == -1) {
					setEditRegex(dummyRegex);
				} else {
					setEditRegex(packetRegex.get(row));
				}
				if (e.getClickCount() == 2 && row != -1) {
					EditRegexDialog erd = new EditRegexDialog(editRegex, regexTableModel, controller);
					erd.setVisible(true);
				}
			}
		});
		scrollPane.setViewportView(tableRegex);
		
		panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.anchor = GridBagConstraints.EAST;
		gbc_panel.fill = GridBagConstraints.VERTICAL;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 1;
		contentPane.add(panel, gbc_panel);
		panel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		btnAddNewRegex = new JButton("Add new Regex");
		btnAddNewRegex.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				AddNewRegexDialog erd = new AddNewRegexDialog(new PacketRegex("", ""), controller, regexTableModel);
				erd.setVisible(true);
			}
		});
		panel.add(btnAddNewRegex);
		
		btnDeleteSelectedRegex = new JButton("Delete selected Regex");
		btnDeleteSelectedRegex.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				packetRegex.remove(editRegex);
				regexTableModel.regexChanged();
				controller.persistRegex();
			}
		});
		panel.add(btnDeleteSelectedRegex);
		
		btnSaveAndClose = new JButton("Save and close");
		btnSaveAndClose.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				controller.persistRegex();
				dispose();
			}
		});
		panel.add(btnSaveAndClose);
	}

	private void setEditRegex(PacketRegex editRegex) {
		this.editRegex = editRegex;
	}

	private void configureTable(JTable table) {
		table.setSelectionMode(0);
		table.getTableHeader().setReorderingAllowed(false);
		Enumeration<TableColumn> a = table.getColumnModel().getColumns();
		for (int i = 0; a.hasMoreElements(); i++) {
			TableColumn tb = (TableColumn) a.nextElement();
			switch (i) {
			case 0:
				tb.setPreferredWidth(100);
				break;
			case 1:				
				tb.setPreferredWidth(100);
				break;
			case 2:
				tb.setMinWidth(50);
				tb.setMaxWidth(50);
				break;
			}

		}
	}
	
}
