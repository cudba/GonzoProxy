package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.model.SessionSettings.SessionState;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.wrapper.PacketWrapper;

public class PacketStreamWriter implements Runnable {

	public enum State {
		TRAP, FORWARDING, SEND_ONE;
	}

	PacketWrapper wrapper;

	private OutputStream outputStream;
	private SessionSettings sessionSettings;
	private State state = State.FORWARDING;

	private RelayDataHandler relayDataHandler;

	private ForwardingType forwardingType;

	public PacketStreamWriter(OutputStream outputStream,
			RelayDataHandler relayDataHandler, SessionSettings sessionSettings,
			ForwardingType type) {
		this.outputStream = outputStream;
		this.relayDataHandler = relayDataHandler;
		this.sessionSettings = sessionSettings;
		this.forwardingType = type;
		addTrapListener();
		loadWrapper();
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
			} catch (InterruptedException | IOException e) {
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
				Thread.currentThread().interrupt();
			}
		}

	}
	
	private void send(Packet packet)
			throws IOException {

		byte[] wrappedPacket = wrapper.wrap(packet);
		outputStream.write(wrappedPacket);
		outputStream.flush();
	}

	private void addTrapListener() {
		sessionSettings.setTrapListener(new TrapListener() {
	
			@Override
			public void sendOnePacket(ForwardingType type) {
				if (forwardingType == type) {
					state = State.SEND_ONE;
				}
			}
	
			@Override
			public void checkTrapChanged() {
				checkForTraps();
			}
		});
	}

	private void checkForTraps() {
		switch (sessionSettings.getSessionState()) {
		case TRAP:
			state = State.TRAP;
			break;
		case RESPONSE_TRAP:
			if (forwardingType == ForwardingType.RESPONSE) {
				state = State.TRAP;
			} else {
				state = State.FORWARDING;
			}
			break;
		case COMMAND_TRAP:
			if (forwardingType == ForwardingType.COMMAND) {
				state = State.TRAP;
			} else {
				state = State.FORWARDING;
			}
			break;
		case FORWARDING:
			state = State.FORWARDING;
			break;
		default:
			break;
		}
	}

	private Object selectMode(ClassLoader cl, String helper) {
	
		ResourceBundle bundle = ResourceBundle.getBundle("plugin");
	
		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String element = keys.nextElement();
			if (element.contains(helper)
					&& element.contains(sessionSettings.getMode())) {
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

	private void loadWrapper() {
		ClassLoader cl = GonzoProxy.class.getClassLoader();
		wrapper = (PacketWrapper) selectMode(cl, "wrapper");
	}
}
