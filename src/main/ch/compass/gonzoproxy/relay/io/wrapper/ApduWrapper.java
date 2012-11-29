package ch.compass.gonzoproxy.relay.io.wrapper;

import ch.compass.gonzoproxy.mvc.model.Packet;

public interface ApduWrapper {

	public byte[] wrap(Packet apdu);

	public String getName();

}
