package ch.compass.gonzoproxy.relay.parser;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.template.PacketTemplate;
import ch.compass.gonzoproxy.utils.PacketUtils;
import ch.compass.gonzoproxy.utils.TemplateUtils;

public class ParsingUnit {

	public boolean parseBy(PacketTemplate template, Packet processingPacket) {
		processingPacket.setDescription(template.getPacketDescription());
		ArrayList<Field> templateFields = template.getFields();
		byte[] packet = processingPacket.getPacketData();

		int contentStartIndex = 0;
		int contentLength = 0;

		int offset = 0;
		int fieldLength = PacketUtils
				.computeFieldLength(templateFields, offset);

		for (int i = 0; i < templateFields.size(); i++) {
			if (!PacketUtils.packetStreamContainsMoreFields(packet, fieldLength, offset)) {
				return false;
			}
			
			Field processingField = templateFields.get(i).clone();

			if (TemplateUtils.isLastField(templateFields, i)
					&& !TemplateUtils.isContentField(processingField)) {
				fieldLength = PacketUtils.getRemainingPacketSize(packet.length,
						offset);
			}

			parseField(packet, fieldLength, offset, processingField);
			processingPacket.addField(processingField);

			offset += PacketUtils.getEncodedFieldLength(fieldLength, true);

			if (TemplateUtils.isContentLengthField(processingField)) {

				int nextContentIdentifierField = TemplateUtils
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
					fieldLength = TemplateUtils.computeFieldLength(
							templateFields, i + 1);

					break;
				default:
					contentLength = Integer.parseInt(
							processingField.getValue(), 16);
					contentStartIndex = offset;
					int nextIdentifierIndex = TemplateUtils
							.findFieldInPlainPacket(
									packet,
									offset,
									templateFields.get(i
											+ nextContentIdentifierField));
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
		return true;
	}

	public void parseByDefault(Packet processingPacket) {
		
		String packetDescription = processingPacket.getDescription();
		String fieldName;
		String fieldValue = new String(processingPacket.getPacketData());
		String fieldDescription;
		
		if(packetDescription.isEmpty()) {
			packetDescription = "Unknown Packet";
			fieldName = "unknown";
			fieldDescription = "Unknown Packet, parsed by default template";
			
		}else {
			fieldName = packetDescription;
			fieldDescription = packetDescription;
		}
	
		Field defaultField = new Field(fieldName, fieldValue, fieldDescription);
		processingPacket.addField(defaultField);
		processingPacket.setDescription(packetDescription);
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
			byte[] value = TemplateUtils.extractField(payload, fieldLength,
					offset);
			setFieldValue(field, value);
		}
	}

	private void setFieldValue(Field field, byte[] value) {
		field.setValue(new String(value));
	}
}