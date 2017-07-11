package com.greenlogger.tennis;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.SystemClock;

import com.greenlogger.tenis.R;

public class BtLanGame<T extends LanPacket> extends LanGame<T> {
	private BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
			.getDefaultAdapter();
	private SendingThread mSendingThread = null;
	private String mTextStatus;
	private final BtArrayAdapter mListAdapter;
	private boolean mBTScanRequie = true;
	private final Context mContext;
	private long mLastStartUpdateTime;
	private boolean mIsServer = false;
	private boolean mIsConnected = false;
	private boolean mIsConnecting = false;
	private long mServerAutoShutDownTime = 0;
	private final Timer mStatusUpdateTimer = new Timer();
	private final Handler mHandler = new Handler();
	private final LanInterface<T> mCallBackInterface;
	private boolean mIsSearchActive = false;
	public UUID BT_MY_UUID = UUID
			.fromString("aac72ac0-e7d1-4802-aae3-1b5680865bc9");
	public String BT_NAME = "BTCCT";
	private String connectionStatus = "";

	private ServerThread mServerThread = null;
	private long connectEstablishedAt = SystemClock.uptimeMillis();

	private class ConnectionLostRunnable implements Runnable {
		private boolean mProblem;

		ConnectionLostRunnable(final boolean isProblem) {
			mProblem = isProblem;
			// HACK TO SHOW WRONG VERSION DUE PROTOCOL CHANGED
			if (SystemClock.uptimeMillis() - connectEstablishedAt > 5000) {
				mProblem = false;
			}

		}

		public void run() {
			if (mUpdateStatus != null) {
				mUpdateStatus.cancel();
				mUpdateStatus = null;
			}
			mCallBackInterface.onConnectionLost(mProblem);
		}
	}

	private class GuiUpdateRunnable implements Runnable {
		public void run() {
			mCallBackInterface.onGuiUpdate();
		}
	}

	private class ConnectionRunnable implements Runnable {
		public void run() {
			if (mUpdateStatus != null) {
				mUpdateStatus.cancel();
				mUpdateStatus = null;
			}
			mCallBackInterface.onConnectionStarted();
		}
	}

	private class TimeoutRunnable implements Runnable {
		public void run() {
			mCallBackInterface.onTimeoutSearch();
		}
	}

	private class ServerThread extends Thread {
		private BluetoothServerSocket mmServerSocket;
		ObjectInputStream mInputStream = null;
		ObjectOutputStream mOutputStream = null;
		boolean isConnected = false;
		boolean work = true;
		BluetoothSocket socket = null;

		public ServerThread() {
			BluetoothServerSocket tmp = null;
			try {

				tmp = mBluetoothAdapter.listenUsingRfcommWithServiceRecord(
						BT_NAME, BT_MY_UUID);
				mmServerSocket = tmp;
				setPriority(MAX_PRIORITY);

			} catch (final IOException e) {
				mmServerSocket = null;
			}
		}

		public boolean isSocketExist() {
			return mmServerSocket != null;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			if (mmServerSocket == null) {
				return;
			}
			while (work) {
				boolean isError = false;
				try {
					socket = mmServerSocket.accept();
				} catch (final IOException e) {

				}
				// If a connection was accepted
				if (socket != null) {
					connectEstablishedAt = SystemClock.uptimeMillis();
					try {
						mmServerSocket.close();
						mmServerSocket = null;
						mOutputStream = new ObjectOutputStream(
								socket.getOutputStream());
						mOutputStream.flush();
						mInputStream = new ObjectInputStream(
								socket.getInputStream());
						stopScan();
						isConnected = true;
						mIsServer = true;
						mIsConnected = true;
						T read;
						if (work) {
							mHandler.post(new ConnectionRunnable());
						}
						mSendingThread = new SendingThread(mOutputStream);
						while (work) {
							try {
								if ((read = (T) mInputStream.readObject()) != null) {
									mCallBackInterface.onDataRecieve(read);
								}
							} catch (final Exception e) {
								isError = true;
								break;
							}
						}
					} catch (final IOException e) {
						isError = true;
					}
					if (work && socket != null) {
						cleanUpSocket();
					}
					isConnected = false;
					mIsServer = false;
					mIsConnected = false;
					if (work) {
						mHandler.post(new ConnectionLostRunnable(isError));
					}
					/*if (mIsSearchActive && work) {
						startScan();
						startServer();
					}*/
					break;
				}
			}
		}

