package ch.compass.gonzoproxy.relay.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import ch.compass.gonzoproxy.controller.relay.parser.ParsingHandler;
import ch.compass.gonzoproxy.model.packet.Field;
import ch.compass.gonzoproxy.model.packet.Packet;

public class ParsingHandlerTest {
	ParsingHandler parserHanlder;

	@Before
	public void initialize() {
		parserHanlder = new ParsingHandler();
	}

	@Test
	public void testProcessKnownLibNfcPacket() {

		String fakePlainPacket = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet packet = new Packet();
		packet.setPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Case 4 Select Command";
		String trimmedPacket = "00a4040007d2 76 00 00 85 01 0100";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());
	}

	@Test
	public void testProcessKnownLibNfcPacketCustomLength() {

		String fakePlainPacket = "00 a4 04 00 07 d2 76 00 00 85 01 00";
		Packet packet = new Packet();
		packet.setPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Case 3 Select Command";
		String trimmedPacket = "00a4040007d2 76 00 00 85 01 00";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());

	}

	@Test
	public void testProcessUnknownLibNfcPacket() {

		String fakePlainPacket = "ff ff ff ff";
		Packet packet = new Packet();
		packet.setPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		assertEquals(1, packet.getFields().size());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getPacketData());
		assertEquals(new String(fakePlainPacket), packet.getFields().get(0)
				.getValue());

	}

	@Test
	public void testProcessCustomContentIdentifierLength() {

		String fakePlainPacket = "a5 7f hallo 07 83 00 07 ff ff 00 00";
		Packet packet = new Packet();
		packet.setPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Test ci length";
		String trimmedPacket = "a5 7fhallo0783 00 07ff ff00 00";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());

	}

	@Test
	public void testProcessSelectResponse() {

		String fakePlainPacket = "6f 2f 84 0e 32 50 41 59 2e 53 59 53 2e 44 44 46 30 31 a5 1d bf 0c 1a 61 18 4f 07 a0 00 00 00 04 10 10 87 01 01 50 0a 4d 61 73 74 65 72 43 61 72 64 90 00";

		Packet packet = new Packet();
		packet.setPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "Select Response";
		String trimmedPacket = "6f2f840e 32 50 41 59 2e 53 59 53 2e 44 44 46 30 31a51d bf 0c 1a 61 18 4f 07 a0 00 00 00 04 10 10 87 01 01 50 0a 4d 61 73 74 65 72 43 61 72 649000";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());

	}

	@Test
	public void testEqualContentIdentifiers() {
		String fakePlainPacket = "66 07 83 00 07 83 ff 83 00";
		Packet packet = new Packet();
		packet.setPacketData(fakePlainPacket.getBytes());

		parserHanlder.tryParse(packet);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : packet.getFields()) {
			mergedFields.append(field.getValue());
		}

		String packetDescription = "TestEqualCi";
		String trimmedPacket = "66078300 0783ff8300";
		assertEquals(packetDescription, packet.getDescription());
		assertArrayEquals(fakePlainPacket.getBytes(), packet.getPacketData());
		assertEquals(trimmedPacket, mergedFields.toString());
	}
}
