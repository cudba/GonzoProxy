package ch.compass.gonzoproxy.relay.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.compass.gonzoproxy.mvc.model.Field;
import ch.compass.gonzoproxy.mvc.model.Packet;

public class ParsingHandlerTest {
	ParsingHandler parserHanlder;
	
	@Before public void initialize() {
		parserHanlder = new ParsingHandler();
	    }

	@Test
	public void testProcessKnownLibNfcPacket() {

		String fakePlainPacket = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		String libnfcInput = "C-Packet 000d: 00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet packet = new Packet(libnfcInput.getBytes());
		packet.setOriginalPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Case 4 Select Command";
		String trimmedPacket = "00a4040007d2 76 00 00 85 01 0100";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getOriginalPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());
	}

	@Test
	public void testProcessKnownLibNfcPacketCustomLength() {

		String fakePlainPacket = "00 a4 04 00 07 d2 76 00 00 85 01 00";
		String libnfcInput = "C-Packet 000c: 00 a4 04 00 07 d2 76 00 00 85 01 00";
		Packet packet = new Packet(libnfcInput.getBytes());
		packet.setOriginalPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Case 3 Select Command";
		String trimmedPacket = "00a4040007d2 76 00 00 85 01 00";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getOriginalPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());

	}

	@Test
	public void testProcessUnknownLibNfcPacket() {

		String fakePlainPacket = "ff ff ff ff";
		String libnfcInput = "C-Packet 0004: ff ff ff ff";
		Packet packet = new Packet(libnfcInput.getBytes());
		packet.setOriginalPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		assertEquals(1, packet.getFields().size());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getOriginalPacketData());
		assertEquals(new String(fakePlainPacket), packet.getFields().get(0).getValue());

	}

	@Test
	public void testProcessFourBytesContentIdentifier() {

		String fakePlainPacket = "77 07 82 00 07 94 76 00 0a 85";
		String libnfcInput = "#R-Packet 000a: 77 07 8200 07 94 76 00 0a 85";
		Packet packet = new Packet(libnfcInput.getBytes());
		packet.setOriginalPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Get Processing Options Response";
		String trimmedPacket = "77078200079476 00 0a85";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getOriginalPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());

	}

	@Test
	public void testProcessSelectResponse() {

		String fakePlainPacket = "6f 2f 84 0e 32 50 41 59 2e 53 59 53 2e 44 44 46 30 31 a5 1d bf 0c 1a 61 18 4f 07 a0 00 00 00 04 10 10 87 01 01 50 0a 4d 61 73 74 65 72 43 61 72 64 90 00";
		String libnfcInput = "#R-Packet 0033: 6f 2f 84 0e 32 50 41 59 2e 53 59 53 2e 44 44 46 30 31 a5 1d bf 0c 1a 61 18 4f 07 a0 00 00 00 04 10 10 87 01 01 50 0a 4d 61 73 74 65 72 43 61 72 64 90 00";

		Packet packet = new Packet(libnfcInput.getBytes());
		packet.setOriginalPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Select Response";
		String trimmedPacket = "6f2f840e 32 50 41 59 2e 53 59 53 2e 44 44 46 30 31a51d bf 0c 1a 61 18 4f 07 a0 00 00 00 04 10 10 87 01 01 50 0a 4d 61 73 74 65 72 43 61 72 649000";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getOriginalPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());

	}
}
