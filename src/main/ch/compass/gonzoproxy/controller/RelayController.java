package ch.compass.gonzoproxy.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.ResourceBundle;

import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.relay.RelayHandler;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.modifier.FieldRule;

public class RelayController {
	
	private PacketModifier packetModifier = new PacketModifier();
	private SessionModel sessionModel = new SessionModel();
	private SessionSettings sessionSettings = new SessionSettings();
	private String[] modes;

	private RelayHandler relayHandler;

	public RelayController() {
		loadModes();
	}

	private void loadModes() {
		ArrayList<String> inputModes = new ArrayList<>();

		ResourceBundle bundle = ResourceBundle.getBundle("plugin");

		Enumeration<String> keys = bundle.getKeys();
		while (keys.hasMoreElements()) {
			String element = keys.nextElement();
			if (element.contains("name")) {
				inputModes.add(bundle.getString(element));
			}
		}

		this.modes = inputModes.toArray(new String[2]);
	}

	public void startRelaySession() {
		relayHandler = new RelayHandler(sessionModel, sessionSettings, packetModifier);
		new Thread(relayHandler).start();
	}

	public void newSession(String portListen, String remoteHost,
			String remotePort, String mode) {
		clearOldSession();
		generateNewSessionDescription(portListen, remoteHost, remotePort, mode);
		startRelaySession();

	}

	private void generateNewSessionDescription(String portListen,
			String remoteHost, String remotePort, String mode) {
		sessionSettings.setSession(Integer.parseInt(portListen), remoteHost,
				Integer.parseInt(remotePort));
		sessionModel.clearData();
		sessionSettings.setMode(mode);
	}

	public void clearOldSession() {
		if (relayHandler != null) {
			relayHandler.killSession();
		}
	}
//
//	public void stopRelaySession() {
//		relaySession.stopForwarder();
//	}

	public SessionModel getSessionModel() {
		return sessionModel;
	}

	public void addModifierRule(String packetName, String fieldName,
			String originalValue, String replacedValue, Boolean updateLength) {
		FieldRule fieldRule = new FieldRule(fieldName, originalValue, replacedValue);
		packetModifier.addRule(packetName, fieldRule, updateLength);
	}

	public void changeCommandTrap() {
		if (sessionSettings.commandIsTrapped()) {
			sessionSettings.setCommandTrapped(false);
		} else {
			sessionSettings.setCommandTrapped(true);
		}
	}

	public void changeResponseTrap() {
		if (sessionSettings.responseIsTrapped()) {
			sessionSettings.setResponseTrapped(false);
		} else {
			sessionSettings.setResponseTrapped(true);
		}
	}

	public void sendOneCmd() {
		sessionSettings.sendOneCommand(true);
	}

	public void sendOneRes() {
		sessionSettings.sendOneResponse(true);
	}

	public String[] getModes() {
		return modes;
	}

	@SuppressWarnings("unchecked")
	public void openFile(File file) {
		FileInputStream fin;
		try {
			fin = new FileInputStream(file);
			ObjectInputStream ois = new ObjectInputStream(fin);
			sessionModel.addList((ArrayList<Packet>) ois.readObject());
			ois.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public void saveFile(File file) {
		FileOutputStream fout;
		try {
			fout = new FileOutputStream(file);
			ObjectOutputStream oos = new ObjectOutputStream(fout);
			oos.writeObject(sessionModel.getPacketList());
			oos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public PacketModifier getPacketModifier() {
		return packetModifier;
	}

	public SessionSettings getSessionSettings() {
		return sessionSettings;
	}

}
