package ch.compass.gonzoproxy.model.template;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.packet.Field;

public class PacketTemplate {

	private String packetDescription = "";
	private ArrayList<Field> fields = new ArrayList<Field>();
	
	
	public String getPacketDescription() {
		return packetDescription;
	}
	
	public void setPacketDescription(String packetDescription) {
		this.packetDescription = packetDescription;
	}

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

}
