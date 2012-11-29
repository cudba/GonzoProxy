package ch.compass.gonzoproxy.mvc.listener;

import ch.compass.gonzoproxy.mvc.model.Packet;



public interface SessionListener {

	public void sessionChanged();

	public void packetCleared();

	public void packetReceived(Packet receivedPacket);

	public void newList();

}
