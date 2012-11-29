package ch.compass.gonzoproxy.relay.parser;

import java.util.ArrayList;

import ch.compass.gonzoproxy.mvc.model.Field;
import ch.compass.gonzoproxy.mvc.model.Packet;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class ParsingUnit {

	public boolean parseBy(PacketTemplate template, Packet processingPacket) {
		processingPacket.setDescription(template.getPacketDescription());
		ArrayList<Field> templateFields = template.getFields();
		byte[] packet = processingPacket.getOriginalPacketData();

		int contentStartIndex = 0;
		int contentLength = PacketUtils.DEFAULT_FIELDLENGTH;

		int fieldLength = PacketUtils.DEFAULT_FIELDLENGTH;
		int offset = 0;

		for (int i = 0; i < templateFields.size(); i++) {
			Field processingField = templateFields.get(i).clone();
			parseField(packet, fieldLength, offset, processingField);
			processingPacket.addField(processingField);

			int currentFieldOffset = offset;
			offset += PacketUtils.getEncodedFieldLength(fieldLength, true);

			if (PacketUtils.isContentLengthField(processingField)) {
				if (PacketUtils.isContentIdentifierField(templateFields
						.get(i + 1))) {
					contentLength = Integer.parseInt(
							processingField.getValue(), 16);
					contentStartIndex = offset;
				} else {
					fieldLength = Integer.parseInt(processingField.getValue(),
							16);
				}

			} else if (PacketUtils.isIdentifiedContent(templateFields, i,
					processingField)) {
				int nextContentIdentifierField = PacketUtils
						.findNextContentIdentifierField(i + 1, templateFields);

				switch (nextContentIdentifierField) {
				case 0:
					fieldLength = PacketUtils.getRemainingContentSize(
							contentStartIndex, contentLength, offset);
					break;
				case 1:
					fieldLength = PacketUtils.DEFAULT_FIELDLENGTH;
					break;
				default:
					int nextIdentifierIndex = PacketUtils.findFieldInPlainPacket(
							packet, currentFieldOffset,
							templateFields.get(i + nextContentIdentifierField));
					fieldLength = PacketUtils.calculateSubContentLength(
							offset, nextIdentifierIndex);
					break;
				}
			} else {
				fieldLength = PacketUtils.DEFAULT_FIELDLENGTH;
			}
		}
		return true;
	}

	private void parseField(byte[] payload, int fieldLength, int offset,
			Field processingField) {
		if (PacketUtils.hasCustomLenght(fieldLength)) {
			parseValueToField(payload, offset,
					PacketUtils.getEncodedFieldLength(fieldLength, false),
					processingField);
		} else {
			int encodedFieldLength = PacketUtils.getEncodedFieldLength(
					fieldLength, false);
			parseValueToField(payload, offset, encodedFieldLength,
					processingField);
		}
	}

	private void parseValueToField(byte[] payload, int offset, int fieldLength,
			Field field) {
		if ((offset + fieldLength) <= payload.length) {
			byte[] value = PacketUtils.extractField(payload,
					fieldLength, offset);
			setFieldValue(field, value);
		}
	}

	private void setFieldValue(Field field, byte[] value) {
		field.setValue(new String(value));
	}

	public void parseByDefault(Packet processingPacket) {
		String packetDescription = "Unknown Packet";
		String fieldName = "unknown";
		String fieldValue = new String(processingPacket.getOriginalPacketData());
		String fieldDescription = "Unknown Packet, parsed by default template";
		
		Field defaultField = new Field(fieldName, fieldValue, fieldDescription);
		processingPacket.addField(defaultField);
		processingPacket.setDescription(packetDescription);
	}
}