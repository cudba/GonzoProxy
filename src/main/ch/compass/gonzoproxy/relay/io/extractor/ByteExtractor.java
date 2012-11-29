package ch.compass.gonzoproxy.relay.io.extractor;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class ByteExtractor implements ApduExtractor {

	private static final char DELIMITER = '#';
	private static final char EOC = '$';

	private String bytesToHexString(byte[] bytes) {  
	    StringBuilder sb = new StringBuilder(bytes.length * 2);  
	  
	    Formatter formatter = new Formatter(sb);  
	    for (byte b : bytes) {  
	        formatter.format("%02x", b);  
	    }  
	    formatter.close();
	    return sb.toString();  
	}

	@Override
	public byte[] extractPacketsToQueue(byte[] buffer, LinkedTransferQueue<Packet> packetQueue,
			int readBytes, ForwardingType forwardingType) {
		ArrayList<Integer> indices = ByteArraysUtils.getDelimiterIndices(buffer,
				DELIMITER);

		int startIndex = 0;
		int endIndex = 0;

		for (int i = 0; i < indices.size() - 1; i++) {
			startIndex = indices.get(i);
			endIndex = indices.get(i + 1);
			int size = endIndex - startIndex;
			byte[] plainPacket = ByteArraysUtils.trim(buffer, startIndex, size);
			
			String packetAsHexString = bytesToHexString(plainPacket);
			packetAsHexString = packetAsHexString.substring(2, packetAsHexString.length()-2);
			byte[]asciiPacket = packetAsHexString.getBytes();
			
			Packet packet = new Packet(asciiPacket);
			packet.setOriginalPacketData(asciiPacket);
			
			packetQueue.add(packet);
			
		}
		
		byte[] singlePacket = ByteArraysUtils.trim(buffer, endIndex, readBytes - endIndex);
		
		if(packetIsComplete(singlePacket)){
			String packetAsHexString = bytesToHexString(singlePacket);
			System.out.println(packetAsHexString);
			Packet packet = new Packet(packetAsHexString.getBytes());
			String plainPacket = packetAsHexString.substring(2, packetAsHexString.length() -2);
			System.out.println(plainPacket);
			packet.setOriginalPacketData(plainPacket.getBytes());
			packetQueue.add(packet);
		}

		
		//TODO: fix
		return new byte[0];
	}

	private boolean packetIsComplete(byte[] singlePacket) {
		return singlePacket[singlePacket.length - 1] == EOC;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}  
}