		private void cleanUpSocket() {
			try {
				if (mSendingThread != null) {
					mSendingThread.cleanup();
					mSendingThread = null;
					mOutputStream.close();
					mInputStream.close();
					mOutputStream = null;
				}
				socket.getOutputStream().close();
				socket.getInputStream().close();
				socket.close();
			} catch (final IOException e) {
			}
		}

		public void send(final T packet) {
			if (mSendingThread != null) {
				mSendingThread.send(packet);
			}
		}

		public void cancel() {
			work = false;
			if (mmServerSocket != null) {
				try {
					mmServerSocket.close();
				} catch (final IOException e) {
				}
			}
			if (socket != null) {
				cleanUpSocket();
			}
			if (isConnected) {
				mIsServer = false;
				mIsConnected = false;
			}
		}
	}

	private ConnectThread mConnectThread = null;

	private class ConnectThread extends Thread {
		private final BluetoothSocket mmSocket;
		ObjectInputStream mInputStream = null;
		ObjectOutputStream mOutputStream = null;
		String btname;
		boolean isConnected = false;
		boolean work = true;

		public ConnectThread(final BluetoothDevice device) {
			btname = BtArrayAdapter.getStringByDevice(device);
			BluetoothSocket tmp = null;

			try {
				tmp = device.createRfcommSocketToServiceRecord(BT_MY_UUID);
			} catch (final IOException e) {
			}
			mmSocket = tmp;
			setPriority(MAX_PRIORITY);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			boolean isError = false;
			setConnecting(true);
			stopScan();
			stopServer();
			try {
				mmSocket.connect();
			} catch (final IOException connectException) {
				try {
					mmSocket.close();
				} catch (final IOException closeException) {
				}
				setConnectionStatusString(mContext
						.getString(R.string.connectError)
						+ " "
						+ btname
						+ " "
						+ connectException.getMessage());
				setConnecting(false);
				if (mIsSearchActive) {
					startScan();
					startServer();
				}
				return;
			}
			setConnectionStatusString("");
			setConnecting(false);
			try {
				connectEstablishedAt = SystemClock.uptimeMillis();
				mOutputStream = new ObjectOutputStream(
						mmSocket.getOutputStream());
				mOutputStream.flush();
				mInputStream = new ObjectInputStream(mmSocket.getInputStream());
				mIsConnected = true;
				isConnected = true;
				setConnecting(false);
				T read;
				if (work) {
					mHandler.post(new ConnectionRunnable());
				}
				mSendingThread = new SendingThread(mOutputStream);
				while (work) {
					try {
						if ((read = (T) mInputStream.readObject()) != null) {
							mCallBackInterface.onDataRecieve(read);
						}
					} catch (final ClassNotFoundException e) {
						isError = true;
					}
				}
			} catch (final IOException e) {
				isError = true;
			}
			if (work && mmSocket != null) {
				cleanupSocket();
			}
			setConnecting(false);
			mIsConnected = false;
			if (work) {
				mHandler.post(new ConnectionLostRunnable(isError));
			}
		}

		private void cleanupSocket() {
			try {
				if (mSendingThread != null) {
					mSendingThread.cleanup();
					mSendingThread = null;
					mOutputStream.close();
					mInputStream.close();
					mOutputStream = null;
				}
				mmSocket.getOutputStream().close();
				mmSocket.getInputStream().close();
				mmSocket.close();
			} catch (final IOException e) {
			}
		}

		public void send(final T packet) {
			if (mSendingThread != null) {
				mSendingThread.send(packet);
			}
		}

		public void cancel() {
			work = false;
			if (isConnected) {
				mIsConnected = false;
				if (mmSocket != null) {
					cleanupSocket();
				}
			}
		}
	}

