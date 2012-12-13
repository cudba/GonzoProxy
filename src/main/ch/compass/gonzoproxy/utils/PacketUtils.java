package ch.compass.gonzoproxy.utils;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.Field;
import ch.compass.gonzoproxy.model.Packet;

public class PacketUtils {

	public static final String END_OF_STREAM_PACKET_DESCRIPTION = "End Of Stream";
	public static final String MODE_FAILURE_PACKET_DESCRIPTION = "Mode loading error";

	public static final String CONTENT_LENGTH_FIELD = "Lc";
	public static final String CONTENT_IDENTIFIER = "Ci";
	public static final String CONTENT_DATA = "CONTENT";
	
	public static final int DEFAULT_FIELDLENGTH = 1;
	public static final int ENCODING_OFFSET = 2;
	public static final int WHITESPACE_OFFSET = 1;

	
	public static byte[] extractField(byte[] plainPacket, int fieldLength,
			int currentOffset) {
		return ByteArraysUtils.trim(plainPacket, currentOffset, fieldLength);
	}

	public static int getEncodedFieldLength(int fieldLength, boolean withWhitespace) {
		if (withWhitespace) {
			return fieldLength * (ENCODING_OFFSET + WHITESPACE_OFFSET);
		} else {
			return fieldLength * (ENCODING_OFFSET + WHITESPACE_OFFSET)
					- WHITESPACE_OFFSET;
		}
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
			if(matches)
				return i;
		}
		return 0;
	}

	public static int computeFieldLength(ArrayList<Field> fields, int offset) {
		int fieldLength = DEFAULT_FIELDLENGTH;
		if(fields.size() > offset){
			Field field = fields.get(offset);
			if(field.getValue() != null) {
				String value = field.getValue().replaceAll("\\s", "");
				fieldLength = value.length() / ENCODING_OFFSET;
			}
		}
		return fieldLength;
	}

	public static int getRemainingContentSize(int contentStartIndex,
			int contentLength, int offset) {
		return contentLength
				- ((offset - contentStartIndex) / (ENCODING_OFFSET + WHITESPACE_OFFSET));
	}

	public static boolean isContentLengthField(Field processingField) {
		return processingField.getName() != null
				&& processingField.getName().equals(CONTENT_LENGTH_FIELD);
	}

	public static int calculateSubContentLength(int offset, int nextIdentifier) {
		return (nextIdentifier - offset)
				/ (ENCODING_OFFSET + WHITESPACE_OFFSET);
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
		return processingField.getName().contains(CONTENT_IDENTIFIER);
	}

	public static boolean isNextFieldContentIdentifier(
			ArrayList<Field> fields, int offset) {
		if (fields.size() > offset + 1) {
			return PacketUtils.isContentIdentifierField(fields
					.get(offset + 1));
		}
		return false;
	}

	public static boolean hasCustomLenght(int fieldLength) {
		return fieldLength > DEFAULT_FIELDLENGTH;
	}

	public static boolean isIdentifyingContent(ArrayList<Field> templateFields,
			int offset, Field processingField) {
		return processingField.getName().contains(CONTENT_IDENTIFIER)
				&& templateFields.size() > offset + 1;
	}

	public static Packet getModeFailurePacket() {
		Packet modeFailurePacket = new Packet();
		modeFailurePacket.setDescription(MODE_FAILURE_PACKET_DESCRIPTION);
		return modeFailurePacket;
	}

	public static Packet getEndOfStreamPacket() {
		Packet eosPacket = new Packet();
		eosPacket.setDescription(END_OF_STREAM_PACKET_DESCRIPTION);
		return eosPacket;
	}

}
