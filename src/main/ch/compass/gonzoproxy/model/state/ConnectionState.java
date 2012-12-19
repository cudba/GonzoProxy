package ch.compass.gonzoproxy.model.state;

public enum ConnectionState implements RelayState {
	
	DISCONNECTED("Disconnected"),
	CONNECTING("Waiting for initiator ..."),
	CONNECTED("Connection established"), 
	CONNECTION_REFUSED("Could not connect to target"),
	CONNECTION_LOST("Connection lost"), 
	EOS("End of stream reached"), 
	MODE_FAILURE("Failed to instantiate chosen relay mode");
	
	private String description;

	private ConnectionState(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	

}
