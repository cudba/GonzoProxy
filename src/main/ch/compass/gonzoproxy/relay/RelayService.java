package ch.compass.gonzoproxy.relay;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ch.compass.gonzoproxy.listener.StateListener;
import ch.compass.gonzoproxy.model.SessionModel;
import ch.compass.gonzoproxy.relay.modifier.FieldRule;
import ch.compass.gonzoproxy.relay.modifier.PacketRegex;
import ch.compass.gonzoproxy.relay.modifier.PacketRule;

public interface RelayService extends Runnable {

	public abstract void run();

	public abstract void stopSession();

	public abstract void generateNewSessionParameters(String portListen,
			String remoteHost, String remotePort, String mode);

	public abstract void commandTrapChanged();

	public abstract void responseTrapChanged();

	public abstract void sendOneCmd();

	public abstract void sendOneRes();

	public abstract int getCurrentListenPort();

	public abstract String getCurrentRemoteHost();

	public abstract int getCurrentRemotePort();

	public abstract void addSessionStateListener(StateListener stateListener);

	public abstract SessionModel getSessionModel();

	public abstract void reParse();

	public abstract void persistSessionData(File file) throws IOException;

	public abstract void loadPacketsFromFile(File file)
			throws ClassNotFoundException, IOException;

	public abstract ArrayList<PacketRule> getPacketRules();

	public abstract ArrayList<PacketRegex> getPacketRegex();

	public abstract void addRule(String packetName, FieldRule fieldRule,
			Boolean updateLength);

	public abstract void addRegex(PacketRegex packetRegex, boolean isActive);

	public abstract void persistRules() throws IOException;

	public abstract void persistRegex() throws IOException;

}