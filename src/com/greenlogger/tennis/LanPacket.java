package com.greenlogger.tennis;

import java.io.Serializable;

public abstract class LanPacket implements Serializable, Cloneable {
	
	public LanPacket(byte packet_type){
		type = packet_type;
	}
	public Object clone() { 
		try { 
			return super.clone(); 
		} catch(CloneNotSupportedException e) { 
			return null; 
		} 
	}

	public transient final static byte TYPE_NONE = 0;
	public transient final static byte TYPE_PING = 1;
	public transient final static byte TYPE_PONG = 2;
	// user types must start from greater number

	private transient static final long serialVersionUID = 3929547972871810929L;
	public byte type = TYPE_NONE;
	public byte recoveryType = TYPE_NONE;
	public int id;
	public long timeStamp; // must feel before sending ping packet
	public long timeDelta = 0;

}
