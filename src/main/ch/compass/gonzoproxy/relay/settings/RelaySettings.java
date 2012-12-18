package ch.compass.gonzoproxy.relay.settings;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.packet.PacketType;

public class RelaySettings {
	
//	public enum SessionState {
//		DISCONNECTED("Disconnected"),
//		CONNECTION_REFUSED("Could not connect to target"),
//		CONNECTING("Waiting for initiator ..."),
//		FORWARDING("Forwarding"),
//		TRAP("Trapped"),
//		COMMAND_TRAP("Command trapped"),
//		RESPONSE_TRAP("Response trapped"), 
//		CONNECTED("Connection established"), 
//		CONNECTION_LOST("Connection lost"), 
//		EOS("End of stream reached"), 
//		MODE_FAILURE("Failed to instantiate chosen relay mode");
//		
//		private String description;
//
//		private SessionState(String description) {
//			this.description = description;
//		}
//		
//		public String getDescription() {
//			return description;
//		}
//	}
	
	
	
	private TrapState trapState = TrapState.FORWARDING;
	private ConnectionState connectionState = ConnectionState.DISCONNECTED;
	
	private Preferences sessionPrefs = Preferences.userRoot().node(
			this.getClass().getName());
	
	private ArrayList<StateListener> stateListeners = new ArrayList<StateListener>();
	private ArrayList<TrapListener> trapListeners = new ArrayList<TrapListener>();
	
	private String relayMode;
	
	public void setConnectionParameter(int listenPort, String remoteHost, int remotePort) {
		sessionPrefs.putInt("listenPort", listenPort);
		sessionPrefs.put("remoteHost", remoteHost);
		sessionPrefs.putInt("remotePort", remotePort);
		notifyStateChanged(connectionState);
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

	public void setRelayMode(String mode) {
		this.relayMode = mode;
	}
	
	public String getRelayMode() {
		return relayMode;
	}

	public void addTrapListener(TrapListener trapListener) {
		trapListeners.add(trapListener);
	}
	
//	public SessionState getSessionState() {
//		return sessionState;
//	}
//	
//	
//	public void setConnectionState(SessionState sessionState) {
//		this.sessionState = sessionState;
//		notifyStateChanged();
//	}
	
	public void setConnectionState(ConnectionState state) {
		connectionState = state;
		notifyStateChanged(state);
	}

	public void addStateListener(
			StateListener stateListener) {
		stateListeners.add(stateListener);
	}
	
//	public void setTrapState(SessionState sessionState) {
//		this.sessionState = sessionState;
//		notifyStateChanged();
//		notifyTrapChanged();
//	}
	
	
	public void setTrapState(TrapState state) {
		trapState = state;
		notifyStateChanged(state);
		notifyTrapState();
	}
	
	public TrapState getTrapState() {
		return trapState;
	}
	

	private void notifyStateChanged(RelayState state) {
		for (StateListener stateListener : stateListeners) {
			stateListener.sessionStateChanged(state);
		}
	}

	private void notifySendOneResponse() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.sendOnePacket(PacketType.RESPONSE);
		}
	}

	private void notifySendOneCommand() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.sendOnePacket(PacketType.COMMAND);
		}
	}

	private void notifyTrapState() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.trapStateChanged(trapState);
		}
	}

	public void updateForwardingMode() {
		notifyTrapState();
		notifyStateChanged(trapState);
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}
}
