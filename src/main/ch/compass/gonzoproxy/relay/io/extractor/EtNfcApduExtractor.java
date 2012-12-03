package ch.compass.gonzoproxy.relay.io.extractor;

import java.util.ArrayList;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.utils.ByteArraysUtils;

public class EtNfcApduExtractor implements ApduExtractor {

	private static final char EOC = '\n';

	private static final String DELIMITER = "#";


	public byte[] extractPacketsToQueue(byte[] buffer, RelayDataHandler relayDataHandler,
			int readBytes, ForwardingType forwardingType) {
		ArrayList<Integer> indices = ByteArraysUtils.getDelimiterIndices(buffer,
				DELIMITER.getBytes());

		int startIndex = 0;
		int endIndex = 0;

		for (int i = 0; i < indices.size() - 1; i++) {
			startIndex = indices.get(i);
			endIndex = indices.get(i + 1);
			int size = endIndex - startIndex;
			byte[] rawApdu = ByteArraysUtils.trim(buffer, startIndex, size);
			Packet apdu = splitApdu(rawApdu);
			relayDataHandler.offer(apdu);
		}

		byte[] singleApdu = ByteArraysUtils.trim(buffer, endIndex, readBytes - endIndex);

		if (apduIsComplete(singleApdu)) {
			Packet apdu = splitApdu(singleApdu);
			relayDataHandler.offer(apdu);
			return new byte[0];
		} else {
			return singleApdu;
		}
	}

	private boolean apduIsComplete(byte[] singleApdu) {
		return singleApdu[singleApdu.length - 1] == EOC;
	}

	private Packet splitApdu(byte[] rawApdu) {
		int size = getApduSize(rawApdu);
		byte[] preamble = getApduPreamble(rawApdu, size);
		byte[] plainApdu = getPlainApdu(rawApdu, size);
		byte[] trailer = getApduTrailer(rawApdu, size);
		Packet newApdu = new Packet();
		newApdu.setPreamble(preamble);
		newApdu.setOriginalPacketData(plainApdu);
		newApdu.setTrailer(trailer);
		newApdu.setSize(size);
		return newApdu;
	}

	private byte[] getApduTrailer(byte[] rawApdu, int size) {
		for (int i = 0; i < rawApdu.length; i++) {
			if (rawApdu[i] == ':') {
				int endOfPlainApdu = i + 3 * size + 1;
				return ByteArraysUtils.trim(rawApdu, endOfPlainApdu, rawApdu.length
						- endOfPlainApdu);
			}
		}
		return null;
	}

	private byte[] getPlainApdu(byte[] rawApdu, int size) {
		for (int i = 0; i < rawApdu.length; i++) {
			if (rawApdu[i] == ':') {
				return ByteArraysUtils.trim(rawApdu, i + 2, size * 3 - 1);
			}
		}
		return rawApdu;
	}

	private byte[] getApduPreamble(byte[] rawApdu, int size) {
		for (int i = 0; i < rawApdu.length; i++) {
			if (rawApdu[i] == ':') {
				return ByteArraysUtils.trim(rawApdu, 0, i + 2);
			}
		}
		return rawApdu;
	}

	private int getApduSize(byte[] rawApdu) {
		int value = 0;
		byte[] size = new byte[4];
		for (int i = 0; i < rawApdu.length; i++) {
			if (rawApdu[i] == ' ') {
				size[0] = rawApdu[i + 1];
				size[1] = rawApdu[i + 2];
				size[2] = rawApdu[i + 3];
				size[3] = rawApdu[i + 4];
				value = Integer.parseInt(new String(size), 16);
				System.out.println("Size: " + value);
				return value;
			}
		}
		return value;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "etnfc";
	}
}
