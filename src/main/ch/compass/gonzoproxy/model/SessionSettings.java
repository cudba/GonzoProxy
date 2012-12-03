package ch.compass.gonzoproxy.model;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import ch.compass.gonzoproxy.listener.SessionListener;
import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.listener.TrapListener;

public class SessionSettings {
	
	public enum SessionState {
		DISCONNECTED("disconnected"),
		CONNECTING("connecting..."),
		FORWARDING("forwarding"),
		TRAP("trapped"),
		COMMAND_TRAP("command trapped"),
		RESPONSE_TRAP("response trapped");
		
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
	private TrapListener trapListener;
	
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
		trapListener.sendOneCommand();
	}

	public void sendOneResponse() {
		trapListener.sendOneResponse();
	}

	public void setMode(String mode) {
		this.mode = mode;
	}
	
	public String getMode() {
		return mode;
	}

	public void setTrapListener(TrapListener trapListener) {
		this.trapListener = trapListener;
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
	
	private void notifyStateListeners() {
		for (StateListener stateListener : stateListeners) {
			stateListener.sessionStateChanged(sessionState.getDescription());
		}
	}

	public void setTrapState(SessionState sessionState) {
		this.sessionState = sessionState;
		notifyStateListeners();
		trapListener.checkTrapChanged();
	}
}