	private class SendingThread extends Thread {
		private final LinkedList<T> mList;
		private final ObjectOutputStream mOutputStream;
		private boolean mWork = true;
		private boolean mNeedWait = true;

		SendingThread(final ObjectOutputStream stream) {
			mOutputStream = stream;
			mList = new LinkedList<T>();
			start();
		}

		@Override
		public void run() {
			while (mWork) {
				T pak = null;
				int hasMore = 0;
				synchronized (mList) {
					if (!mList.isEmpty()) {
						pak = mList.remove();
						hasMore = mList.size();
					}
				}
				//Log.i("tag", "query size " + mList.size());

				if (pak != null) {
					if ((pak.type == TennisPacket.TYPE_TAB_MOVE && hasMore > 0)
							|| ((pak.type == TennisPacket.TYPE_PING || pak.type == TennisPacket.TYPE_PONG) && hasMore > 10)) {
						continue;
					}
					try {
						if (pak.type == TennisPacket.TYPE_PING) {
							pak.timeStamp = SystemClock.uptimeMillis();
						} else if (pak.type == TennisPacket.TYPE_PONG) {
							pak.timeDelta = SystemClock.uptimeMillis()
									- pak.timeDelta;
						}
						mOutputStream.writeObject(pak);
					} catch (final IOException e) {
						e.printStackTrace();
					}
					if (hasMore > 0) {
						continue;
					}
				}

				try {
					if (mNeedWait) {
						synchronized (this) {
							wait(100);
						}
					}
				} catch (final InterruptedException e) {
				}
			}
		}

		public void cleanup() {
			mNeedWait = false;
			mWork = false;
			synchronized (this) {
				notify();
			}
			try {
				join();
			} catch (final InterruptedException e) {
				interrupt();
			}
			mNeedWait = true;
		}

		public void send(final T pak) {
			mNeedWait = false;
			synchronized (mList) {
				mList.add(pak);
			}
			synchronized (this) {
				notify();
			}
			mNeedWait = true;
		}

	};

	private void setConnecting(final boolean b) {
		mListAdapter.setEnable(!b);
		mIsConnecting = b;
		mHandler.post(new GuiUpdateRunnable());
	}

