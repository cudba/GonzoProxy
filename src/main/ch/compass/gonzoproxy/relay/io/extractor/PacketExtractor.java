package ch.compass.gonzoproxy.relay.io.extractor;

import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;

public interface PacketExtractor {

	public byte[] extractPacketsToHandler(byte[] buffer, RelayDataHandler relayDataHandler,
			int readBytes, PacketType forwardingType);

	public String getName();

}
