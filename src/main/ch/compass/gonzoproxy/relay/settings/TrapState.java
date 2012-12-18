package ch.compass.gonzoproxy.relay.settings;

public enum TrapState implements RelayState {
	
	FORWARDING("Forwarding"),
	TRAP("Trapped"),
	COMMAND_TRAP("Command trapped"),
	RESPONSE_TRAP("Response trapped");
	
	private String description;
	
	private TrapState(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}

}