	public BtLanGame(final LanInterface<T> callBackInterface,
			final Context context) {
		mCallBackInterface = callBackInterface;
		mContext = context;
		mListAdapter = new BtArrayAdapter(context);
		if (mBluetoothAdapter != null) {
			final IntentFilter filter = new IntentFilter(
					BluetoothDevice.ACTION_FOUND);
			mContext.registerReceiver(mReceiver, filter);
			final IntentFilter filter2 = new IntentFilter(
					BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
			mContext.registerReceiver(mReceiver, filter2);
			final IntentFilter filter3 = new IntentFilter(
					BluetoothAdapter.ACTION_SCAN_MODE_CHANGED);
			mContext.registerReceiver(mReceiver, filter3);
		}
	}

	@Override
	public String getTextStatus() {
		return mTextStatus;
	}

	private void setTextStatus(final String status) {
		mTextStatus = status;
		mHandler.post(new GuiUpdateRunnable());
	}

	@Override
	public BtArrayAdapter getListAdapter() {
		return mListAdapter;
	}

	@Override
	public void cleanup() {
		mContext.unregisterReceiver(mReceiver);
		stopSearch();
		disconnect();
	}

	@Override
	public boolean isServer() {
		return mIsServer;
	}

	@Override
	public boolean isConnected() {
		return mIsConnected;
	}

	@Override
	public boolean isConnecting() {
		return mIsConnecting;
	}

	@Override
	public String getConnectingString() {
		return connectionStatus;
	}

	public void setBluetoothAdapter(final BluetoothAdapter adapter) {
		mBluetoothAdapter = adapter;
	}

	private void startScan() {
		if (mBluetoothAdapter != null) {
			mLastStartUpdateTime = SystemClock.uptimeMillis();
			mBTScanRequie = true;
			mBluetoothAdapter.startDiscovery();
		}
	}

	private void stopScan() {
		mBTScanRequie = false;
		mBluetoothAdapter.cancelDiscovery();
	}

	TimerTask mUpdateStatus = null;

	private class UpTimerTask extends TimerTask {
		@Override
		public void run() {
			if (mServerAutoShutDownTime <= SystemClock.uptimeMillis()) {
				cancel();
				if (!isConnected()) {
					mHandler.post(new TimeoutRunnable());
				}
				mUpdateStatus = null;
				stopSearch();
				return;
			}
			setTextStatus(mContext.getText(R.string.serverRun).toString()
					+ " "
					+ (mServerAutoShutDownTime - SystemClock.uptimeMillis())
					/ 1000
					+ " "
					+ mContext.getText(R.string.serverRun2).toString()
					+ " "
					+ mBluetoothAdapter.getName()
					+ " ["
					+ mBluetoothAdapter.getAddress()
					+ "]"
					+ ((mServerThread == null || mServerThread.isSocketExist()) ? ""
							: mContext.getText(R.string.serverRun3).toString()));
		}
	};

	@Override
	public void bluetoothDiscoverableEnabled() {
		final int timeout = 120; // default from system
		mServerAutoShutDownTime = SystemClock.uptimeMillis() + timeout * 1000;
	};

	private void startServer() {
		if (mServerThread != null) {
			mServerThread.cancel();
		}
		mServerThread = new ServerThread();
		mServerThread.start();

		if (mUpdateStatus == null) {
			mUpdateStatus = new UpTimerTask();
			mStatusUpdateTimer.schedule(mUpdateStatus, 10, 1000);
		}
	}

	public void stopServer() {
		if (mServerThread != null) {
			if (!mServerThread.isConnected) {
				mServerThread.cancel();
				mServerThread = null;
			}
		}
	}

	@Override
	public void connect(final int pos) {
		if (mConnectThread != null) {
			mConnectThread.cancel();
		}
		mConnectThread = new ConnectThread(mListAdapter.getItem(pos).device);
		mConnectThread.start();
		setConnectionStatusString(mContext.getString(R.string.connectString)
				+ " "
				+ BtArrayAdapter
						.getStringByDevice(mListAdapter.getItem(pos).device));
	}

	private void setConnectionStatusString(final String string) {
		connectionStatus = string;
		mHandler.post(new GuiUpdateRunnable());
	}

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(final Context context, final Intent intent) {
			final String action = intent.getAction();
			if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				if (mBTScanRequie) {
					mListAdapter.cleanOld(mLastStartUpdateTime);
					startScan();
				}
			}
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				final BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				mListAdapter.addDevice(device);
			}
			if (BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals(action)) {
				final int m = intent.getIntExtra(
						BluetoothAdapter.EXTRA_SCAN_MODE,
						BluetoothAdapter.SCAN_MODE_NONE);
				if (m != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
					if (mUpdateStatus != null) {
						mUpdateStatus.cancel();
						mUpdateStatus = null;
						if (!isConnected()) {
							mHandler.post(new TimeoutRunnable());
						}
					}
				}
			}
		}
	};

	@Override
	public void send(final T packet) {
		if (isServer()) {
			if (mServerThread != null) {
				mServerThread.send(packet);
			}
		} else if (mConnectThread != null) {
			mConnectThread.send(packet);
		}
	}

	@Override
	public void startSearch() {
		mIsSearchActive = true;
		startScan();
		startServer();
	}

	@Override
	public void stopSearch() {
		mIsSearchActive = false;
		stopScan();
		stopServer();
	}

	@Override
	public void disconnect() {
		if (mUpdateStatus != null) {
			mUpdateStatus.cancel();
			mUpdateStatus = null;
		}
		if (isServer()) {
			if (mServerThread != null) {
				mServerThread.cancel();
				mServerThread = null;
			}
		} else {
			if (mConnectThread != null) {
				mConnectThread.cancel();
				mConnectThread = null;
				setConnecting(false);
			}
		}
	}

}
