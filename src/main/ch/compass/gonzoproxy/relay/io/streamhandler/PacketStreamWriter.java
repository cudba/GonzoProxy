package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.wrapper.PacketWrapper;
import ch.compass.gonzoproxy.relay.settings.RelaySettings;
import ch.compass.gonzoproxy.relay.settings.TrapState;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class PacketStreamWriter implements Runnable {

	public enum State {
		TRAP, FORWARDING, SEND_ONE;
	}

	PacketWrapper wrapper;

	private OutputStream outputStream;
	private State state = State.FORWARDING;
	private String relayMode;

	private RelayDataHandler relayDataHandler;

	private PacketType forwardingType;

	public PacketStreamWriter(OutputStream outputStream,
			RelayDataHandler relayDataHandler, String mode,
			PacketType type) {
		this.outputStream = outputStream;
		this.relayDataHandler = relayDataHandler;
		this.relayMode = mode;
		this.forwardingType = type;
	}

	@Override
	public void run() {
		loadWrapper();
		sendPackets();
	}

	private void sendPackets() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				switch (state) {
				case TRAP:
					Thread.sleep(300);
					break;
				case FORWARDING:
					Packet sendingPacket = relayDataHandler
							.poll(forwardingType);
					if (sendingPacket != null)
						send(sendingPacket);
					break;
				case SEND_ONE:
					Packet sendOnePacket = relayDataHandler
							.poll(forwardingType);
					if (sendOnePacket != null)
						send(sendOnePacket);
					state = State.TRAP;
					break;
				}
			} catch (Exception e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	private void send(Packet packet) throws IOException {

		byte[] wrappedPacket = wrapper.wrap(packet);
		outputStream.write(wrappedPacket);
		outputStream.flush();
	}

	public void setTrapListener(RelaySettings sessionSettings) {
		sessionSettings.addTrapListener(new TrapListener() {

			@Override
			public void sendOnePacket(PacketType type) {
				if (forwardingType == type) {
					state = State.SEND_ONE;
				}
			}

			@Override
			public void trapStateChanged(TrapState trapState) {
				checkForTraps(trapState);
			}
		});
	}

	private void checkForTraps(TrapState trapState) {
		switch (trapState) {
		case TRAP:
			state = State.TRAP;
			break;
		case RESPONSE_TRAP:
			if (forwardingType == PacketType.RESPONSE) {
				state = State.TRAP;
			} else {
				state = State.FORWARDING;
			}
			break;
		case COMMAND_TRAP:
			if (forwardingType == PacketType.COMMAND) {
				state = State.TRAP;
			} else {
				state = State.FORWARDING;
			}
			break;
		case FORWARDING:
			state = State.FORWARDING;
			break;
		}
	}

	private Object selectMode(ClassLoader cl, String helper) {

		ResourceBundle bundle = ResourceBundle.getBundle("plugin");

		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String key = keys.nextElement();
			if (key.contains(helper)
					&& key.contains(relayMode)) {
				try {
					return cl.loadClass(bundle.getString(key))
							.newInstance();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private void loadWrapper() {
		ClassLoader cl = getClassloader(relayMode);
		if((wrapper = (PacketWrapper) selectMode(cl, "wrapper")) == null) {
			relayDataHandler.offer(PacketUtils.getModeFailurePacket());
			Thread.currentThread().interrupt();
		}
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
						relayDataHandler.offer(PacketUtils.getModeFailurePacket());
						Thread.currentThread().interrupt();
					}
					 URL[] urls = new URL[]{url};
					 return new URLClassLoader(urls);
				}
			}
			return null;
		}
}
