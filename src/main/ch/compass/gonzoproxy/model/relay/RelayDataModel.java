package ch.compass.gonzoproxy.model.relay;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.listener.DataListener;
import ch.compass.gonzoproxy.model.packet.Packet;

public class RelayDataModel {

	private ArrayList<DataListener> dataListeners = new ArrayList<DataListener>();
	private ArrayList<Packet> sessionData = new ArrayList<Packet>();


	public void addDataListener(DataListener listener) {
		dataListeners.add(listener);
	}

	public void addPacket(Packet data) {
		sessionData.add(data);
		notifyPacketReceived(data);
	}

	public ArrayList<Packet> getPacketList() {
		return sessionData;
	}

	public void setPacketList(ArrayList<Packet> packetList) {
		this.sessionData = packetList;
		notifyNewPacketList();
	}

	public void clearData() {
		sessionData.clear();
		notifyDataCleared();
	}

	private void notifyDataCleared() {
		for (DataListener listener : dataListeners) {
			listener.packetsCleared();
		}
	}

	private void notifyPacketReceived(Packet receivedPacket) {
		for (DataListener listener : dataListeners) {
			listener.packetReceived(receivedPacket);
		}
	}

	private void notifyNewPacketList() {
		for (DataListener listener : dataListeners) {
			listener.newList();
		}		
	}
}
