package ch.compass.gonzoproxy.mvc.model;

import java.util.ArrayList;

import javax.swing.table.AbstractTableModel;

import ch.compass.gonzoproxy.relay.modifier.Rule;


public class RuleModel extends AbstractTableModel{

	private String[] columnNames = { "Field", "Old value", "New value", "is active" };
	private ArrayList<Rule> rules = new ArrayList<Rule>();
	
	
	public String getColumnName(int col) {
		return this.columnNames[col].toString();
	}
	
	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return rules.size();
	}

	@Override
	public Object getValueAt(int row, int column) {
		
		Rule rule = rules.get(row);

		switch (column) {
		case 0:
			return rule.getCorrespondingField();
		case 1:
			return rule.getOriginalValue();
		case 2:
			return rule.getReplacedValue();
		case 3:
			return rule.isActive();
		}
		
		return null;
	}
	
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
	
	public void setRules(ArrayList<Rule> rules){
		this.rules = rules;
		fireTableDataChanged();
	}

}
