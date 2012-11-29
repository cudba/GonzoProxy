package ch.compass.gonzoproxy.mvc.model;

import java.util.ArrayList;
import java.util.concurrent.Semaphore;
import java.util.prefs.Preferences;

import ch.compass.gonzoproxy.mvc.listener.SessionListener;
import ch.compass.gonzoproxy.relay.modifier.PacketModifier;

public class SessionModel {

	private ArrayList<SessionListener> sessionListeners = new ArrayList<SessionListener>();
	private ArrayList<Packet> sessionData = new ArrayList<Packet>();
	private Semaphore lock = new Semaphore(1);
	


	public void addSessionListener(SessionListener listener) {
		sessionListeners.add(listener);
	}

	public void addPacket(Packet data) {
		try {
			lock.acquire();
			sessionData.add(data);
			lock.release();
			notifyPacketReceived(data);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public ArrayList<Packet> getPacketList() {
		return sessionData;
	}

	public void clearData() {
		sessionData.clear();
		notifyClear();
	}

	private void notifyClear() {
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
		notifyNewList();
	}

	private void notifyNewList() {
		for (SessionListener listener : sessionListeners) {
			listener.newList();
		}		
	}
}
