package ch.compass.gonzoproxy.relay.parser;

import static org.junit.Assert.*;

import org.junit.Test;

import ch.compass.gonzoproxy.mvc.model.Packet;
import ch.compass.gonzoproxy.mvc.model.Field;

public class TemplateValidatorTest {
	
	//TODO: Fix: all fields are now essential for accepting templates

	@Test
	public void testSingleIdentifierTemplateAccepted() {
		TemplateValidator templateValidator = new TemplateValidator();
		
		String processingApduFake = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		String libnfcInput = "C-APDU 000d: 00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet apdu = new Packet(libnfcInput.getBytes());
		apdu.setOriginalPacketData(processingApduFake.getBytes());
		
		PacketTemplate templateFake = new PacketTemplate();
		templateFake.getFields().add(new Field("testFieldName", "00", "testDescription"));
		
		assertTrue(templateValidator.accept(templateFake, apdu));
	}
	
	@Test
	public void testSingleContentIdentifierAccepted(){
		
		TemplateValidator templateValidator = new TemplateValidator();
		
		String processingApduFake = "00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		String libnfcInput = "C-APDU 000d: 00 a4 04 00 07 d2 76 00 00 85 01 01 00";
		Packet apdu = new Packet(libnfcInput.getBytes());
		apdu.setOriginalPacketData(processingApduFake.getBytes());
		
		PacketTemplate templateFake = new PacketTemplate();
		templateFake.getFields().add(new Field("idField2", "00", "testDescription"));
		templateFake.getFields().add(new Field("idField 2", "a4", "idfield 2"));
		templateFake.getFields().add(new Field("Lc", "04", "Content Length"));
		templateFake.getFields().add(new Field("Ci", "00", "Content Identifier"));
		templateFake.getFields().add(new Field("Content identifier", "07 d2 76", "Content Length"));
		
		assertTrue(templateValidator.accept(templateFake, apdu));
		
	}

}
