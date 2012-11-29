package ch.compass.gonzoproxy.relay.io.wrapper;

import java.util.Arrays;

import ch.compass.gonzoproxy.mvc.model.Field;
import ch.compass.gonzoproxy.mvc.model.Packet;

public class LibNfcApduWrapper implements ApduWrapper {

	private byte[] trailer;
	private byte[] plainApdu;
	private byte[] preamble;

	public byte[] wrap(Packet apdu) {
		this.trailer = apdu.getTrailer();
		this.preamble = computePreamble(apdu);

		StringBuilder mergedFields = new StringBuilder();

		for (Field field : apdu.getFields()) {
			mergedFields.append(field.getValue() + " ");
		}

		this.plainApdu = mergedFields.substring(0, mergedFields.length() - 1)
				.getBytes();

		// plainApdu = mergedFields.toString().getBytes();

		// TODO compute new size of APDU and put into preamble

		int newSize = preamble.length + plainApdu.length + trailer.length;

		byte[] wrappedApdu = Arrays.copyOf(preamble, newSize);

		System.arraycopy(plainApdu, 0, wrappedApdu, preamble.length,
				plainApdu.length);
		System.arraycopy(trailer, 0, wrappedApdu, preamble.length
				+ plainApdu.length, trailer.length);

		return wrappedApdu;
	}

	private byte[] computePreamble(Packet apdu) {
		byte[] newPreamble = apdu.getPreamble();
		int lastSizeIndex = newPreamble.length - 1 - 2;

		int apduSize = apdu.getSize();
		String strApduSize = Integer.toHexString(apduSize);
		byte[] newSize = strApduSize.getBytes();
		int lastIndexNew = newSize.length - 1;

		for (int i = 0; i < newSize.length; i++) {
			newPreamble[lastSizeIndex - i] = newSize[lastIndexNew - i];
		}
		;

		apdu.setPreamble(newPreamble);

		return newPreamble;

	}

	@Override
	public String getName() {
		return "libnfc";
	}

}
