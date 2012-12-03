package ch.compass.gonzoproxy.model;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

import ch.compass.gonzoproxy.relay.modifier.PacketRule;


public class RuleSetModel extends AbstractListModel<String>{
	
	private static final long serialVersionUID = 8749844876110276715L;
	private ArrayList<PacketRule> rules;

	public RuleSetModel(ArrayList<PacketRule> rules) {
		this.rules = rules;
	}

	@Override
	public String getElementAt(int index) {
		return rules.get(index).getCorrespondingPacket();
	}

	@Override
	public int getSize() {
		return rules.size();
	}
	
	public void setRules(ArrayList<PacketRule> rules) {
		this.rules = rules;
		fireContentsChanged(this, 0, rules.size());
	}

}
