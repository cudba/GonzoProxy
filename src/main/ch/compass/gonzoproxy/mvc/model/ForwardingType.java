package ch.compass.gonzoproxy.mvc.model;

public enum ForwardingType {
	COMMAND("COM"), 
	RESPONSE("RES");
	
	private String id;
	private ForwardingType(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
