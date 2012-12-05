package ch.compass.gonzoproxy.model;

import java.util.ArrayList;

import ch.compass.gonzoproxy.listener.SessionListener;

public class SessionModel {

	private ArrayList<SessionListener> sessionListeners = new ArrayList<SessionListener>();
	private ArrayList<Packet> sessionData = new ArrayList<Packet>();


	public void addSessionListener(SessionListener listener) {
		sessionListeners.add(listener);
	}

	public void addPacket(Packet data) {
		sessionData.add(data);
		notifyPacketReceived(data);
	}

	public ArrayList<Packet> getPacketList() {
		return sessionData;
	}

	public void clearData() {
		sessionData.clear();
		notifyDataCleared();
	}

	private void notifyDataCleared() {
		for (SessionListener listener : sessionListeners) {
			listener.packetCleared();
		}
	}

	private void notifyPacketReceived(Packet receivedPacket) {
		for (SessionListener listener : sessionListeners) {
			listener.packetReceived(receivedPacket);
		}
	}

	public void addList(ArrayList<Packet> readObject) {
		this.sessionData = readObject;
		notifyNewPacketList();
	}

	private void notifyNewPacketList() {
		for (SessionListener listener : sessionListeners) {
			listener.newList();
		}		
	}
}
