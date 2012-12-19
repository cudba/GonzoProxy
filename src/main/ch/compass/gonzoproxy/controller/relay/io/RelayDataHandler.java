package ch.compass.gonzoproxy.controller.relay.io;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import ch.compass.gonzoproxy.controller.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.controller.relay.modifier.PacketRegex;
import ch.compass.gonzoproxy.controller.relay.modifier.PacketRule;
import ch.compass.gonzoproxy.controller.relay.parser.ParsingHandler;
import ch.compass.gonzoproxy.controller.relay.settings.ConnectionState;
import ch.compass.gonzoproxy.controller.relay.settings.RelaySettings;
import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.model.relay.RelayDataModel;
import ch.compass.gonzoproxy.utils.PacketUtils;
import ch.compass.gonzoproxy.utils.PersistingUtils;

public class RelayDataHandler {

	private boolean isProcessingData = false;

	private LinkedBlockingQueue<Packet> receiverQueue = new LinkedBlockingQueue<Packet>();

	private LinkedBlockingQueue<Packet> commandSenderQueue = new LinkedBlockingQueue<Packet>();
	private LinkedBlockingQueue<Packet> responseSenderQueue = new LinkedBlockingQueue<Packet>();

	private ParsingHandler parsingHandler = new ParsingHandler();
	private PacketModifier packetModifier = new PacketModifier();

	private RelayDataModel relayDataModel = new RelayDataModel();

	private RelaySettings sessionSettings;

	public RelayDataHandler(RelaySettings sessionSettings) {
		this.sessionSettings = sessionSettings;

	}

	public void processRelayData() throws InterruptedException {
		isProcessingData = true;
		while (isProcessingData) {
			Packet receivedPacket = receiverQueue.take();

			if (!streamFailure(receivedPacket)) {
				Packet sendingPacket = processPacket(receivedPacket);
				addToSenderQueue(sendingPacket);
			} else {
				isProcessingData = false;
				clearQueues();
				throw new InterruptedException();
			}
		}

	}

	private Packet processPacket(Packet packet) {
		Packet sendingPacket = packet.clone();
		parsingHandler.tryParse(packet);
		relayDataModel.addPacket(packet);
		tryModifyPacket(sendingPacket);
	
		if (sendingPacket.isModified())
			relayDataModel.addPacket(sendingPacket);
	
		return sendingPacket;
	}

	private void tryModifyPacket(Packet packetToModify) {
		packetModifier.modifyByRegex(packetToModify);
		parsingHandler.tryParse(packetToModify);
		packetModifier.modifyByRule(packetToModify);
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

	private boolean streamFailure(Packet receivedPacket) {
		if (Arrays.equals(receivedPacket.getPacketData(),
				PacketUtils.END_OF_STREAM_PACKET)) {
			sessionSettings.setConnectionState(ConnectionState.EOS);
			return true;
		} else if (Arrays.equals(receivedPacket.getPacketData(),
				PacketUtils.MODE_FAILURE_PACKET)) {
			sessionSettings.setConnectionState(ConnectionState.MODE_FAILURE);
			return true;
		} else if (Arrays.equals(receivedPacket.getPacketData(),
				PacketUtils.STOP_PACKET)) {
			return true;
		}
		return false;
	}

	private void clearQueues() {
		receiverQueue.clear();
		commandSenderQueue.clear();
		responseSenderQueue.clear();
	}

	public void offer(Packet packet) {
		receiverQueue.offer(packet);
	}

	public Packet poll(PacketType type) throws InterruptedException {
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

	public void reparse() {
		parsingHandler.loadTemplates();
		for (Packet packet : relayDataModel.getPacketList()) {
			packet.updatePacketDataFromFields();
			packet.clearFields();
			parsingHandler.tryParse(packet);
		}
	}

	public RelayDataModel getSessionModel() {
		return relayDataModel;
	}

	@SuppressWarnings("unchecked")
	public void loadPacketsFromFile(File file) throws ClassNotFoundException,
			IOException {
		relayDataModel.setPacketList((ArrayList<Packet>) PersistingUtils
				.loadFile(file));
	}

	public void persistSessionData(File file) throws IOException {
		PersistingUtils.saveFile(file, relayDataModel.getPacketList());
	}

	public ArrayList<PacketRule> getPacketRules() {
		return packetModifier.getPacketRule();
	}

	public ArrayList<PacketRegex> getPacketRegex() {
		return packetModifier.getPacketRegex();
	}

	public void addRule(String packetName, String fieldName,
			String originalValue, String replacedValue, Boolean updateLength) {
		packetModifier.addRule(packetName, fieldName, originalValue,
				replacedValue, updateLength);
	}

	public void addRegex(String regex, String replaceWith, boolean isActive) {
		packetModifier.addRegex(regex, replaceWith, isActive);
	}

	public void persistRules() throws IOException {
		packetModifier.persistRules();
	}

	public void persistRegex() throws IOException {
		packetModifier.persistRegex();
	}

	public void clearSessionData() {
		relayDataModel.clearData();
		clearQueues();
	}

	public boolean isProcessingData() {
		return isProcessingData;
	}

}
