package ch.compass.gonzoproxy.model.packet;

public class PacketDataSettings {
	
	public static final int DEFAULT_FIELDLENGTH = 1;
	public static final int ENCODING_OFFSET = 2;
	public static final int WHITESPACE_OFFSET = 1;
	
	public static final byte[] END_OF_STREAM_PACKET = "End Of Stream".getBytes();
	public static final byte[] MODE_FAILURE_PACKET = "Mode loading error".getBytes();

}
