package ch.compass.gonzoproxy.relay.io;

import java.util.ArrayList;
import java.util.concurrent.LinkedTransferQueue;
import java.util.concurrent.TimeUnit;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.parser.ParsingHandler;

public class RelayDataHandler implements Runnable {

	private LinkedTransferQueue<Packet> receiverQueue = new LinkedTransferQueue<Packet>();

	private LinkedTransferQueue<Packet> commandSenderQueue = new LinkedTransferQueue<Packet>();
	private LinkedTransferQueue<Packet> responseSenderQueue = new LinkedTransferQueue<Packet>();

	private ParsingHandler parsingHandler = new ParsingHandler();
	private PacketModifier packetModifier;

	private SessionModel sessionModel;

	@Override
	public void run() {
		handleRelayData();
	}

	public void setSessionParameters(SessionModel sessionModel,
			PacketModifier packetModifier) {
		this.sessionModel = sessionModel;
		this.packetModifier = packetModifier;
	}

	private void handleRelayData() {

		while (!Thread.currentThread().isInterrupted()) {
			try {
				Packet receivedPacket = receiverQueue.take();

				Packet sendingPacket = processPacket(receivedPacket,
						sessionModel);
				addToSenderQueue(sendingPacket);

			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

	}

	private Packet processPacket(Packet packet, SessionModel sessionModel) {
		parsingHandler.tryParse(packet);
		sessionModel.addPacket(packet);
		Packet processedPacket = packetModifier.modifyByRule(packet);
		if (processedPacket.isModified())
			sessionModel.addPacket(processedPacket);

		return processedPacket;
	}

	private void addToSenderQueue(Packet sendingPacket) {
		switch (sendingPacket.getType()) {
		case COMMAND:
			commandSenderQueue.offer(sendingPacket);
			break;

		case RESPONSE:
			responseSenderQueue.offer(sendingPacket);
			break;
		}

	}

	public void reParse(ArrayList<Packet> loadedPacketStream) {

		for (Packet packet : loadedPacketStream) {
			packet.clearFields();
			parsingHandler.tryParse(packet);
		}
	}

	public void offer(Packet packet) {
		receiverQueue.offer(packet);
	}

	public Packet poll(ForwardingType type) throws InterruptedException {
		Packet sendingPacket = null;
		switch (type) {
		case COMMAND:
			sendingPacket = commandSenderQueue.poll(200, TimeUnit.MILLISECONDS);
			break;
		case RESPONSE:
			sendingPacket = responseSenderQueue
					.poll(200, TimeUnit.MILLISECONDS);
			break;
		}
		return sendingPacket;
	}
}
