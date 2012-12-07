package ch.compass.gonzoproxy.relay.io.wrapper;

import java.util.Arrays;

import ch.compass.gonzoproxy.model.Packet;

public class LibNfcWrapper implements PacketWrapper {
	//TODO: make local
	private byte[] trailer;
	private byte[] plainPacket;
	private byte[] preamble;

	public byte[] wrap(Packet packet) {
		this.trailer = packet.getTrailer();
		this.preamble = computePreamble(packet);


		this.plainPacket = packet.getPacketDataAsBytes();

		int newSize = preamble.length + plainPacket.length + trailer.length;

		byte[] wrappedPacket = Arrays.copyOf(preamble, newSize);

		System.arraycopy(plainPacket, 0, wrappedPacket, preamble.length,
				plainPacket.length);
		System.arraycopy(trailer, 0, wrappedPacket, preamble.length
				+ plainPacket.length, trailer.length);

		return wrappedPacket;
	}

	private byte[] computePreamble(Packet packet) {
		byte[] newPreamble = packet.getPreamble();
		int lastSizeIndex = newPreamble.length - 1 - 2;

		int packetSize = packet.getSize();
		
		if(packetSize > 0) {
			String strPacketSize = Integer.toHexString(packetSize);
			byte[] newSize = strPacketSize.getBytes();
			int lastIndexNew = newSize.length - 1;
			
			for (int i = 0; i < newSize.length; i++) {
				newPreamble[lastSizeIndex - i] = newSize[lastIndexNew - i];
			}
			
			packet.setPreamble(newPreamble);
			
			return newPreamble;
		}
		return new byte[0];

	}

	@Override
	public String getName() {
		return "libnfc";
	}

}
