package ch.compass.gonzoproxy.model.packet;

public enum PacketType {
	COMMAND("COM"), 
	RESPONSE("RES");
	
	private String id;
	private PacketType(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
}
