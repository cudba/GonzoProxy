package ch.compass.gonzoproxy.relay;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.ForwardingType;

public class RelaySettings {
	
	public enum SessionState {
		DISCONNECTED("Disconnected"),
		CONNECTION_REFUSED("Could not connect to target"),
		CONNECTING("Waiting for initiator ..."),
		FORWARDING("Forwarding"),
		TRAP("Trapped"),
		COMMAND_TRAP("Command trapped"),
		RESPONSE_TRAP("Response trapped"), 
		CONNECTED("Connection established"), CONNECTION_LOST("Connection lost");
		
		private String description;

		private SessionState(String description) {
			this.description = description;
		}
		
		public String getDescription() {
			return description;
		}
	}
	
	private SessionState sessionState = SessionState.DISCONNECTED;
	
	private Preferences sessionPrefs = Preferences.userRoot().node(
			this.getClass().getName());
	
	private ArrayList<StateListener> stateListeners = new ArrayList<StateListener>();
	private ArrayList<TrapListener> trapListeners = new ArrayList<TrapListener>();
	
	private String mode;
	
	public void setSession(int listenPort, String remoteHost, int remotePort) {
		sessionPrefs.putInt("listenPort", listenPort);
		sessionPrefs.put("remoteHost", remoteHost);
		sessionPrefs.putInt("remotePort", remotePort);
		notifyStateListeners();
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
	
	public void sendOneCommand() {
		notifySendOneCommand();
	}

	public void sendOneResponse() {
		notifySendOneResponse();
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public String getMode() {
		return mode;
	}

	public void addTrapListener(TrapListener trapListener) {
		trapListeners.add(trapListener);
	}
	
	public SessionState getSessionState() {
		return sessionState;
	}
	
	public void setSessionState(SessionState sessionState) {
		this.sessionState = sessionState;
		notifyStateListeners();
	}

	public void addSessionStateListener(
			StateListener stateListener) {
		stateListeners.add(stateListener);
	}
	
	public void setTrapState(SessionState sessionState) {
		this.sessionState = sessionState;
		notifyStateListeners();
		notifyTrapChanged();
	}

	private void notifyStateListeners() {
		for (StateListener stateListener : stateListeners) {
			stateListener.sessionStateChanged(sessionState);
		}
	}

	private void notifySendOneResponse() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.sendOnePacket(ForwardingType.RESPONSE);
		}
	}

	private void notifySendOneCommand() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.sendOnePacket(ForwardingType.COMMAND);
		}
	}

	private void notifyTrapChanged() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.checkTrapChanged(sessionState);
		}
	}
}
