package com.greenlogger.tennis;

public class TennisPacket extends LanPacket {

	public TennisPacket(byte packet_type) {
		super(packet_type);
	}
	public TennisPacket() {
		super(TYPE_NONE);
	}
	
	private transient static final long serialVersionUID = 3929544472871810929L;
	//enum TYPES{
	public transient final static byte TYPE_BALL_GOAL = 10;
	public transient final static byte TYPE_TAB_MOVE = 11;
	public transient final static byte TYPE_BALL_REFLECTED = 12;

	public transient final static byte TYPE_PAUSE = 20;
	public transient final static byte TYPE_RESUME = 21;
	public transient final static byte TYPE_PLAYAGAIN = 22;
	//}
	public transient final static byte SERVER_POS_UNKNOWN = 0;
	public transient final static byte SERVER_POS_LEFT = 1;
	public transient final static byte SERVER_POS_RIGHT = 2;

	public byte version = 1;
	public float tabPos = 0.0f;
	public float xBallPos = 0.0f;
	public float yBallPos = 0.0f;
	public float xBallSpeed = 0.0f;
	public float yBallSpeed = 0.0f;
	public byte sScore = 0;
	public byte cScore = 0;
	public byte sPos = SERVER_POS_UNKNOWN;

}
