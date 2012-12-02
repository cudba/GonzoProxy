package ch.compass.gonzoproxy.relay;

import java.util.ArrayList;
import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.relay.io.CommunicationHandler;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.parser.ParsingHandler;

public class RelayHandler implements Runnable {
	
	private boolean sessionIsAlive = true;

	private LinkedTransferQueue<Packet> receiverQueue = new LinkedTransferQueue<Packet>();

	private LinkedTransferQueue<Packet> commandSenderQueue = new LinkedTransferQueue<Packet>();
	private LinkedTransferQueue<Packet> responseSenderQueue = new LinkedTransferQueue<Packet>();

	private ParsingHandler parsingHandler = new ParsingHandler();
	private PacketModifier packetModifier;

	private CommunicationHandler communicationHandler;

	private SessionModel sessionModel;

	private SessionSettings sessionSettings;

	@Override
	public void run() {
		sessionIsAlive = true;
		startCommunication();
		handleRelayData();
	}

	public void setSessionParameters(SessionModel sessionModel,
			SessionSettings sessionSettings, PacketModifier packetModifier) {
		this.sessionModel = sessionModel;
		this.sessionSettings = sessionSettings;
		this.packetModifier = packetModifier;
	}

	public void killSession() {
		if (communicationHandler != null)
			communicationHandler.killSession();
		
		sessionIsAlive = false;
	}

	private void startCommunication() {
		communicationHandler = new CommunicationHandler(sessionSettings,
				receiverQueue, commandSenderQueue, responseSenderQueue);
		new Thread(communicationHandler).start();
	}

	private void handleRelayData() {

		while (sessionIsAlive) {
			try {
				Packet receivedPacket = receiverQueue.take();

				Packet sendingPacket = processPacket(receivedPacket,
						sessionModel);
				addToSenderQueue(sendingPacket);

			} catch (InterruptedException e) {
				e.printStackTrace();
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
}
