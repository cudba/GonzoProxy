package ch.compass.gonzoproxy.relay.modifier;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import ch.compass.gonzoproxy.model.Field;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class PacketModifierTest {

	@Test
	public void testAddNewRule() {

		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Modified Field", "0f", "a5");
		packetModifier.addRule("Modified Packet", rule, false);

		FieldRule actualAddedRule = packetModifier.getRuleSets().get(0).getRules()
				.get(0);
		assertEquals(rule, actualAddedRule);
	}

	@Test
	public void testAddExistingRule() {
		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Modified Field", "0f", "a5");
		packetModifier.addRule("Modified Packet", rule, false);

		FieldRule addedRule = new FieldRule("Modified Field", "0f", "ff");
		packetModifier.addRule("Modified Packet", addedRule, false);
		
		int actualRuleSetCount = packetModifier.getRuleSets().size();
		int actualRuleCount = packetModifier.getRuleSets().get(0).getRules().size();
		
		assertEquals(1, actualRuleSetCount);
		assertEquals(2, actualRuleCount);
		FieldRule actualAddedRule = packetModifier.getRuleSets().get(0)
				.getRules().get(0);
		assertEquals(addedRule, actualAddedRule);
	}

	@Test
	public void testModifyPacketReplacePatternNoLengthUpdate() {

		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Modified Field", "0f", "a5");

		packetModifier.addRule("Modified Packet", rule, false);

		String fakePlainApdu = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet receivedPacket = new Packet();
		receivedPacket.setOriginalPacketData(fakePlainApdu.getBytes());
		receivedPacket.setDescription("Modified Packet");

		receivedPacket.addField(new Field("FooName", "00", "FooDescription"));
		receivedPacket.addField(new Field("Modified Field", "0f",
				"FooDescriptoin"));

		packetModifier.modifyByRule(receivedPacket);

		String expectedReplacedValue = rule.getReplacedValue();
		String actualReplacedValue = receivedPacket.getFields().get(1)
				.getValue();

		assertEquals(expectedReplacedValue, actualReplacedValue);

	}

	@Test
	public void testModifyPacketReplaceWholeValue() {

		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Modified Field", "", "a5");

		packetModifier.addRule("Modified Packet", rule, false);

		String fakePlainApdu = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet receivedPacket = new Packet();
		receivedPacket.setOriginalPacketData(fakePlainApdu.getBytes());
		receivedPacket.setDescription("Modified Packet");

		receivedPacket.addField(new Field("FooName", "00", "FooDescription"));
		receivedPacket.addField(new Field("Modified Field", "0f c7",
				"FooDescriptoin"));

		packetModifier.modifyByRule(receivedPacket);

		String expectedReplacedValue = rule.getReplacedValue();
		String actualReplacedValue = receivedPacket.getFields().get(1).getValue();
		

		assertEquals(expectedReplacedValue, actualReplacedValue);

	}
	
	@Test
	public void testNoModificationWithInactiveRule() {
		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Modified Field", "0f", "a5");
		rule.setActive(false);

		packetModifier.addRule("Modified Packet", rule, false);

		String fakePlainApdu = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet receivedPacket = new Packet();
		receivedPacket.setOriginalPacketData(fakePlainApdu.getBytes());
		receivedPacket.setDescription("Modified Packet");

		receivedPacket.addField(new Field("FooName", "00", "FooDescription"));
		receivedPacket.addField(new Field("Modified Field", "0f",
				"FooDescriptoin"));

		packetModifier.modifyByRule(receivedPacket);

		String expectedValue = "0f";
		String actualValue = receivedPacket.getFields().get(1).getValue();

		assertEquals(expectedValue, actualValue);

	}
	
	@Test
	public void testModifyPacketReplacePatternUpdateLength() {

		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Foo Content", "0f c7", "a5");

		packetModifier.addRule("Modified Packet", rule, true);

		String fakePlainApdu = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet receivedPacket = new Packet();
		receivedPacket.setOriginalPacketData(fakePlainApdu.getBytes());
		receivedPacket.setDescription("Modified Packet");

		receivedPacket.addField(new Field("Lc", "03", "Content Length"));
		receivedPacket.addField(new Field("Foo Content", "0f c7 b8",
				"FooDescriptoin"));
		receivedPacket.setSize(3);

		packetModifier.modifyByRule(receivedPacket);

		String expectedReplacedValue = rule.getReplacedValue() + " b8";
		String actualReplacedValue = receivedPacket.getFields().get(1).getValue();
		
		
		Field contentLengthField = new Field();

		for (Field field : receivedPacket.getFields()) {
			if(field.getName().equals(PacketUtils.CONTENT_LENGTH_FIELD))
				contentLengthField = field;
		}
		
		assertEquals(expectedReplacedValue, actualReplacedValue);
		assertEquals("02", contentLengthField.getValue());
		assertEquals(2, receivedPacket.getSize());
	}
	
	@Test
	public void testModifyPacketReplacePatternNoUpdateLength() {

		PacketModifier packetModifier = new PacketModifier();

		FieldRule rule = new FieldRule("Foo Content", "0f c7", "a5 c7 84");

		packetModifier.addRule("Modified Packet", rule, false);

		String fakePlainApdu = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet receivedPacket = new Packet();
		receivedPacket.setOriginalPacketData(fakePlainApdu.getBytes());
		receivedPacket.setDescription("Modified Packet");

		receivedPacket.addField(new Field("Lc", "03", "Content Length"));
		receivedPacket.addField(new Field("Foo Content", "0f c7 b8",
				"FooDescriptoin"));
		receivedPacket.setSize(3);

		packetModifier.modifyByRule(receivedPacket);

		String expectedReplacedValue = rule.getReplacedValue() + " b8";
		String actualReplacedValue = receivedPacket.getFields().get(1).getValue();
		
		
		Field contentLengthField = new Field();

		for (Field field : receivedPacket.getFields()) {
			if(field.getName().equals(PacketUtils.CONTENT_LENGTH_FIELD))
				contentLengthField = field;
		}
		
		assertEquals(expectedReplacedValue, actualReplacedValue);
		assertEquals("03", contentLengthField.getValue());
		assertEquals(4, receivedPacket.getSize());
	}
	
	@Test
	public void testModifyByRegex() {

		PacketModifier packetModifier = new PacketModifier();

		String fakePlainApdu = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet receivedPacket = new Packet();
		receivedPacket.setOriginalPacketData(fakePlainApdu.getBytes());
		receivedPacket.setDescription("Modified Packet");
		receivedPacket.setSize(3);
		
		PacketRegex regex = new PacketRegex("a4 04", "ff ff cc cc");
		packetModifier.addRegex(regex, true);
		
		packetModifier.modifyByRegex(receivedPacket);
		
		String expectedValue = "00 ff ff cc cc 00 07 d2 76 00 00 85 01 01 00";
		String actualValue = new String(receivedPacket.getOriginalPacketData());
		
		assertEquals(expectedValue, actualValue);
	}
	
	
}
