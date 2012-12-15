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
import ch.compass.gonzoproxy.model.packet.PacketType;
import ch.compass.gonzoproxy.model.relay.RelayDataModel;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamReader;
import ch.compass.gonzoproxy.relay.io.streamhandler.PacketStreamWriter;
import ch.compass.gonzoproxy.relay.modifier.PacketRegex;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;
import ch.compass.gonzoproxy.relay.settings.RelaySettings;
import ch.compass.gonzoproxy.relay.settings.RelaySettings.SessionState;
import ch.compass.gonzoproxy.utils.PacketUtils;

public class GonzoRelayService implements RelayService {

	private ExecutorService threadPool;

	private ServerSocket serverSocket;
	private Socket initiator;
	private Socket target;
	private RelaySettings sessionSettings = new RelaySettings();;

	private RelayDataHandler relayDataHandler;

	public GonzoRelayService() {
		relayDataHandler = new RelayDataHandler(sessionSettings);
	}

	@Override
	public void run() {
		startRelaySession();
	}

	private void startRelaySession() {
		threadPool = Executors.newFixedThreadPool(4);
		if (connectionEstablished()) {
			initProducerConsumer();
			startDataProcessing();
		}
	}

	private boolean connectionEstablished() {
		try {
			sessionSettings.setSessionState(SessionState.CONNECTING);

			if (initiatorConnected()) {
				if (connectedToTarget()) {
					sessionSettings.setSessionState(SessionState.CONNECTED);
					return true;
				} else {
					sessionSettings
							.setSessionState(SessionState.CONNECTION_REFUSED);
					return false;
				}
			} else {
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
				return false;
			}
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				// is this code reached? :>
				// log server socket not closed ?
			}
		}
	}

	private boolean connectedToTarget() {
		try {
			target = new Socket(sessionSettings.getRemoteHost(),
					sessionSettings.getRemotePort());
		} catch (IOException e) {
			try {
				initiator.close();
			} catch (IOException e1) {
			}
			return false;
		}
		return true;
	}

	private boolean initiatorConnected() {
		try {
			serverSocket = new ServerSocket(sessionSettings.getListenPort());
			initiator = serverSocket.accept();
		} catch (IOException e) {
			return false;
		}
		return true;
	}

	private void initProducerConsumer() {
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.CONNECTION_REFUSED);
			closeRelayComponents();
		}
		sessionSettings.setSessionState(SessionState.FORWARDING);
	}

	private void initCommandStreamHandlers() throws IOException {

		InputStream inputStream = new BufferedInputStream(
				initiator.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				target.getOutputStream());
		PacketStreamReader commandStreamReader = new PacketStreamReader(
				inputStream, relayDataHandler, sessionSettings.getRelayMode(),
				PacketType.COMMAND);
		PacketStreamWriter commandStreamWriter = new PacketStreamWriter(
				outputStream, relayDataHandler, sessionSettings.getRelayMode(),
				PacketType.COMMAND);

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
				inputStream, relayDataHandler, sessionSettings.getRelayMode(),
				PacketType.RESPONSE);
		PacketStreamWriter responseStreamWriter = new PacketStreamWriter(
				outputStream, relayDataHandler, sessionSettings.getRelayMode(),
				PacketType.RESPONSE);

		responseStreamWriter.setTrapListener(sessionSettings);

		threadPool.execute(responseStreamReader);
		threadPool.execute(responseStreamWriter);
	}

	private void startDataProcessing() {
		try {
			relayDataHandler.processRelayData();
		} catch (InterruptedException e) {
			closeRelayComponents();
		}

	}

	public void stopSession() {
		closeRelayComponents();
		sessionSettings.setSessionState(SessionState.DISCONNECTED);
	}

	private void closeRelayComponents() {
		stopDataProcessing();
		closeSockets();
		shutdownConsumerProducer();

	}

	private void stopDataProcessing() {
		if (relayDataHandler.isProcessingData()) {
			relayDataHandler.offer(PacketUtils.getEndOfStreamPacket());
		}
	}

	private void shutdownConsumerProducer() {
		if (threadPool != null)
			threadPool.shutdownNow();
	}

	private void closeSockets() {
		if (socketIsOpen(initiator)) {
			try {
				initiator.close();
			} catch (IOException e) {
				// TODO : state -> socket closing error
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
			}
		}

		if (socketIsOpen(target)) {
			try {
				target.close();
			} catch (IOException e) {
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
			}
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				sessionSettings.setSessionState(SessionState.DISCONNECTED);
			}
		}
	}

	private boolean socketIsOpen(Socket socket) {
		return socket != null && !socket.isClosed();
	}

	public void setConnectionParameters(String portListen, String remoteHost,
			String remotePort, String mode) {
		sessionSettings.setConnectionParameter(Integer.parseInt(portListen),
				remoteHost, Integer.parseInt(remotePort));
		sessionSettings.setRelayMode(mode);
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
			sessionSettings.setTrapState(SessionState.COMMAND_TRAP);
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
			sessionSettings.setTrapState(SessionState.RESPONSE_TRAP);
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

	public RelayDataModel getSessionModel() {
		return relayDataHandler.getSessionModel();
	}

	public void reparse() {
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

	public void addRule(String packetName, String fieldName,
			String originalValue, String replacedValue, Boolean updateLength) {
		relayDataHandler.addRule(packetName, fieldName, originalValue,
				replacedValue, updateLength);

	}

	public void addRegex(String regex, String replaceWith, boolean isActive) {
		relayDataHandler.addRegex(regex, replaceWith, isActive);
	}

	public void persistRules() throws IOException {
		relayDataHandler.persistRules();
	}

	public void persistRegex() throws IOException {
		relayDataHandler.persistRegex();
	}

}
