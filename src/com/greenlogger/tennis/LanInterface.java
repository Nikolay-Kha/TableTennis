package com.greenlogger.tennis;

//for callbacks
public interface LanInterface<T extends LanPacket> {
	void onConnectionLost(boolean isProblem);

	void onConnectionStarted();

	void onDataRecieve(T buffer); // not thread safe

	void onGuiUpdate();

	void onTimeoutSearch();
}
