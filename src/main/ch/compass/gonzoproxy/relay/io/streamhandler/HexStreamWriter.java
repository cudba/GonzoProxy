package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.OutputStream;

import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.wrapper.ApduWrapper;


public class HexStreamWriter {

	private ApduWrapper wrapper;

	public HexStreamWriter(ApduWrapper wrapper) {
		this.wrapper = wrapper;
	}
	
	public void sendPacket(OutputStream outputStream, Packet packet)
			throws IOException {

		byte[] wrappedPacket = wrapper.wrap(packet);
		outputStream.write(wrappedPacket);
		outputStream.flush();
	}
	
	

	
	
}
