package ch.compass.gonzoproxy.relay.io;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedTransferQueue;

import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamReader;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamWriter;

public class CommunicationHandler implements Runnable {

	private boolean sessionIsAlive = true;

	private ExecutorService threadPool = Executors.newFixedThreadPool(4);

	private ServerSocket serverSocket;
	private Socket initiator;
	private Socket target;
	private SessionSettings sessionSettings;

	private HexStreamReader commandStreamReader;
	private HexStreamWriter commandStreamWriter;

	private HexStreamReader responseStreamReader;
	private HexStreamWriter responseStreamWriter;

	private LinkedTransferQueue<Packet> receiverQueue;
	private LinkedTransferQueue<Packet> senderQueue;

	private LinkedTransferQueue<Packet> commandSenderQueue = new LinkedTransferQueue<Packet>();
	private LinkedTransferQueue<Packet> responseSenderQueue = new LinkedTransferQueue<Packet>();

	public CommunicationHandler(SessionSettings sessionSettings,
			LinkedTransferQueue<Packet> receiverQueue,
			LinkedTransferQueue<Packet> senderQueue) {
		this.sessionSettings = sessionSettings;
		this.receiverQueue = receiverQueue;
		this.senderQueue = senderQueue;
	}

	@Override
	public void run() {

		establishConnection();
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
			startCommunication();
			hanleSenderQueue();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}

	}

	private void hanleSenderQueue() throws InterruptedException {
		while (sessionIsAlive) {
			Packet sendingPacket = senderQueue.take();
			switch (sendingPacket.getType()) {
			case COMMAND:
				commandSenderQueue.tryTransfer(sendingPacket);
				break;

			case RESPONSE:
				responseSenderQueue.tryTransfer(sendingPacket);
				break;
			}
		}

	}

	private void startCommunication() {
		threadPool.execute(commandStreamReader);
		threadPool.execute(commandStreamWriter);
		threadPool.execute(responseStreamReader);
		threadPool.execute(responseStreamWriter);
	}

	private void initCommandStreamHandlers() throws IOException {

		InputStream inputStream = new BufferedInputStream(
				initiator.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				target.getOutputStream());
		commandStreamReader = new HexStreamReader(inputStream, receiverQueue,
				sessionSettings.getMode(), ForwardingType.COMMAND);
		commandStreamWriter = new HexStreamWriter(outputStream,
				commandSenderQueue, sessionSettings.getMode());
	}

	private void initResponseStreamHandlers() throws IOException {
		InputStream inputStream = new BufferedInputStream(
				target.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				initiator.getOutputStream());
		responseStreamReader = new HexStreamReader(inputStream, receiverQueue,
				sessionSettings.getMode(), ForwardingType.RESPONSE);
		responseStreamWriter = new HexStreamWriter(outputStream,
				responseSenderQueue, sessionSettings.getMode());
	}

	public void killSession() {
		sessionIsAlive = false;
		threadPool.shutdownNow();
		closeSockets();
	}

	private void establishConnection() {
		try {
			serverSocket = new ServerSocket(sessionSettings.getListenPort());
			initiator = serverSocket.accept();
			target = new Socket(sessionSettings.getRemoteHost(),
					sessionSettings.getRemotePort());
		} catch (IOException openSocket) {
			try {
				initiator.close();
				target.close();
			} catch (IOException closeSocket) {
				closeSocket.printStackTrace();
			}

		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void closeSockets() {
		try {
			initiator.close();
			target.close();
		} catch (IOException e) {
			// TODO: status update connection closed
		}
	}
}
