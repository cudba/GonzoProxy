package ch.compass.gonzoproxy.model;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.listener.SessionListener;

public class ApduTableModel extends AbstractTableModel {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1437358812481945385L;
	private SessionModel session;
	String[] columnNames;;

	public ApduTableModel(SessionModel session, String[] columnNames) {
		this.session = session;
		this.columnNames = columnNames;
		this.session.addSessionListener(createListener());
	}

	private SessionListener createListener() {
		return new SessionListener() {
			
			@Override
			public void packetReceived(Packet receivedPacket) {
				updateTable();
			}
			
			@Override
			public void packetCleared() {
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
		Packet apdu = session.getPacketList().get(rowIndex);

		switch (columnIndex) {
		case 0:
			return rowIndex;
		case 1:
			return apdu.getType().getId();
		case 2:
			return apdu.getPacketDataAsString();
		case 3:
			return apdu.toAscii();
		case 4:
			return apdu.getDescription();
		}
		return null;

	}

}
