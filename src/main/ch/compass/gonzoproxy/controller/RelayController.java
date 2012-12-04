package ch.compass.gonzoproxy.controller;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.relay.GonzoRelayService;
import ch.compass.gonzoproxy.relay.RelayService;
import ch.compass.gonzoproxy.relay.modifier.FieldRule;
import ch.compass.gonzoproxy.relay.modifier.PacketRegex;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;

public class RelayController {

	private String[] relayModes;

	private Thread relayManagerThread;
	private RelayService relayManager = new GonzoRelayService();

	public RelayController() {
		loadPossibleRelayModes();
	}

	public void newSession(String portListen, String remoteHost,
			String remotePort, String mode) {
		stopRunningSession();
		relayManager.generateNewSessionParameters(portListen, remoteHost,
				remotePort, mode);
		relayManagerThread = new Thread(relayManager);
		relayManagerThread.start();
	}

	public void stopRunningSession() {
		if (relayManagerThread != null && relayManagerThread.isAlive())
			relayManager.killSession();
	}

	public SessionModel getSessionModel() {
		return relayManager.getSessionModel();
	}

	public String[] getPossibleRelayModes() {
		return relayModes;
	}

	public ArrayList<PacketRule> getPacketRules() {
		return relayManager.getPacketRules();
	}

	public ArrayList<PacketRegex> getPacketRegex() {
		return relayManager.getPacketRegex();
	}

	public void addModifierRule(String packetName, String fieldName,
			String originalValue, String replacedValue, Boolean updateLength) {
		FieldRule fieldRule = new FieldRule(fieldName, originalValue,
				replacedValue);
		relayManager.addRule(packetName, fieldRule, updateLength);
		persistRules();
	}

	public void addRegex(String regex, String replaceWith, boolean isActive) {
		PacketRegex packetRegex = new PacketRegex(regex, replaceWith);
		relayManager.addRegex(packetRegex, isActive);
		persistRegex();
	}

	public void commandTrapChanged() {
		relayManager.commandTrapChanged();
	}

	public void responseTrapChanged() {
		relayManager.responseTrapChanged();
	}

	public void sendOneCmd() {
		relayManager.sendOneCmd();
	}

	public void sendOneRes() {
		relayManager.sendOneRes();
	}

	public int getCurrentListenPort() {
		return relayManager.getCurrentListenPort();
	}

	public String getCurrentRemoteHost() {
		return relayManager.getCurrentRemoteHost();
	}

	public int getCurrentRemotePort() {
		return relayManager.getCurrentRemotePort();
	}

	public void addSessionStateListener(StateListener stateListener) {
		relayManager.addSessionStateListener(stateListener);
	}

	public void reparsePackets() {
		relayManager.reParse();
	}

	public void persistSessionData(File file) {
		try {
			relayManager.persistSessionData(file);
		} catch (IOException e) {
			// notify user
			e.printStackTrace();
		}
	}

	public void loadPacketsFromFile(File file) {
		try {
			relayManager.loadPacketsFromFile(file);
		} catch (ClassNotFoundException | IOException e) {
			// notify user
		}
	}

	public void persistRules() {
		try {
			relayManager.persistRules();
		} catch (IOException e) {
			// TODO: save failed notification
		}

	}

	public void persistRegex() {
		try {
			relayManager.persistRegex();
		} catch (IOException e) {
			// TODO: PERSISTNG FAIL
		}
	}

	private void loadPossibleRelayModes() {
		ArrayList<String> inputModes = new ArrayList<>();

		ResourceBundle bundle = ResourceBundle.getBundle("plugin");

		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String element = keys.nextElement();
			if (element.contains("name")) {
				inputModes.add(bundle.getString(element));
			}
		}

		this.relayModes = inputModes.toArray(new String[2]);
	}

}
