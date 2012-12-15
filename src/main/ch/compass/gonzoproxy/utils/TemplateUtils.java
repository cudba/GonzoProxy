package ch.compass.gonzoproxy.utils;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketDataSettings;
import ch.compass.gonzoproxy.model.template.TemplateSettings;

public class TemplateUtils {

	public static byte[] extractField(byte[] plainPacket, int fieldLength,
			int currentOffset) {
		return ByteArraysUtils.trim(plainPacket, currentOffset, fieldLength);
	}

	public static int findFieldInPlainPacket(byte[] plainPacket, int offset,
			Field field) {
		byte[] nextContentIdentifier = field.getValue().getBytes();

		for (int i = offset; i < plainPacket.length
				- nextContentIdentifier.length; i++) {

			boolean matches = true;
			for (int j = 0; j < nextContentIdentifier.length; j++) {
				if (nextContentIdentifier[j] != plainPacket[i + j])
					matches = false;
			}
			if (matches)
				return i;
		}
		return 0;
	}

	public static boolean isContentLengthField(Field processingField) {
		return processingField.getName() != null
				&& processingField.getName().equals(TemplateSettings.CONTENT_LENGTH_FIELD);
	}

	public static int findNextContentIdentifierField(int offset,
			ArrayList<Field> templateFields) {

		int fieldIndex = 1;
		for (int i = offset; i < templateFields.size(); i++) {
			if (isContentIdentifierField(templateFields.get(i))) {
				return fieldIndex;
			}
			fieldIndex++;
		}
		return 0;
	}

	public static boolean isContentIdentifierField(Field processingField) {
		if (processingField.getName() != null)
			return processingField.getName().contains(TemplateSettings.CONTENT_IDENTIFIER);
		return false;
	}

	public static boolean isNextFieldContentIdentifier(ArrayList<Field> fields,
			int offset) {
		if (fields.size() > offset + 1) {
			return TemplateUtils.isContentIdentifierField(fields.get(offset + 1));
		}
		return false;
	}

	public static boolean isIdentifyingContent(ArrayList<Field> templateFields,
			int offset, Field processingField) {
		if (processingField.getName() != null)
			return processingField.getName().contains(TemplateSettings.CONTENT_IDENTIFIER)
					&& templateFields.size() > offset + 1;
		return false;
	}

	public static boolean isLastField(ArrayList<Field> templateFields, int i) {
		return templateFields.size() - 1 == i;
	}

	public static boolean isContentField(Field processingField) {
		if (processingField.getName() != null)
			return processingField.getName().toUpperCase()
					.contains(TemplateSettings.CONTENT_DATA);
		return false;
	}
	
	public static int computeFieldLength(ArrayList<Field> fields, int offset) {
		int fieldLength = PacketDataSettings.DEFAULT_FIELDLENGTH;
		if (fields.size() > offset) {
			Field field = fields.get(offset);
			if (field.getValue() != null) {
				String value = field.getValue().replaceAll("\\s", "");
				fieldLength = value.length() / PacketDataSettings.ENCODING_OFFSET;
			}
		}
		return fieldLength;
	}
	
	public static Field findContentLengthField(Packet packet) {
		for (Field field : packet.getFields()) {
			if (field.getName().equals(TemplateSettings.CONTENT_LENGTH_FIELD))
				return field;
		}
		return new Field();
	}

}
