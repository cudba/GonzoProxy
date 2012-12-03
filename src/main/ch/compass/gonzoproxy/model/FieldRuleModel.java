package ch.compass.gonzoproxy.model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.relay.modifier.FieldRule;


public class FieldRuleModel extends AbstractTableModel{

	private static final long serialVersionUID = 3327345381172706548L;
	
	private String[] columnNames = { "Field", "Old value", "New value", "Active" };
	private ArrayList<FieldRule> fieldRules = new ArrayList<FieldRule>();
	
	
	public String getColumnName(int col) {
		return this.columnNames[col].toString();
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return fieldRules.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		
		FieldRule fieldRule = fieldRules.get(row);

		switch (column) {
		case 0:
			return fieldRule.getCorrespondingField();
		case 1:
			return fieldRule.getOriginalValue();
		case 2:
			return fieldRule.getReplacedValue();
		case 3:
			return fieldRule.isActive();
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
                return String.class;
            case 3:
                return Boolean.class;
            default:
                return String.class;
        }
    }
	
	public boolean isCellEditable(int row, int column){
		if(column == 3){
			return true;
		}
		return false;
	}
	
	public void setValueAt(Object value, int row, int column) {
		fieldRules.get(row).setActive((boolean) value);
	}

	
	public void setRules(ArrayList<FieldRule> rules){
		this.fieldRules = rules;
		fireTableDataChanged();
	}

}
