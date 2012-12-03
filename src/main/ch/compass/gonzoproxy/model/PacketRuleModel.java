package ch.compass.gonzoproxy.model;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

import ch.compass.gonzoproxy.relay.modifier.PacketRule;


public class PacketRuleModel extends AbstractListModel<String>{
	
	private static final long serialVersionUID = 8749844876110276715L;
	private ArrayList<PacketRule> packetRules;

	public PacketRuleModel(ArrayList<PacketRule> rules) {
		this.packetRules = rules;
	}

	@Override
	public String getElementAt(int index) {
		return packetRules.get(index).getCorrespondingPacket();
	}

	@Override
	public int getSize() {
		return packetRules.size();
	}
	
	public void setRules(ArrayList<PacketRule> rules) {
		this.packetRules = rules;
		fireContentsChanged(this, 0, rules.size());
	}

}
