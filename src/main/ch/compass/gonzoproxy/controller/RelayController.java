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
import ch.compass.gonzoproxy.model.SessionSettings.SessionState;
import ch.compass.gonzoproxy.relay.RelayManager;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.modifier.FieldRule;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;
import ch.compass.gonzoproxy.utils.FileUtils;

public class RelayController {

	private PacketModifier packetModifier = new PacketModifier();
	private SessionModel sessionModel = new SessionModel();
	private SessionSettings sessionSettings = new SessionSettings();
	private String[] modes;

	private RelayManager relayManager;

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
		RelayDataHandler relayDataHandler = new RelayDataHandler();
		relayDataHandler.setSessionParameters(sessionModel,
				packetModifier);
		relayManager = new RelayManager(relayDataHandler, sessionSettings);
		 new Thread(relayManager).start();
	}

	public void newSession(String portListen, String remoteHost,
			String remotePort, String mode) {
		stopRunningSession();
		generateNewSessionParameters(portListen, remoteHost, remotePort, mode);
		startRelaySession();

	}

	private void generateNewSessionParameters(String portListen,
			String remoteHost, String remotePort, String mode) {
		sessionSettings.setSession(Integer.parseInt(portListen), remoteHost,
				Integer.parseInt(remotePort));
		sessionModel.clearData();
		sessionSettings.setMode(mode);
	}

	public void stopRunningSession() {
		if (relayManager != null ){
			relayManager.killSession();
		}
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
		switch (sessionSettings.getSessionState()) {
		case COMMAND_TRAP:
			sessionSettings.setTrapState(SessionState.FORWARDING);
			break;
		case FORWARDING:
			sessionSettings.setTrapState(SessionState.COMMAND_TRAP);
			break;
		case RESPONSE_TRAP:
			sessionSettings.setTrapState(SessionState.TRAP);
			break;
		case TRAP:
			sessionSettings.setTrapState(SessionState.RESPONSE_TRAP);
			break;
		default:
			break;
		}
	}

	public void responseTrapChanged() {
		switch (sessionSettings.getSessionState()) {
		case RESPONSE_TRAP:
			sessionSettings.setTrapState(SessionState.FORWARDING);
			break;
		case FORWARDING:
			sessionSettings.setTrapState(SessionState.RESPONSE_TRAP);
			break;
		case COMMAND_TRAP:
			sessionSettings.setTrapState(SessionState.TRAP);
			break;
		case TRAP:
			sessionSettings.setTrapState(SessionState.COMMAND_TRAP);
			break;
		default:
			break;
		}
	}

	public void sendOneCmd() {
		sessionSettings.sendOneCommand();
	}

	public void sendOneRes() {
		sessionSettings.sendOneResponse();
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

	public SessionSettings getSessionSettings() {
		return sessionSettings;
	}

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
			FileUtils.saveFile(file, sessionModel.getPacketList());
		} catch (IOException e) {
			//TODO: SAVE FAILED
			e.printStackTrace();
		}

	}
	
	@SuppressWarnings("unchecked")
	public void openPackets(File file){
		try {
			sessionModel.addList((ArrayList<Packet>) FileUtils.loadFile(file));
		} catch (ClassNotFoundException | IOException e) {
			// TODO couldnt open file
			e.printStackTrace();
		}
	}

}
