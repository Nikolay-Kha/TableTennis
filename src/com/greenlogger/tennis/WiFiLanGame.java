package com.greenlogger.tennis;

import android.content.Context;
import android.widget.ListAdapter;

public class WiFiLanGame<T extends LanPacket> extends LanGame<T> /*implements WifiP2pManager.ChannelListener*/{

	/*private WifiP2pManager mManager;
	private WifiP2pManager.Channel mChanel;*/
	/*private final LanInterface<T> mCallBackInterface;
	private Context mContext;
	private boolean isSearch = false;*/
	private final boolean isConnected = false;
	private final BtArrayAdapter mListAdapter;
	private final String mStatusString = "";

	public WiFiLanGame(final LanInterface<T> callBackInterface,
			final Context context) {
		mListAdapter = new BtArrayAdapter(context);
		/*mCallBackInterface = callBackInterface;
		mContext = context;
		mManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
		
		final IntentFilter filter = new IntentFilter(
				WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		mContext.registerReceiver(mReceiver, filter);*/
	}

	@Override
	public ListAdapter getListAdapter() {
		return mListAdapter;
	}

	@Override
	public void cleanup() {
		stopSearch();
		disconnect();
	}

	@Override
	public boolean isConnected() {
		return isConnected;
	}

	@Override
	public void send(final T pak) {
		// TODO Auto-generated method stub

	}

	@Override
	public void stopSearch() {
		//isSearch = false;
	}

	@Override
	public void disconnect() {
		// TODO Auto-generated method stub

	}

	/*public class SearchListenet implements WifiP2pManager.ActionListener {
		public void onFailure(int arg0) {
			if(isSearch) {
				startSearch();		
			}
		}

		public void onSuccess() {
			if(isSearch) {
				startSearch();
			}
		}
		
	};
	SearchListenet mSearchListenet = new SearchListenet();
	
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent arg1) {
			mManager.requestPeers(mChanel, new PeerListListener() {
				public void onPeersAvailable(WifiP2pDeviceList peers) {
					Log.i("tag", "found "+peers.toString());
					
				}
			});			
		}
	};*/

	@Override
	public void startSearch() {
		/*	mChanel = mManager.initialize(mContext, Looper.getMainLooper(), this);
			if(mChanel!=null) {
				mManager.discoverPeers(mChanel,mSearchListenet);
			} else {
				mStatusString = mContext.getString(R.string.connectError);
			}*/
	}

	@Override
	public boolean isServer() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void connect(final int id) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getTextStatus() {
		return mStatusString;
	}

	@Override
	public boolean isConnecting() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getConnectingString() {
		return mStatusString;
	}

	/*public void onChannelDisconnected() {
		// TODO Auto-generated method stub
		mChanel = mManager.initialize(mContext, Looper.getMainLooper(), this);
		Log.i("tag", "onChannelDisconnected");
		
	}*/

}
