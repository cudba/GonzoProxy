package ch.compass.gonzoproxy.relay.parser;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.Field;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class TemplateValidator {

	public boolean accept(PacketTemplate template, Packet processingPacket) {
		byte[] packet = processingPacket.getOriginalPacketData();
		ArrayList<Field> templateFields = template.getFields();

		int contentStartIndex = 0;
		int contentLength = 0;

		int offset = 0;
		int fieldLength = PacketUtils
				.computeFieldLength(templateFields, offset);

		for (int i = 0; i < templateFields.size(); i++) {
			if (!packetContainsMoreFields(packet, fieldLength, offset)) {
				return false;
			}

			Field processingField = templateFields.get(i);

			if (PacketUtils.isLastField(templateFields, i)
					&& !PacketUtils.isContentField(processingField)) {
				fieldLength = PacketUtils.getRemainingPacketSize(packet.length,
						offset);
			}

			if (shouldVerify(processingField)) {
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

				int nextContentIdentifierPosition = PacketUtils
						.findNextContentIdentifierField(i + 1, templateFields);

				switch (nextContentIdentifierPosition) {
				case 0:
					fieldLength = Integer.parseInt(new String(length), 16);
					break;

				case 1:
					contentLength = Integer.parseInt(new String(length), 16);
					contentStartIndex = offset;
					fieldLength = PacketUtils.computeFieldLength(
							templateFields, i + 1);
					break;

				default:
					contentLength = Integer.parseInt(new String(length), 16);
					contentStartIndex = offset;
					int nextIdentifierIndex = PacketUtils
							.findFieldInPlainPacket(
									packet,
									offset,
									templateFields.get(i
											+ nextContentIdentifierPosition));
					fieldLength = PacketUtils.getSubContentLength(offset,
							nextIdentifierIndex);
					break;
				}

			} else if (PacketUtils.isNextFieldContentIdentifier(templateFields,
					i)) {
				fieldLength = PacketUtils.computeFieldLength(templateFields,
						i + 1);
			} else if (PacketUtils.isIdentifyingContent(templateFields, i,
					processingField)) {

				int nextContentIdentifierPosition = PacketUtils
						.findNextContentIdentifierField(i + 1, templateFields);

				switch (nextContentIdentifierPosition) {
				case 0:
					if (contentLength == 0) {
						fieldLength = PacketUtils.getRemainingPacketSize(
								packet.length, offset);
					} else {
						fieldLength = PacketUtils.getRemainingContentSize(
								contentStartIndex, contentLength, offset);
					}
					break;
				case 1:
					fieldLength = PacketUtils.computeFieldLength(
							templateFields, i + 1);
					break;
				default:
					int nextIdentifierIndex = PacketUtils
							.findFieldInPlainPacket(
									packet,
									offset,
									templateFields.get(i
											+ nextContentIdentifierPosition));
					fieldLength = PacketUtils.getSubContentLength(offset,
							nextIdentifierIndex);
					break;
				}
			} else {
				fieldLength = PacketUtils.computeFieldLength(templateFields,
						i + 1);
			}
		}
		return true;
		// return offset - PacketUtils.WHITESPACE_OFFSET == packet.length;
	}

	private boolean packetContainsMoreFields(byte[] packet, int fieldLength,
			int offset) {
		return (offset + fieldLength * PacketUtils.ENCODING_OFFSET) <= packet.length;
	}

	private boolean shouldVerify(Field processingField) {
		return processingField.getValue() != null;
	}

	private boolean fieldIsVerified(byte[] packet, int fieldLength, int offset,
			Field processingField) {
		int encodedFieldLength = PacketUtils.getEncodedFieldLength(fieldLength,
				false);
		byte[] idByte = PacketUtils.extractField(packet, encodedFieldLength,
				offset);
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
