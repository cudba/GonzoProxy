package ch.compass.gonzoproxy.mvc.view;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableColumn;

import ch.compass.gonzoproxy.mvc.controller.RelayController;
import ch.compass.gonzoproxy.mvc.model.DetailTableModel;
import ch.compass.gonzoproxy.mvc.model.Field;
import ch.compass.gonzoproxy.mvc.model.Packet;

public class ApduDetailPanel extends JPanel {
	private JTable table_detail;

	private static final long serialVersionUID = -5129650768327234148L;

	private JTextPane textPane_ascii;
	private JTextPane textPane_hex;
	private Packet editApdu;
	private RelayController controller;

	private DetailTableModel detailTableModel;

	private Field editField;

	public ApduDetailPanel(RelayController controller) {
		this.controller = controller;
		this.editApdu = new Packet(new byte[0]);
		this.editField = new Field();
		this.detailTableModel = new DetailTableModel(editApdu,
				controller.getSessionModel());
		initGui();

	}

	private void initGui() {
		setBorder(new TitledBorder(null, "APDU Detail", TitledBorder.LEADING,
				TitledBorder.TOP, null, null));
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 0, 5);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 0;
		add(scrollPane, gbc_scrollPane);

		table_detail = new JTable();
		table_detail.setModel(detailTableModel);
		table_detail.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					JTable target = (JTable) e.getSource();
					int row = target.getSelectedRow();
					AddNewModifierDialog nd = new AddNewModifierDialog(
							editApdu, editApdu.getFields().get(row), controller);
					nd.setVisible(true);
				}
			}
		});
		configureTable(table_detail);
		scrollPane.setViewportView(table_detail);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[] { 0, 0 };
		gbl_panel.rowHeights = new int[] { 0, 0, 0 };
		gbl_panel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_panel.rowWeights = new double[] { 1.0, 1.0, Double.MIN_VALUE };
		panel.setLayout(gbl_panel);

		JLabel lblHex = new JLabel("Hex: ");
		GridBagConstraints gbc_lblHex = new GridBagConstraints();
		gbc_lblHex.anchor = GridBagConstraints.NORTH;
		gbc_lblHex.insets = new Insets(0, 0, 5, 5);
		gbc_lblHex.gridx = 0;
		gbc_lblHex.gridy = 0;
		panel.add(lblHex, gbc_lblHex);

		JScrollPane scrollPane_hex = new JScrollPane();
		GridBagConstraints gbc_scrollPane_hex = new GridBagConstraints();
		gbc_scrollPane_hex.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane_hex.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_hex.gridx = 1;
		gbc_scrollPane_hex.gridy = 0;
		panel.add(scrollPane_hex, gbc_scrollPane_hex);

		textPane_hex = new JTextPane();
		scrollPane_hex.setViewportView(textPane_hex);

		JLabel lblAscii = new JLabel("Ascii: ");
		GridBagConstraints gbc_lblAscii = new GridBagConstraints();
		gbc_lblAscii.anchor = GridBagConstraints.NORTH;
		gbc_lblAscii.insets = new Insets(0, 0, 0, 5);
		gbc_lblAscii.gridx = 0;
		gbc_lblAscii.gridy = 1;
		panel.add(lblAscii, gbc_lblAscii);

		JScrollPane scrollPane_ascii = new JScrollPane();
		GridBagConstraints gbc_scrollPane_ascii = new GridBagConstraints();
		gbc_scrollPane_ascii.fill = GridBagConstraints.BOTH;
		gbc_scrollPane_ascii.gridx = 1;
		gbc_scrollPane_ascii.gridy = 1;
		panel.add(scrollPane_ascii, gbc_scrollPane_ascii);

		textPane_ascii = new JTextPane();
		scrollPane_ascii.setViewportView(textPane_ascii);

		// textPane_ascii.setEnabled(false);
		// textPane_hex.setEnabled(false);
	}

	public void clearFields() {
		textPane_ascii.setText("");
		textPane_hex.setText("");
	}

	public void setApdu(Packet editApdu) {
		this.editApdu = editApdu;
		this.detailTableModel.setApdu(editApdu);
		clearFields();
	}

	private void updateFields() {
		textPane_ascii.setText(editField.toAscii());
		textPane_hex.setText(editField.getValue());
	}

	private void configureTable(final JTable table) {
		table.setSelectionMode(0);
		table.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {

					@Override
					public void valueChanged(ListSelectionEvent e) {
						int index = table.getSelectedRow();
						if (index == -1) {
							ApduDetailPanel.this.clearFields();
						} else {
							ApduDetailPanel.this.setField(ApduDetailPanel.this.editApdu
									.getFields().get(index));
						}
					}
				});
		table.getTableHeader().setReorderingAllowed(false);
		Enumeration<TableColumn> a = table.getColumnModel().getColumns();
		for (int i = 0; a.hasMoreElements(); i++) {
			TableColumn tb = (TableColumn) a.nextElement();
			switch (i) {
			case 0:
				tb.setPreferredWidth(45);
				break;
			case 1:
				tb.setPreferredWidth(250);
				break;
			case 2:
				tb.setPreferredWidth(250);
				break;
			}

		}
	}

	protected void setField(Field editField) {
		this.editField = editField;
		updateFields();
	}

}
