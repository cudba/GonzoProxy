package ch.compass.gonzoproxy.relay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.model.SessionSettings.SessionState;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamReader;
import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamWriter;

public class RelayManager implements Runnable {
	
	private boolean sessionIsAlive = false;

	private ExecutorService threadPool; 

	private ServerSocket serverSocket;
	private Socket initiator;
	private Socket target;
	private SessionSettings sessionSettings = new SessionSettings();;

	private RelayDataHandler relayDataHandler = new RelayDataHandler();

	@Override
	public void run() {
		threadPool = Executors.newFixedThreadPool(5);
		threadPool.execute(relayDataHandler);
		sessionSettings.setSessionState(SessionState.CONNECTING);
		establishConnection();
		initProducerConsumer();
		sessionIsAlive = true;
		sessionSettings.setSessionState(SessionState.FORWARDING);
	}

	private void initProducerConsumer() {
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
		} catch (IOException e) {
			System.out.println("manager closed");
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
		}
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
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
			} catch (IOException closeSocket) {
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
			}

		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// TODO: LOG ?
			}
		}
	}

	private void initCommandStreamHandlers() throws IOException {

		InputStream inputStream = new BufferedInputStream(
				initiator.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				target.getOutputStream());
		PacketStreamReader commandStreamReader = new PacketStreamReader(inputStream,
				relayDataHandler, sessionSettings, ForwardingType.COMMAND);
		PacketStreamWriter commandStreamWriter = new PacketStreamWriter(outputStream,
				relayDataHandler, sessionSettings, ForwardingType.COMMAND);

		threadPool.execute(commandStreamReader);
		threadPool.execute(commandStreamWriter);
	}

	private void initResponseStreamHandlers() throws IOException {
		InputStream inputStream = new BufferedInputStream(
				target.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				initiator.getOutputStream());
		PacketStreamReader responseStreamReader = new PacketStreamReader(inputStream,
				relayDataHandler, sessionSettings, ForwardingType.RESPONSE);
		PacketStreamWriter responseStreamWriter = new PacketStreamWriter(
				outputStream, relayDataHandler, sessionSettings,
				ForwardingType.RESPONSE);

		threadPool.execute(responseStreamReader);
		threadPool.execute(responseStreamWriter);
	}

	public void killSession() {
		if (sessionIsAlive) {
			closeSockets();
			threadPool.shutdownNow();
			try {
				if (threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
					System.out
							.println("consumer / producer threads closed successfully");
				} else {
					System.out
							.println("consumer / producer not closed in time");
				}
			} catch (InterruptedException e) {
				System.out.println("error while closing worker threads");
			}
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
		}
	}

	private void closeSockets() {
		try {
			initiator.close();
			target.close();
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
		}
	}

	public void generateNewSessionParameters(String portListen,
			String remoteHost, String remotePort, String mode) {
		sessionSettings.setSession(Integer.parseInt(portListen), remoteHost,
				Integer.parseInt(remotePort));
		sessionSettings.setMode(mode);
	}

	public void commandTrapChanged() {
		switch (sessionSettings.getSessionState()) {
		case COMMAND_TRAP:
			sessionSettings.setTrapState(SessionState.FORWARDING);
			break;
		case FORWARDING:
			sessionSettings.setTrapState(SessionState.COMMAND_TRAP);
			break;
		case RESPONSE_TRAP:
			sessionSettings.setTrapState(SessionState.TRAP);
			break;
		case TRAP:
			sessionSettings.setTrapState(SessionState.RESPONSE_TRAP);
			break;
		default:
			break;
		}
	}

	public void responseTrapChanged() {
		switch (sessionSettings.getSessionState()) {
		case RESPONSE_TRAP:
			sessionSettings.setTrapState(SessionState.FORWARDING);
			break;
		case FORWARDING:
			sessionSettings.setTrapState(SessionState.RESPONSE_TRAP);
			break;
		case COMMAND_TRAP:
			sessionSettings.setTrapState(SessionState.TRAP);
			break;
		case TRAP:
			sessionSettings.setTrapState(SessionState.COMMAND_TRAP);
			break;
		default:
			break;
		}
	}

	public void sendOneCmd() {
		sessionSettings.sendOneCommand();
	}

	public void sendOneRes() {
		sessionSettings.sendOneResponse();
	}

	public void setDataHandler(RelayDataHandler relayDataHandler) {
		this.relayDataHandler = relayDataHandler;
	}

	public int getCurrentListenPort() {
		return sessionSettings.getListenPort();
	}

	public String getCurrentRemoteHost() {
		return sessionSettings.getRemoteHost();
	}

	public int getCurrentRemotePort() {
		return sessionSettings.getRemotePort();
	}

	public void addSessionStateListener(StateListener stateListener) {
		sessionSettings.addSessionStateListener(stateListener);
	}

}
