package ch.compass.gonzoproxy.controller.relay.parser;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.template.PacketTemplate;
import ch.compass.gonzoproxy.utils.PacketUtils;
import ch.compass.gonzoproxy.utils.TemplateUtils;

public class TemplateValidator {

	public boolean accept(PacketTemplate template, Packet processingPacket) {
		byte[] packet = processingPacket.getPacketData();
		ArrayList<Field> templateFields = template.getFields();

		int contentStartIndex = 0;
		int contentLength = 0;

		int offset = 0;
		int fieldLength = TemplateUtils
				.computeFieldLength(templateFields, offset);

		for (int i = 0; i < templateFields.size(); i++) {
			if (!PacketUtils.packetStreamContainsMoreFields(packet, fieldLength, offset)) {
				return false;
			}

			Field processingField = templateFields.get(i);

			if (TemplateUtils.isLastField(templateFields, i)
					&& !TemplateUtils.isContentField(processingField)) {
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
			if (TemplateUtils.isContentLengthField(processingField)) {
				int encodedFieldLength = PacketUtils.getEncodedFieldLength(
						fieldLength, false);
				byte[] length = TemplateUtils.extractField(packet,
						encodedFieldLength, currentFieldOffset);

				int nextContentIdentifierPosition = TemplateUtils
						.findNextContentIdentifierField(i + 1, templateFields);

				switch (nextContentIdentifierPosition) {
				case 0:
					fieldLength = Integer.parseInt(new String(length), 16);
					break;

				case 1:
					contentLength = Integer.parseInt(new String(length), 16);
					contentStartIndex = offset;
					fieldLength = TemplateUtils.computeFieldLength(
							templateFields, i + 1);
					break;

				default:
					contentLength = Integer.parseInt(new String(length), 16);
					contentStartIndex = offset;
					int nextIdentifierIndex = TemplateUtils
							.findFieldInPlainPacket(
									packet,
									offset,
									templateFields.get(i
											+ nextContentIdentifierPosition));
					fieldLength = PacketUtils.getSubContentLength(offset,
							nextIdentifierIndex);
					break;
				}

			} else if (TemplateUtils.isNextFieldContentIdentifier(templateFields,
					i)) {
				fieldLength = TemplateUtils.computeFieldLength(templateFields,
						i + 1);
			} else if (TemplateUtils.isIdentifyingContent(templateFields, i,
					processingField)) {

				int nextContentIdentifierPosition = TemplateUtils
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
					fieldLength = TemplateUtils.computeFieldLength(
							templateFields, i + 1);
					break;
				default:
					int nextIdentifierIndex = TemplateUtils
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
				fieldLength = TemplateUtils.computeFieldLength(templateFields,
						i + 1);
			}
		}
		return templateFields.size() > 0;
	}

	private boolean shouldVerify(Field processingField) {
		return processingField.getValue() != null;
	}

	private boolean fieldIsVerified(byte[] packet, int fieldLength, int offset,
			Field processingField) {
		int encodedFieldLength = PacketUtils.getEncodedFieldLength(fieldLength,
				false);
		byte[] idByte = TemplateUtils.extractField(packet, encodedFieldLength,
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
