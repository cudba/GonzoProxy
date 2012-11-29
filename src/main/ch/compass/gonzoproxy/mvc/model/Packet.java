package ch.compass.gonzoproxy.mvc.model;

import java.io.Serializable;
import java.util.ArrayList;

public class Packet implements Serializable, Cloneable {

	private static final long serialVersionUID = -4766720932383072042L;

	private boolean isModified = false;

	private byte[] originalPacketData;
	private byte[] preamble;
	private byte[] trailer;
	private ArrayList<Field> fields = new ArrayList<Field>();

	private String description;
	private ForwardingType type;
	private int size;

	private byte[] streamInput;

	public Packet(byte[] streamInput) {
		this.streamInput = streamInput;
	}

	public byte[] getOriginalPacketData() {
		return originalPacketData;
	}

	public String getPacketFromFields() {

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : this.getFields()) {
			mergedFields.append(field.getValue() + " ");
		}
		return mergedFields.substring(0, mergedFields.length() - 1);
	}

	public void setOriginalPacketData(byte[] packet) {
		this.originalPacketData = packet;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
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

	@Override
	public String toString() {
		return new String(originalPacketData);
	}

	public String toAscii() {
		StringBuffer sb = new StringBuffer("");
		String ascii = getPacketFromFields().replaceAll("\\s", "");
		String[] strArr = ascii.split("(?<=\\G..)");

		for (String a : strArr) {
			int c = Integer.parseInt(a, 16);
			char chr = (char) c;
			sb.append(chr);
		}

		return sb.toString();
	}

	public void setPreamble(byte[] preamble) {
		this.preamble = preamble;
	}

	public byte[] getPreamble() {
		return preamble;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getSize() {
		return size;
	}

	public byte[] getStreamInput() {
		return streamInput;
	}

	public void setTrailer(byte[] trailer) {
		this.trailer = trailer;
	}

	public byte[] getTrailer() {
		return trailer;
	}

	public void addField(Field field) {
		fields.add(field);
	}

	@Override
	public Packet clone() {
		Packet clonedPacket = new Packet(streamInput);
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

	public boolean isModified() {
		return isModified;
	}

	public void isModified(boolean isModified) {
		this.isModified = isModified;
	}
}
