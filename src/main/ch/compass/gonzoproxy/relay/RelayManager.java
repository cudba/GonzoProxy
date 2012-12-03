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

import ch.compass.gonzoproxy.listener.TrapListener;
import ch.compass.gonzoproxy.model.ForwardingType;
import ch.compass.gonzoproxy.model.SessionSettings;
import ch.compass.gonzoproxy.model.SessionSettings.SessionState;
import ch.compass.gonzoproxy.relay.io.RelayDataHandler;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamReader;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamWriter;
import ch.compass.gonzoproxy.relay.io.streamhandler.HexStreamWriter.State;

public class RelayManager implements Runnable {
	
	private ExecutorService threadPool = Executors.newFixedThreadPool(5);
	
	private ServerSocket serverSocket;
	private Socket initiator;
	private Socket target;
	private SessionSettings sessionSettings;
	
	private RelayDataHandler relayDataHandler;

	private HexStreamWriter responseStreamWriter;
	private HexStreamReader responseStreamReader;

	private HexStreamWriter commandStreamWriter;
	private HexStreamReader commandStreamReader;
	
	public RelayManager(RelayDataHandler relayDataHandler, SessionSettings sessionSettings){
		this.relayDataHandler = relayDataHandler;
		this.sessionSettings = sessionSettings;
	}

	@Override
	public void run() {
		setTrapListener();
		threadPool.execute(relayDataHandler);
		sessionSettings.setSessionState(SessionState.CONNECTING);
		establishConnection();
		initProducerConsumer();
	}

	private void initProducerConsumer() {
		try {
			initCommandStreamHandlers();
			initResponseStreamHandlers();
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
		}
		sessionSettings.setSessionState(SessionState.FORWARDING);
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
	
	private void initCommandStreamHandlers() throws IOException {

		InputStream inputStream = new BufferedInputStream(
				initiator.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				target.getOutputStream());
		commandStreamReader = new HexStreamReader(inputStream, relayDataHandler,
				sessionSettings.getMode(), ForwardingType.COMMAND);
		commandStreamWriter = new HexStreamWriter(outputStream,
				relayDataHandler, sessionSettings.getMode(), ForwardingType.COMMAND);
		
		threadPool.execute(commandStreamReader);
		threadPool.execute(commandStreamWriter);
	}

	private void initResponseStreamHandlers() throws IOException {
		InputStream inputStream = new BufferedInputStream(
				target.getInputStream());
		OutputStream outputStream = new BufferedOutputStream(
				initiator.getOutputStream());
		responseStreamReader = new HexStreamReader(inputStream, relayDataHandler,
				sessionSettings.getMode(), ForwardingType.RESPONSE);
		responseStreamWriter = new HexStreamWriter(outputStream,
				relayDataHandler, sessionSettings.getMode(), ForwardingType.RESPONSE);
		
		threadPool.execute(responseStreamReader);
		threadPool.execute(responseStreamWriter);
	}
	
	public void killSession() {
		closeSockets();
		threadPool.shutdownNow();
		try {
			if(threadPool.awaitTermination(2, TimeUnit.SECONDS)){
				System.out.println("consumer / producer threads closed successfully");
			}else {
				System.out.println("consumer / producer not closed in time");
			}
		} catch (InterruptedException e) {
			System.out.println("error while closing worker threads");
		}
		sessionSettings.setSessionState(SessionState.DISCONNECTED);
	}
	
	private void closeSockets() {
		try {
			initiator.close();
			target.close();
		} catch (IOException e) {
			sessionSettings.setSessionState(SessionState.DISCONNECTED);
		}
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
