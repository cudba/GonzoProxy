package ch.compass.gonzoproxy.controller.relay.modifier;

import java.io.Serializable;

public class PacketRegex implements Serializable{
	
	private static final long serialVersionUID = -936176001069709025L;
	private boolean isActive = true;
	private String regex;
	private String replaceWith;
	
	public PacketRegex(String regex, String replaceWith) {
		this.regex = regex;
		this.replaceWith = replaceWith;
	}
	public boolean isActive() {
		return isActive;
	}
	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}
	public String getRegex() {
		return regex;
	}
	public void setRegex(String regex) {
		this.regex = regex;
	}
	public String getReplaceWith() {
		return replaceWith;
	}
	public void setReplaceWith(String replaceWith) {
		this.replaceWith = replaceWith;
	}
	

	
}
