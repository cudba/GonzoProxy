package ch.compass.gonzoproxy.listener;

import ch.compass.gonzoproxy.model.Packet;



public interface SessionListener {

	public void sessionChanged();

	public void packetCleared();

	public void packetReceived(Packet receivedPacket);

	public void newList();

}
