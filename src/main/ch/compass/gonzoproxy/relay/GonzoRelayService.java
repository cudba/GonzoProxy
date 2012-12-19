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
import ch.compass.gonzoproxy.relay.settings.ConnectionState;
import ch.compass.gonzoproxy.relay.settings.RelaySettings;
import ch.compass.gonzoproxy.relay.settings.TrapState;
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

	public void stopSession() {
		closeRelayComponents();
		sessionSettings.setConnectionState(ConnectionState.DISCONNECTED);
	}

	public void setConnectionParameters(String portListen, String remoteHost,
			String remotePort, String mode) {
		sessionSettings.setConnectionParameter(Integer.parseInt(portListen),
				remoteHost, Integer.parseInt(remotePort));
		sessionSettings.setRelayMode(mode);
		relayDataHandler.clearSessionData();
	}

	private void startRelaySession() {
		threadPool = Executors.newFixedThreadPool(4);
		if (connectionEstablished()) {
			initProducerConsumer();
			processData();
		}
	}

	private boolean connectionEstablished() {
		try {
			sessionSettings.setConnectionState(ConnectionState.CONNECTING);

			if (initiatorConnected()) {
				if (connectedToTarget()) {
					sessionSettings
							.setConnectionState(ConnectionState.CONNECTED);
					return true;
				} else {
					sessionSettings
							.setConnectionState(ConnectionState.CONNECTION_REFUSED);
					return false;
				}
			} else {
				sessionSettings
						.setConnectionState(ConnectionState.DISCONNECTED);
				return false;
			}
		} finally {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
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
			sessionSettings
					.setConnectionState(ConnectionState.CONNECTION_REFUSED);
			closeRelayComponents();
		}
		sessionSettings.updateForwardingMode();
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

	private void processData() {
		try {
			relayDataHandler.processRelayData();
		} catch (InterruptedException e) {
			closeRelayComponents();
		}

	}

	private void closeRelayComponents() {
		stopDataProcessing();
		closeSockets();
		shutdownConsumerProducer();

	}

	private void stopDataProcessing() {
		if (relayDataHandler.isProcessingData()) {
			relayDataHandler.offer(PacketUtils.getStopPacket());
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
				e.printStackTrace();
			}
		}

		if (socketIsOpen(target)) {
			try {
				target.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (serverSocket != null) {
			try {
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private boolean socketIsOpen(Socket socket) {
		return socket != null && !socket.isClosed();
	}

	public void commandTrapChanged() {
		switch (sessionSettings.getTrapState()) {
		case COMMAND_TRAP:
			sessionSettings.setTrapState(TrapState.FORWARDING);
			break;
		case FORWARDING:
			sessionSettings.setTrapState(TrapState.COMMAND_TRAP);
			break;
		case RESPONSE_TRAP:
			sessionSettings.setTrapState(TrapState.TRAP);
			break;
		case TRAP:
			sessionSettings.setTrapState(TrapState.RESPONSE_TRAP);
			break;
		}
	}

	public void responseTrapChanged() {
		switch (sessionSettings.getTrapState()) {
		case RESPONSE_TRAP:
			sessionSettings.setTrapState(TrapState.FORWARDING);
			break;
		case FORWARDING:
			sessionSettings.setTrapState(TrapState.RESPONSE_TRAP);
			break;
		case COMMAND_TRAP:
			sessionSettings.setTrapState(TrapState.TRAP);
			break;
		case TRAP:
			sessionSettings.setTrapState(TrapState.COMMAND_TRAP);
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
		sessionSettings.addStateListener(stateListener);
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
