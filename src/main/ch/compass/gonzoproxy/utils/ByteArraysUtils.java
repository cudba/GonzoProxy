package ch.compass.gonzoproxy.utils;

import java.util.ArrayList;
import java.util.Arrays;

public class ByteArraysUtils {

	public static byte[] trim(byte[] array, int fromIndex, int length) {
		if (validInput(array, fromIndex, length)) {
			byte[] newArray = new byte[length];
			System.arraycopy(array, fromIndex, newArray, 0, length);
			return newArray;
		}
		return array;
	}

	public static ArrayList<Integer> getDelimiterIndices(byte[] buffer,
			byte[] delimiter) {
		ArrayList<Integer> indices = new ArrayList<Integer>();
		for (int i = 0; i <= buffer.length - delimiter.length;) {
			boolean matches = true;
			int currentIndex = i;
			for (int j = 0; j < delimiter.length; j++) {
				if (buffer[currentIndex] != delimiter[j])
					matches = false;
				currentIndex ++;
			}
			if(matches) {
				indices.add(i);
				i = currentIndex;
			} else {
				i++;
			}

		}
		return indices;
	}
	
	public static byte[] enlarge(byte[] array) {
		byte[] newArray = new byte[array.length << 1];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}
	
	public static byte[] enlarge(byte[] array, int size){
		byte[] newArray = new byte[size];
		System.arraycopy(array, 0, newArray, 0, array.length);
		return newArray;
	}
	
	public static byte[] merge(byte[] source, byte[] append) {
		byte[] merged = Arrays.copyOf(source, source.length + append.length);
		
		System.arraycopy(append, 0, merged, source.length , append.length);
		return merged;
	}

	private static boolean validInput(byte[] array, int fromIndex, int length) {
		return array.length >= fromIndex + length && length >= 0;
	}
	
	public static byte[] byteHexToAsciiHex(byte[] byteData) {
	    StringBuilder sb = new StringBuilder();
	    for (byte b : byteData) {
	        sb.append(String.format("%02X ", b));
	        sb.append(" ");
	    }
	    
	    String hexRepresentation = sb.toString();
	    
	    hexRepresentation = hexRepresentation.substring(0, hexRepresentation.length() -1);
	    	
		return hexRepresentation.getBytes();
	}
	
	public static byte[] asciiHexToByteHex(String hexString) {
		hexString = hexString.replaceAll("\\s", "");
	    int len = hexString.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
	                             + Character.digit(hexString.charAt(i+1), 16));
	    }
	    return data;
	}
}
