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

import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamReader;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamWriter;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamWriter.State;

public class CommunicationHandler implements Runnable {

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

	private LinkedTransferQueue<Packet> responseSenderQueue;

	private LinkedTransferQueue<Packet> commandSenderQueue;

	public CommunicationHandler(SessionSettings sessionSettings,
			LinkedTransferQueue<Packet> receiverQueue,
			LinkedTransferQueue<Packet> commandSenderQueue, LinkedTransferQueue<Packet> responseSenderQueue) {
		this.sessionSettings = sessionSettings;
		this.receiverQueue = receiverQueue;
		this.commandSenderQueue = commandSenderQueue;
		this.responseSenderQueue = responseSenderQueue;
	}

	@Override
	public void run() {
		setTrapListener();
		establishConnection();
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
			startCommunication();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void setTrapListener() {
		sessionSettings.setTrapListener(new TrapListener() {
			
			@Override
			public void responseTrapped() {
				if(sessionSettings.responseIsTrapped()){
					responseStreamWriter.setState(State.TRAP);
				}else {
					responseStreamWriter.setState(State.FORWARDING);
				}
			}
			
			@Override
			public void commandTrapped() {
				if(sessionSettings.commandIsTrapped()) {
					commandStreamWriter.setState(State.TRAP);
				}else {
					commandStreamWriter.setState(State.FORWARDING);
				}
			}
		});
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
		closeSockets();
		threadPool.shutdownNow();
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

