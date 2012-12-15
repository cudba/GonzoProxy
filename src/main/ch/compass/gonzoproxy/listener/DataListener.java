package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.packet.Packet;



public interface DataListener {

	public void packetsCleared();

	public void packetReceived(Packet receivedPacket);

	public void newList();

}
