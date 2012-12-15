package ch.compass.gonzoproxy.model.ui;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.listener.DataListener;
import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.relay.RelayDataModel;

public class PacketDetailTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1437358812481945385L;
	private Packet packet;
	private RelayDataModel session;
	private String[] columnNames = { "Field", "Value", "Description" };
	  
	public PacketDetailTableModel(Packet packet, RelayDataModel session) {
		this.packet = packet;
		this.session = session;
		this.session.addDataListener(createListener());
	}

	private DataListener createListener() {
		return new DataListener() {
			
			@Override
			public void packetReceived(Packet receivedPacket) {
				
			}
			
			@Override
			public void packetsCleared() {
				setPacket(new Packet());
			}

			@Override
			public void newList() {
				
			}
		};
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
		return packet.getFields().size();
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Field field = packet.getFields().get(rowIndex);

		switch (columnIndex) {
		case 0:
			return field.getName();
		case 1:
			return field.getValue();
		case 2:
			return field.getDescription();
		}
		return null;

	}

	public void setPacket(Packet editPacket) {
		this.packet = editPacket;
		fireTableDataChanged();
	}

}
