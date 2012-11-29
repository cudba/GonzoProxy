package ch.compass.gonzoproxy.model;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import ch.compass.gonzoproxy.listener.SessionListener;

public class SessionSettings {
	
	private Preferences sessionPrefs = Preferences.userRoot().node(
			this.getClass().getName());
	
	private ArrayList<SessionListener> sessionListeners = new ArrayList<SessionListener>();
	
	private Boolean commandTrapped = false;
	private Boolean responseTrapped = false;
	private Boolean sendOneCommand;
	private Boolean sendOneResponse;
	private String mode;
	
	public void setSession(int listenPort, String remoteHost, int remotePort) {
		sessionPrefs.putInt("listenPort", listenPort);
		sessionPrefs.put("remoteHost", remoteHost);
		sessionPrefs.putInt("remotePort", remotePort);
		notifySessionChanged();
	}

	public int getListenPort() {
		return sessionPrefs.getInt("listenPort", 1234);
	}

	public String getRemoteHost() {
		return sessionPrefs.get("remoteHost", "127.0.0.1");
	}

	public int getRemotePort() {
		return sessionPrefs.getInt("remotePort", 4321);
	}
	
	private void notifySessionChanged() {
		for (SessionListener listener : sessionListeners) {
			listener.sessionChanged();
		}
	}
	
	public Boolean isResponseTrapped() {
		return responseTrapped;
	}

	public Boolean isCommandTrapped() {
		return commandTrapped;
	}

	public void setCommandTrapped(Boolean cmdTrap) {
		this.commandTrapped = cmdTrap;
	}

	public void setResponseTrapped(Boolean resTrap) {
		this.responseTrapped = resTrap;
	}

	public void sendOneCommand(boolean send) {
		this.sendOneCommand = send;
	}

	public void sendOneResponse(boolean send) {
		this.sendOneResponse = send;
	}

	public Boolean shouldSendOneCommand() {
		return sendOneCommand;
	}

	public Boolean shouldSendOneResponse() {
		return sendOneResponse;
	}
	
	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public String getMode() {
		return mode;
	}
}
