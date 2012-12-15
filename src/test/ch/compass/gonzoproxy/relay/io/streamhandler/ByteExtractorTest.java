package ch.compass.gonzoproxy.relay.io.streamhandler;

import static org.junit.Assert.assertArrayEquals;

import org.junit.Test;

import ch.compass.gonzoproxy.model.packet.Packet;
import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.relay.io.MockedRelayDataHandler;
import ch.compass.gonzoproxy.relay.io.extractor.ByteExtractor;
import ch.compass.gonzoproxy.relay.settings.RelaySettings;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class ByteExtractorTest {

	@Test
	public void testExtractByteStreamToPacket() {

		MockedRelayDataHandler dataHandler = new MockedRelayDataHandler(
				new RelaySettings());

		ByteExtractor byteExtractor = new ByteExtractor();

		byte[] inputStream = new byte[] { (byte) 0xaa, (byte) 0xaa,
				(byte) 0xaa, (byte) 0xaa, (byte) 0xa4, 0x74, (byte) 0x93,
				(byte) 0xab, (byte) 0xbb, (byte) 0xbb, (byte) 0xbb,
				(byte) 0xbb };

		byteExtractor.extractPacketsToHandler(inputStream, dataHandler,
				inputStream.length, PacketType.COMMAND);

		byte[] expectedPreamble = new byte[] { (byte) 0xaa, (byte) 0xaa,
				(byte) 0xaa, (byte) 0xaa };

		byte[] packetData = new byte[] { (byte) 0xa4, 0x74, (byte) 0x93,
				(byte) 0xab };
		byte[] expectedPacketData = ByteArraysUtils
				.byteToParsableAsciiHex(packetData);

		byte[] expectedTrailer = new byte[] { (byte) 0xbb, (byte) 0xbb,
				(byte) 0xbb, (byte) 0xbb};
		
		Packet extractedPacket = dataHandler.getExtractedPacket();
		
		assertArrayEquals(expectedPreamble, extractedPacket.getPreamble());
		assertArrayEquals(expectedPacketData, extractedPacket.getPacketData());
		assertArrayEquals(expectedTrailer, extractedPacket.getTrailer());
	}
}
