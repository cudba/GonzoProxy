package ch.compass.gonzoproxy.relay.io.extractor;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;

public interface ApduExtractor {

	/**
	 * @param buffer contains the read bytes
	 * 
	 * @param relayDataHandler Queue to store APDUS
	 * 
	 * @return 	Returns empty buffer if read is complete, in case some bytes are missing, a buffer 
	 * 			containing the unfinished content is returned.
	 * 			Notice:	Its important that the returned buffer is not bigger than the content inside
	 * 					
	 */
	
	public byte[] extractPacketsToQueue(byte[] buffer, RelayDataHandler relayDataHandler,
			int readBytes, ForwardingType forwardingType);

	public String getName();

}
