package ch.compass.gonzoproxy.model.ui;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.listener.DataListener;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.relay.RelayDataModel;

public class PacketTableModel extends AbstractTableModel {

	private static final long serialVersionUID = -1437358812481945385L;
	private RelayDataModel session;
	String[] columnNames = { "#", "Type", "Packet", "ASCII", "Description" };

	public PacketTableModel(RelayDataModel session) {
		this.session = session;
		this.session.addDataListener(createListener());
	}

	private DataListener createListener() {
		return new DataListener() {

			@Override
			public void packetReceived(Packet receivedPacket) {
				updateTable();
			}

			@Override
			public void packetsCleared() {
				updateTable();
			}

			@Override
			public void newList() {
				updateTable();
			}
		};
	}

	private void updateTable() {
		fireTableDataChanged();
	}

	public String getColumnName(int col) {
		return this.columnNames[col].toString();
	}

	@Override
	public int getColumnCount() {
		return this.columnNames.length;
	}

	@Override
	public int getRowCount() {
		return session.getPacketList().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Packet packet = session.getPacketList().get(rowIndex);

		switch (columnIndex) {
		case 0:
			return rowIndex;
		case 1:
			return packet.getType().getId();
		case 2:
			return packet.getPacketDataAsString();
		case 3:
			return packet.toAscii();
		case 4:
			return packet.getDescription();
		}
		return null;

	}

}
