package ch.compass.gonzoproxy.relay;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.relay.RelaySettings.SessionState;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamReader;
import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamWriter;
import ch.compass.gonzoproxy.relay.modifier.FieldRule;
import ch.compass.gonzoproxy.relay.modifier.PacketRegex;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;

public class GonzoRelayService implements RelayService {

	private ExecutorService threadPool;

	private ServerSocket serverSocket;
	private Socket initiator;
	private Socket target;
	private RelaySettings sessionSettings = new RelaySettings();;

	private RelayDataHandler relayDataHandler = new RelayDataHandler();

	@Override
	public void run() {
		handleRelaySession();
	}

	private void handleRelaySession() {
		threadPool = Executors.newFixedThreadPool(4);
		establishConnection();
		initProducerConsumer();
		handleData();
		killSession();
	}

	private void handleData() {
		relayDataHandler.processRelayData();
		
	}

	private void initProducerConsumer() {
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
			sessionSettings.setSessionState(SessionState.FORWARDING);
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.CONNECTION_REFUSED);
			System.out
					.println("session is beeing killed by manager, io exception");
			killSession();
		}
	}

	private void establishConnection() {
		try {
			awaitInitiatorConnection();
			connectToTarget();
			sessionSettings.setSessionState(SessionState.CONNECTED);
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// log server socket not closed ?
			}
		}
	}

	private void connectToTarget() {
		try {
			target = new Socket(sessionSettings.getRemoteHost(),
					sessionSettings.getRemotePort());
		} catch (IOException e) {
			try {
				initiator.close();
			} catch (IOException e1) {
			}
			sessionSettings.setSessionState(SessionState.CONNECTION_REFUSED);
		}
	}

	private void awaitInitiatorConnection() {
		sessionSettings.setSessionState(SessionState.CONNECTING);
		try {
			serverSocket = new ServerSocket(sessionSettings.getListenPort());
			initiator = serverSocket.accept();
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.CONNECTION_REFUSED);
		}
	}

	private void initCommandStreamHandlers() throws IOException {

		InputStream inputStream = new BufferedInputStream(
				initiator.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				target.getOutputStream());
		PacketStreamReader commandStreamReader = new PacketStreamReader(
				inputStream, relayDataHandler, sessionSettings.getMode(),
				ForwardingType.COMMAND);
		PacketStreamWriter commandStreamWriter = new PacketStreamWriter(
				outputStream, relayDataHandler, sessionSettings.getMode(),
				ForwardingType.COMMAND);
		
		commandStreamWriter.setTrapListener(sessionSettings);

		threadPool.execute(commandStreamReader);
		threadPool.execute(commandStreamWriter);
	}

	private void initResponseStreamHandlers() throws IOException {
		InputStream inputStream = new BufferedInputStream(
				target.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				initiator.getOutputStream());
		PacketStreamReader responseStreamReader = new PacketStreamReader(
				inputStream, relayDataHandler, sessionSettings.getMode(),
				ForwardingType.RESPONSE);
		PacketStreamWriter responseStreamWriter = new PacketStreamWriter(
				outputStream, relayDataHandler, sessionSettings.getMode(),
				ForwardingType.RESPONSE);
		
		responseStreamWriter.setTrapListener(sessionSettings);

		threadPool.execute(responseStreamReader);
		threadPool.execute(responseStreamWriter);
	}

	public void killSession() {
		sessionSettings.setSessionState(SessionState.DISCONNECTED);
		threadPool.shutdownNow();
		closeSockets();
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
		relayDataHandler.clearSessionData();
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

	public SessionModel getSessionModel() {
		return relayDataHandler.getSessionModel();
	}

	public void reParse() {
		relayDataHandler.reparse();
	}

	public void persistSessionData(File file) throws IOException {
		relayDataHandler.persistSessionData(file);
	}

	public void loadPacketsFromFile(File file) throws ClassNotFoundException,
			IOException {
		relayDataHandler.loadPacketsFromFile(file);
	}

	public ArrayList<PacketRule> getPacketRules() {
		return relayDataHandler.getPacketRules();
	}

	public ArrayList<PacketRegex> getPacketRegex() {
		return relayDataHandler.getPacketRegex();
	}

	public void addRule(String packetName, FieldRule fieldRule,
			Boolean updateLength) {
		relayDataHandler.addRule(packetName, fieldRule, updateLength);

	}

	public void addRegex(PacketRegex packetRegex, boolean isActive) {
		relayDataHandler.addRegex(packetRegex, isActive);
	}

	public void persistRules() throws IOException {
		relayDataHandler.persistRules();
	}

	public void persistRegex() throws IOException {
		relayDataHandler.persistRegex();
	}

}
