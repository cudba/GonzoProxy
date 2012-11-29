package ch.compass.gonzoproxy.relay.io.streamhandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.mvc.model.ForwardingType;
import ch.compass.gonzoproxy.mvc.model.Packet;
import ch.compass.gonzoproxy.relay.io.extractor.ApduExtractor;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class PacketStreamReader {

	private static final int BUFFER_SIZE = 1024;

	private ApduExtractor extractor;

	public PacketStreamReader(ApduExtractor extractor) {
		this.extractor = extractor;
	}

	public void readPackets(InputStream inputStream,
			LinkedTransferQueue<Packet> receiverQueue,
			ForwardingType forwardingType) throws IOException {
		while (true) {
			byte[] buffer = new byte[BUFFER_SIZE];

			int length = 0;
			int readBytes = 0;

			boolean readCompleted = false;

			while (!readCompleted) {

				if (length == buffer.length) {
					ByteArraysUtils.enlarge(buffer);
				}

				if ((readBytes = inputStream.read(buffer, length, buffer.length
						- length)) != -1) {
					length += readBytes;
					buffer = extractor.extractPacketsToQueue(buffer,
							receiverQueue, length, forwardingType);
				}

				readCompleted = buffer.length == 0;

				if (!readCompleted) {
					length = buffer.length;
					buffer = ByteArraysUtils.enlarge(buffer, BUFFER_SIZE);
				}
			}
		}
	}

}
