package ch.compass.gonzoproxy.relay.modifier;

import java.io.Serializable;
import java.util.ArrayList;

import ch.compass.gonzoproxy.mvc.model.Field;

public class RuleSet implements Serializable {

	private static final long serialVersionUID = -3893571727728725384L;

	private String correspondingPacket;

	private ArrayList<Rule> rules = new ArrayList<Rule>();

	private Boolean updateLength;

	public RuleSet(String correspondingPacket) {
		this.correspondingPacket = correspondingPacket;
	}

	public String getCorrespondingPacket() {
		return correspondingPacket;
	}

	public void setCorrespondingPacket(String correspondingPacket) {
		this.correspondingPacket = correspondingPacket;
	}

	public void add(Rule rule) {
		rules.add(rule);
	}

	public ArrayList<Rule> getRules() {
		return rules;
	}

	public Rule findMatchingRule(Field field) {
		for (Rule rule : rules) {
			if (isMatchingRule(field, rule))
				return rule;
		}
		return null;
	}

	private boolean isMatchingRule(Field field, Rule rule) {
		return field.getName().equals(rule.getCorrespondingField())
				&& field.getValue().contains(rule.getOriginalValue());
	}

	public void shouldUpdateLength(Boolean updateLength) {
		this.updateLength = updateLength;
		
	}
	
	public boolean shouldUpdateContentLength(){
		return updateLength;
	}

	@Override
	public boolean equals(Object object) {
		return ((RuleSet) object).getCorrespondingPacket().equals(
				correspondingPacket);
	}
}
