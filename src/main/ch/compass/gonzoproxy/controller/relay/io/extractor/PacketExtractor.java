package ch.compass.gonzoproxy.controller.relay.io.extractor;

import ch.compass.gonzoproxy.controller.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.model.packet.PacketType;

public interface PacketExtractor {

	public byte[] extractPacketsToHandler(byte[] buffer, RelayDataHandler relayDataHandler,
			int readBytes, PacketType forwardingType);

	public String getName();

}
