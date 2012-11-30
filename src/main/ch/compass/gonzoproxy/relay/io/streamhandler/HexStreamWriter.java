package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TransferQueue;

import ch.compass.gonzoproxy.GonzoProxy;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.wrapper.ApduWrapper;

public class HexStreamWriter implements Runnable {
	
	public enum State {
		TRAP,
		FORWARDING;
	}

	private PacketStreamWriter streamWriter;

	private OutputStream outputStream;
	private String mode;
	private State state = State.FORWARDING;

	private TransferQueue<Packet> senderQueue;

	public HexStreamWriter(OutputStream outputStream,
			TransferQueue<Packet> senderQueue, String mode) {
		this.outputStream = outputStream;
		this.senderQueue = senderQueue;
		this.mode = mode;
		configureStreamWriter();
	}

	@Override
	public void run() {
		sendPackets();
	}

	private void sendPackets() {
		while (true) {
			try {
				switch (state) {
				case TRAP:
					Thread.yield();
					break;
				case FORWARDING:
					Packet packet = senderQueue.poll(200, TimeUnit.MILLISECONDS);
					if(packet != null)
					streamWriter.sendPacket(outputStream, packet);
				}
			} catch (InterruptedException | IOException e) {
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
