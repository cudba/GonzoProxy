package ch.compass.gonzoproxy.mvc.controller;

import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.mvc.model.SessionModel;
import ch.compass.gonzoproxy.mvc.model.Packet;
import ch.compass.gonzoproxy.mvc.model.SessionSettings;
import ch.compass.gonzoproxy.relay.io.CommunicationHandler;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.parser.ParsingHandler;

public class RelayHandler implements Runnable {
	
	private boolean sessionIsAlive = true;

	private LinkedTransferQueue<Packet> receiverQueue = new LinkedTransferQueue<Packet>();
	private LinkedTransferQueue<Packet> senderQueue = new LinkedTransferQueue<Packet>();
	
	
	private ParsingHandler parsingHandler = new ParsingHandler();
	private PacketModifier packetModifier;
	
	private CommunicationHandler communicationHandler;

	private SessionModel sessionModel;

	private SessionSettings sessionSettings;

	
	public RelayHandler(SessionModel sessionModel, SessionSettings sessionSettings, PacketModifier packetModifier) {
		this.sessionModel = sessionModel;
		this.sessionSettings = sessionSettings;
		this.packetModifier = packetModifier;
	}

	private void startCommunication() {
		communicationHandler = new CommunicationHandler(sessionSettings, receiverQueue, senderQueue);
		new Thread(communicationHandler).start();
	}

	private void handleRelayData() {
		
		while(sessionIsAlive){
			try {
				Packet receivedPacket = receiverQueue.take();
				
				Packet sendingPacket = processPacket(receivedPacket, sessionModel);
				senderQueue.tryTransfer(sendingPacket);
			
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

	@Override
	public void run() {
		startCommunication();
		handleRelayData();
	}

	public void killSession() {
		communicationHandler.killSession();
		sessionIsAlive = false;
		
	}

}
