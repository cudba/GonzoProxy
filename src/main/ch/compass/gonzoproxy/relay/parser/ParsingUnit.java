package ch.compass.gonzoproxy.relay.parser;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.Field;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class ParsingUnit {

	public boolean parseBy(PacketTemplate template, Packet processingPacket) {
		processingPacket.setDescription(template.getPacketDescription());
		ArrayList<Field> templateFields = template.getFields();
		byte[] packet = processingPacket.getOriginalPacketData();

		int contentStartIndex = 0;
		int contentLength = 0;

		int offset = 0;
		int fieldLength = PacketUtils
				.computeFieldLength(templateFields, offset);

		for (int i = 0; i < templateFields.size(); i++) {
			Field processingField = templateFields.get(i).clone();

			if (PacketUtils.isLastField(templateFields, i)
					&& !PacketUtils.isContentField(processingField)) {
				fieldLength = PacketUtils.getRemainingPacketSize(packet.length,
						offset);
			}

			parseField(packet, fieldLength, offset, processingField);
			processingPacket.addField(processingField);

			offset += PacketUtils.getEncodedFieldLength(fieldLength, true);

			if (PacketUtils.isContentLengthField(processingField)) {

				int nextContentIdentifierField = PacketUtils
						.findNextContentIdentifierField(i + 1, templateFields);

				switch (nextContentIdentifierField) {
				case 0:
					fieldLength = Integer.parseInt(processingField.getValue(),
							16);
					break;
				case 1:
					contentLength = Integer.parseInt(
							processingField.getValue(), 16);
					contentStartIndex = offset;
					fieldLength = PacketUtils.computeFieldLength(
							templateFields, i + 1);

					break;
				default:
					contentLength = Integer.parseInt(
							processingField.getValue(), 16);
					contentStartIndex = offset;
					int nextIdentifierIndex = PacketUtils
							.findFieldInPlainPacket(
									packet,
									offset,
									templateFields.get(i
											+ nextContentIdentifierField));
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
			byte[] value = PacketUtils.extractField(payload, fieldLength,
					offset);
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