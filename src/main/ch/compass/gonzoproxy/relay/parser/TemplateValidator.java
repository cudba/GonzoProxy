package ch.compass.gonzoproxy.relay.parser;

import java.util.ArrayList;

import ch.compass.gonzoproxy.mvc.model.Packet;
import ch.compass.gonzoproxy.mvc.model.Field;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class TemplateValidator {

	public boolean accept(PacketTemplate template, Packet processingPacket) {
		byte[] packet = processingPacket.getOriginalPacketData();
		ArrayList<Field> templateFields = template.getFields();

		int contentStartIndex = 0;
		int contentLength = PacketUtils.DEFAULT_FIELDLENGTH;

		int fieldLength = PacketUtils.DEFAULT_FIELDLENGTH;
		int offset = 0;

		for (int i = 0; i < templateFields.size(); i++) {
			if (!packetContainsMoreFields(packet, fieldLength, offset)) {
				return false;
			}

			Field processingField = templateFields.get(i);
			if (isIdentifierField(processingField)) {
				if (!fieldIsVerified(packet, fieldLength, offset,
						processingField)) {
					return false;
				}
			}

			int currentFieldOffset = offset;
			offset += PacketUtils.getEncodedFieldLength(fieldLength, true);
			if (PacketUtils.isContentLengthField(processingField)) {
				int encodedFieldLength = PacketUtils.getEncodedFieldLength(
						fieldLength, false);
				byte[] length = PacketUtils.extractField(packet,
						encodedFieldLength, currentFieldOffset);
				if (PacketUtils.isContentIdentifierField(templateFields
						.get(i + 1))) {
					contentLength = Integer.parseInt(new String(length), 16);
					contentStartIndex = offset;
				} else {
					fieldLength = Integer.parseInt(new String(length), 16);
				}
			}

			else if (PacketUtils.isIdentifiedContent(templateFields, i,
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
							packet,
							currentFieldOffset,
							templateFields.get(i
									+ PacketUtils.NEXT_IDENTIFIER_OFFSET));
					fieldLength = PacketUtils.calculateSubContentLength(
							offset, nextIdentifierIndex);
					break;
				}
			} else {
				fieldLength = PacketUtils.DEFAULT_FIELDLENGTH;
			}
		}
		return offset - PacketUtils.WHITESPACE_OFFSET == packet.length;
	}

	private boolean packetContainsMoreFields(byte[] packet, int fieldLength,
			int offset) {
		return (offset + fieldLength * PacketUtils.ENCODING_OFFSET) <= packet.length;
	}

	private boolean isIdentifierField(Field processingField) {
		return processingField.getValue() != null;
	}

	private boolean fieldIsVerified(byte[] packet, int fieldLength, int offset,
			Field processingField) {
		byte[] idByte;
		int encodedFieldLength = PacketUtils.getEncodedFieldLength(
				fieldLength, false);
		if (fieldLength > PacketUtils.DEFAULT_FIELDLENGTH) {
			idByte = PacketUtils.extractField(packet,
					encodedFieldLength, offset);
		} else {
			idByte = PacketUtils.extractField(packet,
					encodedFieldLength, offset);
		}
		if (!valueMatches(idByte, processingField)) {
			return false;
		}
		return true;
	}

	private boolean valueMatches(byte[] idByte, Field field) {
		String packetValue = new String(idByte);
		String templateValue = field.getValue();
		return packetValue.equalsIgnoreCase(templateValue);
	}

}
