package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.model.SessionSettings.SessionState;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.extractor.ApduExtractor;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class PacketStreamReader implements Runnable {
	
	private static final int BUFFER_SIZE = 1024;

	private ApduExtractor extractor;

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
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
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
				buffer = extractor.extractPacketsToQueue(buffer,
						relayDataHandler, length, forwardingType);
			}

			readCompleted = buffer.length == 0;

			if (!readCompleted) {
				length = buffer.length;
				buffer = ByteArraysUtils.enlarge(buffer, BUFFER_SIZE);
			}
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

	private void loadExtractor() {
		ClassLoader cl = GonzoProxy.class.getClassLoader();
		extractor = (ApduExtractor) selectMode(cl, "extractor");
	}

}
