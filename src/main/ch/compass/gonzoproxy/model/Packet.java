package ch.compass.gonzoproxy.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable, Cloneable {

	private static final long serialVersionUID = -4766720932383072042L;

	private boolean isModified = false;

	private byte[] originalPacketData = new byte[0];
	private byte[] preamble = new byte[0];
	private byte[] trailer = new byte[0];
	private ArrayList<Field> fields = new ArrayList<Field>();

	private String description = "";
	private ForwardingType type;
	private int size = 0;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public byte[] getPreamble() {
		return preamble;
	}

	public void setPreamble(byte[] preamble) {
		this.preamble = preamble;
	}

	public byte[] getOriginalPacketData() {
		return originalPacketData;
	}
	
	public void setOriginalPacketData(byte[] packet) {
		this.originalPacketData = packet;
	}

	public byte[] getTrailer() {
		return trailer;
	}

	public void setTrailer(byte[] trailer) {
		this.trailer = trailer;
	}

	public ArrayList<Field> getFields() {
		return fields;
	}

	public void setFields(ArrayList<Field> fields) {
		this.fields = fields;
	}

	public ForwardingType getType() {
		return type;
	}

	public void setType(ForwardingType type) {
		this.type = type;
	}

	public void addField(Field field) {
		fields.add(field);
	}

	public boolean isModified() {
		return isModified;
	}

	public void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	public void clearFields() {
		fields.clear();
	}

	public byte[] getPacketDataAsBytes(){
		return mergeFields().getBytes();
	}

	public String getPacketDataAsString() {
		return mergeFields();
	}

	@Override
	public String toString() {
		return new String(originalPacketData);
	}

	public String toAscii() {
		StringBuffer sb = new StringBuffer("");
		String hexPacketData = getPacketDataAsString().replaceAll("\\s", "");
		String[] hexPacketValues = hexPacketData.split("(?<=\\G..)");
	
		for (String hexValue : hexPacketValues) {
			try {
				int decimalValue = Integer.parseInt(hexValue, 16);
				char asciiValue = (char) decimalValue;
				sb.append(asciiValue);
			}catch (NumberFormatException e){
				sb.append('?');
			}
		}
		return sb.toString();
	}

	@Override
	public Packet clone() {
		Packet clonedPacket = new Packet();
		clonedPacket.setDescription(description);
		clonedPacket.setPreamble(preamble);
		clonedPacket.setOriginalPacketData(originalPacketData);
		clonedPacket.setTrailer(trailer);
		clonedPacket.setSize(size);
		clonedPacket.setType(type);
		for (Field field : fields) {
			clonedPacket.addField(field.clone());
		}
		return clonedPacket;
	}

	private String mergeFields() {
		StringBuilder mergedFields = new StringBuilder();
	
		for (Field field : fields) {
			mergedFields.append(field.getValue() + " ");
		}
		return mergedFields.substring(0, mergedFields.length() - 1);
	}
}
