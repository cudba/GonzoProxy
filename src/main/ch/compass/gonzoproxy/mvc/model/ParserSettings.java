package ch.compass.gonzoproxy.mvc.model;

public enum ParserSettings {
	LibNFC(2,1),
	NoWhiteSpaces(2,0);
	
	private int encodingOffset;
	private int whitespaceOffset;
	
	ParserSettings(int encodingOffset, int whitespaceOffset){
		this.encodingOffset = encodingOffset;
		this.whitespaceOffset = whitespaceOffset;
		
	}
	
	public int getEncodingOffset() {
		return encodingOffset;
	}
	
	public int getWhitespaceOffset() {
		return whitespaceOffset;
	}
}
