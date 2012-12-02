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
import java.util.concurrent.TimeUnit;

import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.Packet;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.model.SessionSettings.SessionState;
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
		sessionSettings.setSessionState(SessionState.CONNECTING);
		establishConnection();
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
			startCommunication();
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
		}
		sessionSettings.setSessionState(SessionState.FORWARDING);

	}

	private void setTrapListener() {
		sessionSettings.setTrapListener(new TrapListener() {
			

			@Override
			public void checkTrapChanged() {
				checkForTraps();
			}

			@Override
			public void sendOneCommand() {
				commandStreamWriter.setState(State.SEND_ONE);
			}

			@Override
			public void sendOneResponse() {
				responseStreamWriter.setState(State.SEND_ONE);
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

	public void killSession() throws InterruptedException {
		closeSockets();
		threadPool.shutdownNow();
		if(threadPool.awaitTermination(2, TimeUnit.SECONDS)){
			System.out.println("consumer / producer threads closed successfully");
		}else {
			System.out.println("consumer / producer not closed in time");
		}
		sessionSettings.setSessionState(SessionState.DISCONNECTED);
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
	
	private void checkForTraps() {
		switch (sessionSettings.getSessionState()) {
		case TRAP:
			commandStreamWriter.setState(State.TRAP);
			responseStreamWriter.setState(State.TRAP);
			break;
		case RESPONSE_TRAP:
			responseStreamWriter.setState(State.TRAP);
			commandStreamWriter.setState(State.FORWARDING);
			break;
		case COMMAND_TRAP:
			commandStreamWriter.setState(State.TRAP);
			responseStreamWriter.setState(State.FORWARDING);
			break;
		case FORWARDING:
			commandStreamWriter.setState(State.FORWARDING);
			responseStreamWriter.setState(State.FORWARDING);
		default:
			break;
		}
	}
}	

