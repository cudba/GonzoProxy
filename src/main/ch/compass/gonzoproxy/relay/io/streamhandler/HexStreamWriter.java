package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.wrapper.ApduWrapper;

public class HexStreamWriter implements Runnable {

	public enum State {
		TRAP, FORWARDING, SEND_ONE;
	}

	private PacketStreamWriter streamWriter;

	private OutputStream outputStream;
	private String mode;
	private State state = State.FORWARDING;

	private RelayDataHandler relayDataHandler;

	private ForwardingType type;

	public HexStreamWriter(OutputStream outputStream,
			RelayDataHandler relayDataHandler, String mode, ForwardingType type) {
		this.outputStream = outputStream;
		this.relayDataHandler = relayDataHandler;
		this.mode = mode;
		this.type = type;
		configureStreamWriter();
	}

	@Override
	public void run() {
		sendPackets();
	}

	private void sendPackets() {
		while (!Thread.currentThread().isInterrupted()) {
			try {
				switch (state) {
				case TRAP:
					Thread.yield();
					break;
				case FORWARDING:
					Packet sendingPacket = relayDataHandler.poll(type);
					if (sendingPacket != null)
						streamWriter.sendPacket(outputStream, sendingPacket);
					break;
				case SEND_ONE:
					Packet sendOnePacket = relayDataHandler.poll(type);
					if (sendOnePacket != null)
						streamWriter.sendPacket(outputStream, sendOnePacket);
						state = State.TRAP;
					break;
				}
			} catch (InterruptedException | IOException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	private void configureStreamWriter() {
		ClassLoader cl = GonzoProxy.class.getClassLoader();
		ApduWrapper wrapper = (ApduWrapper) selectMode(cl, "wrapper");

		streamWriter = new PacketStreamWriter(wrapper);
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
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	public void setState(State state) {
		this.state = state;
	}
}
