package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.extractor.ApduExtractor;

public class HexStreamReader implements Runnable {

	private PacketStreamReader streamReader;

	private InputStream inputStream;
	private String mode;
	private ForwardingType forwardingType;

	private LinkedTransferQueue<Packet> receiverQueue;

	public HexStreamReader(InputStream inputStream,
			LinkedTransferQueue<Packet> receiverQueue, String mode,
			ForwardingType forwardingType) {
		this.inputStream = inputStream;
		this.receiverQueue = receiverQueue;
		this.mode = mode;
		this.forwardingType = forwardingType;
		configureStreamReader();
	}

	@Override
	public void run() {
		readPackets();
	}

	private void readPackets() {
		try {
			streamReader
					.readPackets(inputStream, receiverQueue, forwardingType);
		} catch (IOException e) {
		}
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

	private void configureStreamReader() {
		ClassLoader cl = GonzoProxy.class.getClassLoader();
		ApduExtractor extractor = (ApduExtractor) selectMode(cl, "extractor");
		streamReader = new PacketStreamReader(extractor);
	}

}
