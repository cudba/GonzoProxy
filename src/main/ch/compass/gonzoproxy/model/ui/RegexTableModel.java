package ch.compass.gonzoproxy.model.ui;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.model.modifier.PacketRegex;

public class RegexTableModel extends AbstractTableModel {
	
	
	private static final long serialVersionUID = -3354798677594632531L;
	private String[] columnNames = { "Regex", "Replace with", "Active" };
	private ArrayList<PacketRegex> packetRegex;

	public RegexTableModel(ArrayList<PacketRegex> packetRegex) {
		this.packetRegex = packetRegex;
	}
	
	public String getColumnName(int col) {
		return this.columnNames[col].toString();
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return packetRegex.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		
		PacketRegex regex = packetRegex.get(row);

		switch (column) {
		case 0:
			return regex.getRegex();
		case 1:
			return regex.getReplaceWith();
		case 2:
			return regex.isActive();
		}
		
		return null;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
    public Class getColumnClass(int column) {
        switch (column) {
            case 0:
                return String.class;
            case 1:
                return String.class;
            case 2:
                return Boolean.class;
            default:
                return String.class;
        }
    }
	
	public boolean isCellEditable(int row, int column){
		if(column == 2){
			return true;
		}
		return false;
	}
	
	public void setValueAt(Object value, int row, int column) {
		packetRegex.get(row).setActive(((Boolean)value).booleanValue());
	}
	
	public void regexChanged() {
		fireTableDataChanged();
	}

}
