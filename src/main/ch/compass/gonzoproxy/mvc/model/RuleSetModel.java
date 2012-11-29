package ch.compass.gonzoproxy.mvc.model;

import java.util.ArrayList;

import javax.swing.AbstractListModel;

import ch.compass.gonzoproxy.relay.modifier.RuleSet;


public class RuleSetModel extends AbstractListModel<String>{
	
	private static final long serialVersionUID = 8749844876110276715L;
	private ArrayList<RuleSet> rules;

	public RuleSetModel(ArrayList<RuleSet> rules) {
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

}
