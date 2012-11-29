//package ch.compass.gonzoproxy.relay.io;
//import java.io.ByteArrayInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.util.Queue;
//
//import org.junit.Test;
//
//import ch.compass.gonzoproxy.mvc.model.Packet;
//import ch.compass.gonzoproxy.relay.io.extractor.ByteExtractor;
//import ch.compass.gonzoproxy.relay.io.extractor.LibNfcApduExtractor;
//import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamHandler;
//import ch.compass.gonzoproxy.relay.io.wrapper.LibNfcApduWrapper;
//import static org.junit.Assert.*;
//
//public class ApduStreamHandlerTest {
//
//	@Test
//	public void readApduTest() throws IOException{
//		PacketStreamHandler streamHandler = new PacketStreamHandler(new LibNfcApduExtractor(), new LibNfcApduWrapper());
//		
//		byte[] inputStream = "#C-APDU 000d: 00 a4 04 00 07 d2 76 00 00 85 01 01 00 \n#C-APDU 000d: 00 a4 04 00 07 d2 76 00 00 85 01 01 00 \n".getBytes();
//		byte[] originalApduFake = "#C-APDU 000d: 00 a4 04 00 07 d2 76 00 00 85 01 01 00 \n".getBytes();
//		byte[] preamleFake = "#C-APDU 000d: ".getBytes();
//		byte[] plainApduFake = "00 a4 04 00 07 d2 76 00 00 85 01 01 00".getBytes();
//		byte[] trailerFake = " \n".getBytes();
//		
//		InputStream in = new ByteArrayInputStream(inputStream);
//		
//		Queue<Packet> queue = streamHandler.readApdu(in);
//		Packet apdu = queue.poll();
//		assertArrayEquals(originalApduFake, apdu.getStreamInput());
//		assertArrayEquals(plainApduFake, apdu.getOriginalPacketData());
//		assertArrayEquals(preamleFake, apdu.getPreamble());
//		assertArrayEquals(trailerFake, apdu.getTrailer());
//	}
//	
//	@Test
//	public void readApduEtTest() throws IOException{
//		PacketStreamHandler streamHandler = new PacketStreamHandler(new ByteExtractor(), new LibNfcApduWrapper());
//		
//		byte[] inputStream = new byte[]{'#', 0x00,(byte)0xa4, 0x04, 0x00, 0x07, (byte)0xd2, 0x76, 0x00, 0x00, (byte)0x85, 0x01, 0x01, 0x00, '$'};
//		byte[] originalApduFake = "2300a4040007d27600008501010024".getBytes();
//		byte[] plainApduFake = "00a4040007d276000085010100".getBytes();
//		
//		InputStream in = new ByteArrayInputStream(inputStream);
//		
//		Queue<Packet> queue = streamHandler.readApdu(in);
//		Packet apdu = queue.poll();
//		assertArrayEquals(originalApduFake, apdu.getStreamInput());
//		assertArrayEquals(plainApduFake, apdu.getOriginalPacketData());
//	}
//}
