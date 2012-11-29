package ch.compass.gonzoproxy.relay.io;

import static org.junit.Assert.assertEquals;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import ch.compass.gonzoproxy.mvc.model.Packet;
import ch.compass.gonzoproxy.relay.parser.ParsingHandler;

public class PacketPersistingHandlerTest {
	
	
	@SuppressWarnings("unchecked")
	@Test
	public void testSaveAndLoadPackets() throws IOException, ClassNotFoundException {
		
		ParsingHandler parserHanlder = new ParsingHandler();
		
		
		String fakePlainApdu = "77 07 82 00 07 94 76 00 0a 85";
		String libnfcInput = "#R-APDU 000a: 77 07 8200 07 94 76 00 0a 85";
		Packet apdu = new Packet(libnfcInput.getBytes());
		apdu.setOriginalPacketData(fakePlainApdu.getBytes());

		parserHanlder.tryParse(apdu);
		
		ArrayList<Packet> packets = new ArrayList<Packet>();
		packets.add(apdu);
		
            System.out.println("serializing list");
            FileOutputStream fout = new FileOutputStream("packetList.dat");
            ObjectOutputStream oos = new ObjectOutputStream(fout);
            oos.writeObject(packets);
            oos.close();
            
            FileInputStream fin = new FileInputStream("packetList.dat");
            ObjectInputStream ois = new ObjectInputStream(fin);
            List<Packet> list = (ArrayList<Packet>) ois.readObject();
            ois.close();
            
            for (Packet packet : list) {
				System.out.println(packet.getDescription());
			}
            assertEquals(apdu.getDescription(), list.get(0).getDescription());
		

	}


}
