package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.extractor.PacketExtractor;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class PacketStreamReader implements Runnable {
	
	private static final int BUFFER_SIZE = 1024;

	private static final String EOS = "End Of Stream";

	private PacketExtractor extractor;

	private InputStream inputStream;
	private ForwardingType forwardingType;
	private String mode;

	private RelayDataHandler relayDataHandler;

	public PacketStreamReader(InputStream inputStream,
			RelayDataHandler relayDataHandler, String mode,
			ForwardingType forwardingType) {
		this.inputStream = inputStream;
		this.relayDataHandler = relayDataHandler;
		this.mode = mode;
		this.forwardingType = forwardingType;
		loadExtractor();
	}

	@Override
	public void run() {
		readPackets();
	}

	private void readPackets() {
		try {
			while (!Thread.currentThread().isInterrupted())
				read();
		} catch (IOException e) {
			Thread.currentThread().interrupt();
		}
	}
	
	private void read()
			throws IOException {
		byte[] buffer = new byte[BUFFER_SIZE];

		int length = 0;
		int readBytes = 0;

		boolean readCompleted = false;

		while (!readCompleted) {

			if (length == buffer.length) {
				ByteArraysUtils.enlarge(buffer);
			}

			if ((readBytes = inputStream.read(buffer, length, buffer.length
					- length)) != -1) {
				length += readBytes;
				buffer = extractor.extractPacketsToHandler(buffer,
						relayDataHandler, length, forwardingType);
			}else {
				sendEosPacket();
			}

			readCompleted = buffer.length == 0;

			if (!readCompleted) {
				length = buffer.length;
				buffer = ByteArraysUtils.enlarge(buffer, BUFFER_SIZE);
			}
		}
	}

	private void sendEosPacket() {
		Packet eosPacket = new Packet();
		eosPacket.setDescription(EOS);
		relayDataHandler.offer(eosPacket);
	}

	private void loadExtractor() {
		ClassLoader cl = GonzoProxy.class.getClassLoader();
		extractor = (PacketExtractor) selectMode(cl, "extractor");
	}

	private Object selectMode(ClassLoader cl, String helper) {
	
		ResourceBundle bundle = ResourceBundle.getBundle("plugin");
	
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String element = keys.nextElement();
			if (element.contains(helper) && element.contains(mode)) {
				try {
					return cl.loadClass(bundle.getString(element))
							.newInstance();
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return null;
	}

}
