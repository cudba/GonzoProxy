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

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.relay.RelayManager;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.modifier.FieldRule;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;
import ch.compass.gonzoproxy.utils.PersistingUtils;

public class RelayController {

	private PacketModifier packetModifier = new PacketModifier();
	private SessionModel sessionModel = new SessionModel();
	private String[] modes;

	private RelayManager relayManager = new RelayManager();

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


	public void newSession(String portListen, String remoteHost,
			String remotePort, String mode) {
		stopRunningSession();
		prepareSession();
		relayManager.generateNewSessionParameters(portListen, remoteHost, remotePort, mode);
		new Thread(relayManager).start();
	}

	private void prepareSession() {
		RelayDataHandler relayDataHandler = new RelayDataHandler();
		relayDataHandler.setSessionParameters(sessionModel,
				packetModifier);
		relayManager.setDataHandler(relayDataHandler);
	}

	public void stopRunningSession() {
			relayManager.killSession();
	}

	public SessionModel getSessionModel() {
		return sessionModel;
	}

	public void addModifierRule(String packetName, String fieldName,
			String originalValue, String replacedValue, Boolean updateLength) {
		FieldRule fieldRule = new FieldRule(fieldName, originalValue,
				replacedValue);
		packetModifier.addRule(packetName, fieldRule, updateLength);
		persistRules();
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

	public String[] getModes() {
		return modes;
	}
	
	public void reparsePackets(){
		RelayDataHandler fakedDataHandler = new RelayDataHandler();
		fakedDataHandler.reParse(sessionModel.getPacketList());
	}
	
	@SuppressWarnings("unchecked")
	public void openFile(File file) {
		stopRunningSession();
		try (FileInputStream fin = new FileInputStream(file);
				ObjectInputStream ois = new ObjectInputStream(fin)) {
			ArrayList<Packet> loadedPacketStream = (ArrayList<Packet>) ois
					.readObject();
			sessionModel.addList(loadedPacketStream);
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

//	public SessionSettings getSessionSettings() {
//		return sessionSettings;
//	}

	public ArrayList<PacketRule> getPacketRules() {
		return packetModifier.getRuleSets();
	}

	public void persistRules() {
		try {
			packetModifier.persistRules();
		} catch (IOException e) {
			//TODO: save failed notification
			e.printStackTrace();
		}
		
	}
	
	public void persistPackets(File file){
		try {
			PersistingUtils.saveFile(file, sessionModel.getPacketList());
		} catch (IOException e) {
			//TODO: SAVE FAILED
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	public void openPackets(File file){
		try {
			sessionModel.addList((ArrayList<Packet>) PersistingUtils.loadFile(file));
		} catch (ClassNotFoundException | IOException e) {
			// TODO couldnt open file
			e.printStackTrace();
		}
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

}
