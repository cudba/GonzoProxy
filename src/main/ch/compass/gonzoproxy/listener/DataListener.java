package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.Packet;



public interface DataListener {

	public void packetsCleared();

	public void packetReceived(Packet receivedPacket);

	public void newList();

}
