package com.greenlogger.tennis;

import android.bluetooth.BluetoothAdapter;
import android.widget.ListAdapter;

public abstract class LanGame<T extends LanPacket> {

	public static boolean isHaveBTAdapter() {
		return BluetoothAdapter.getDefaultAdapter() != null;
	}

	public static boolean isHaveWiFiAdapter() {
		// TODO Auto-generated method stub
		return true;
	}

	public static void shutdownBTAdapter() {
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			BluetoothAdapter.getDefaultAdapter().disable();
		}
	}

	public static void shutdownWiFiAdapter() {
		// TODO Auto-generated method stub

	}

	public static boolean isBTEnabled() {
		if (BluetoothAdapter.getDefaultAdapter() != null) {
			return BluetoothAdapter.getDefaultAdapter().isEnabled();
		}
		return false;
	}

	public static boolean isWiFiEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public abstract ListAdapter getListAdapter();

	public abstract void cleanup();

	public abstract boolean isConnected();

	public abstract void send(final T pak);

	public abstract void stopSearch();

	public abstract void disconnect();

	public abstract void startSearch();

	public abstract boolean isServer();

	public abstract void connect(final int id);

	public abstract String getTextStatus();

	public abstract boolean isConnecting();

	public abstract String getConnectingString();

	public void bluetoothDiscoverableEnabled() {
	};

}
