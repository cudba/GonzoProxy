package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.extractor.AsciiExtractor;
import ch.compass.gonzoproxy.relay.io.extractor.PacketExtractor;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class PacketStreamReader implements Runnable {
	
	private static final int BUFFER_SIZE = 1024;

	private PacketExtractor extractor;

	private InputStream inputStream;
	private PacketType forwardingType;
	private String relayMode;

	private RelayDataHandler relayDataHandler;

	public PacketStreamReader(InputStream inputStream,
			RelayDataHandler relayDataHandler, String mode,
			PacketType forwardingType) {
		this.inputStream = inputStream;
		this.relayDataHandler = relayDataHandler;
		this.relayMode = mode;
		this.forwardingType = forwardingType;
	}

	@Override
	public void run() {
		loadExtractor();
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
				offerEosPacket();
				throw new IOException();
			}

			readCompleted = buffer.length == 0;

			if (!readCompleted) {
				length = buffer.length;
				buffer = ByteArraysUtils.enlarge(buffer, BUFFER_SIZE);
			}
		}
	}
	
	private void offerEosPacket() {
		relayDataHandler.offer(PacketUtils.getEndOfStreamPacket());
	}
	
	private void offerModeFailurePacket() {
		relayDataHandler.offer(PacketUtils.getModeFailurePacket());
	}

	private void loadExtractor() {
		extractor = new AsciiExtractor();
//		ClassLoader cl = getClassloader(relayMode);
//		if((extractor = (PacketExtractor) selectMode(cl, "extractor")) == null) {
//			offerModeFailurePacket();
//			Thread.currentThread().interrupt();
//		}
	}
	
	private URLClassLoader getClassloader(String mode) {
		ResourceBundle bundle = ResourceBundle.getBundle("plugin");
			
			Enumeration<String> keys = bundle.getKeys();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				if (key.contains(".jar") && key.contains(mode)) {
					File extractorJar = new File("plugin/" + bundle.getString(key));
					URL url = null;
					try {
						url = extractorJar.toURI().toURL();
					} catch (MalformedURLException e) {
						offerModeFailurePacket();
						Thread.currentThread().interrupt();
					}
					 URL[] urls = new URL[]{url};
					 return new URLClassLoader(urls);
				}
			}

			return null;
		}

	private PacketExtractor selectMode(ClassLoader cl, String helper) {
	
		ResourceBundle bundle = ResourceBundle.getBundle("plugin");
	
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.contains(helper) && key.contains(relayMode)) {
				try {
					return (PacketExtractor) cl.loadClass(bundle.getString(key))
							.newInstance();
				} catch (InstantiationException | IllegalAccessException
						| ClassNotFoundException e) {
					offerModeFailurePacket();
					Thread.currentThread().interrupt();
				}
			}
		}
		return null;
	}

}
