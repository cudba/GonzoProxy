package ch.compass.gonzoproxy.relay.io;

import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.relay.settings.RelaySettings;

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
