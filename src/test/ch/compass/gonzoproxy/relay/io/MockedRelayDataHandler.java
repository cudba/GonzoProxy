package ch.compass.gonzoproxy.relay.io;

import ch.compass.gonzoproxy.controller.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.controller.relay.settings.RelaySettings;
import ch.compass.gonzoproxy.model.packet.Packet;

public class MockedRelayDataHandler extends RelayDataHandler {

	Packet extractedPacket;

	public MockedRelayDataHandler(RelaySettings sessionSettings) {
		super(sessionSettings);
	}

	@Override
	public void offer(Packet packet) {
		extractedPacket = packet;
	}

	public Packet getExtractedPacket() {
		if (extractedPacket != null)
			return extractedPacket;
		return new Packet();
	}

}
