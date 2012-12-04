package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.extractor.ApduExtractor;

public class PacketStreamReader implements Runnable {

	private HexStreamReader streamReader;

	private InputStream inputStream;
	private SessionSettings sessionSettings;
	private ForwardingType forwardingType;

	private RelayDataHandler relayDataHandler;

	public PacketStreamReader(InputStream inputStream,
			RelayDataHandler relayDataHandler, SessionSettings sessionSettings,
			ForwardingType forwardingType) {
		this.inputStream = inputStream;
		this.relayDataHandler = relayDataHandler;
		this.sessionSettings = sessionSettings;
		this.forwardingType = forwardingType;
		configureStreamReader();
	}

	@Override
	public void run() {
		readPackets();
	}

	private void readPackets() {
		try {
			while (!Thread.currentThread().isInterrupted())
				streamReader.readPackets(inputStream, relayDataHandler,
						forwardingType);
		} catch (IOException e) {
			System.out.println("closed");
			Thread.currentThread().interrupt();
		}
	}

	private Object selectMode(ClassLoader cl, String helper) {

		ResourceBundle bundle = ResourceBundle.getBundle("plugin");

		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String element = keys.nextElement();
			if (element.contains(helper) && element.contains(sessionSettings.getMode())) {
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
		streamReader = new HexStreamReader(extractor);
	}

}
