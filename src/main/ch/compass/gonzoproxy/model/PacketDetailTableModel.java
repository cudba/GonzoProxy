package ch.compass.gonzoproxy.model;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.listener.SessionListener;

public class PacketDetailTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1437358812481945385L;
	private Packet packet;
	private SessionModel session;
	private String[] columnNames = { "Field", "Value", "Description" };
	  
	public PacketDetailTableModel(Packet packet, SessionModel session) {
		this.packet = packet;
		this.session = session;
		this.session.addSessionListener(createListener());
	}

	private SessionListener createListener() {
		return new SessionListener() {
			
			@Override
			public void packetReceived(Packet receivedPacket) {
				
			}
			
			@Override
			public void packetCleared() {
				setApdu(new Packet());
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

	public void setApdu(Packet editApdu) {
		this.packet = editApdu;
		fireTableDataChanged();
	}

}
