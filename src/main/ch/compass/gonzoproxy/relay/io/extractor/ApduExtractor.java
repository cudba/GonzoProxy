package ch.compass.gonzoproxy.relay.io.extractor;

import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;

public interface ApduExtractor {

	/**
	 * @param buffer contains the read bytes
	 * 
	 * @param apduQueue Queue to store APDUS
	 * 
	 * @return 	Returns empty buffer if read is complete, in case some bytes are missing, a buffer 
	 * 			containing the unfinished content is returned.
	 * 			Notice:	Its important that the returned buffer is not bigger than the content inside
	 * 					
	 */
	
	public byte[] extractPacketsToQueue(byte[] buffer, LinkedTransferQueue<Packet> apduQueue,
			int readBytes, ForwardingType forwardingType);

	public String getName();

}
