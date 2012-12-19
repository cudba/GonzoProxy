package ch.compass.gonzoproxy.controller.relay.settings;

import java.util.ArrayList;
import java.util.prefs.Preferences;

import ch.compass.gonzoproxy.model.listener.StateListener;
import ch.compass.gonzoproxy.model.listener.TrapListener;
import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.model.state.ConnectionState;
import ch.compass.gonzoproxy.model.state.RelayState;
import ch.compass.gonzoproxy.model.state.TrapState;

public class RelaySettings {
	
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
	
	public void addStateListener(
			StateListener stateListener) {
		stateListeners.add(stateListener);
	}
	
	public void setTrapState(TrapState state) {
		trapState = state;
		notifyStateChanged(state);
		notifyTrapState();
	}
	
	public TrapState getTrapState() {
		return trapState;
	}
	

	public void setConnectionState(ConnectionState state) {
		connectionState = state;
		notifyStateChanged(state);
	}

	public ConnectionState getConnectionState() {
		return connectionState;
	}

	public void updateForwardingMode() {
		notifyTrapState();
		notifyStateChanged(trapState);
	}

	private void notifyStateChanged(RelayState state) {
		for (StateListener stateListener : stateListeners) {
			stateListener.sessionStateChanged(state);
		}
	}

	private void notifyTrapState() {
		for (TrapListener trapListener : trapListeners) {
			trapListener.trapStateChanged(trapState);
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
}
