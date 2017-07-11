package com.greenlogger.tennis;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.greenlogger.tenis.R;

public class BtArrayAdapter extends ArrayAdapter<BtDeviceScan> {
	private final Context mContext;
	private boolean mIsEnable = true;
	private final Handler mhandler = new Handler();

	public BtArrayAdapter(final Context context) {
		super(context, R.layout.btrow);
		mContext = context;
	}

	public void addDevice(final BluetoothDevice device) {
		for (int i = 0; i < getCount(); i++) {
			if (getItem(i).device.equals(device)) {
				getItem(i).lastSeenTime = SystemClock.uptimeMillis();
				getItem(i).device = device;
				return;
			}
		}
		final BtDeviceScan sdevice = new BtDeviceScan();
		sdevice.device = device;
		sdevice.lastSeenTime = SystemClock.uptimeMillis();
		super.add(sdevice);
		notifyDataSetChanged();
	}

	public void cleanOld(final long lastStartUpdateTime) {
		for (int i = 0; i < getCount(); i++) {
			if (getItem(i).lastSeenTime < lastStartUpdateTime) {
				remove(getItem(i));
				i--;
			}
		}
		notifyDataSetChanged();
	}

	public static String getStringByDevice(final BluetoothDevice device) {
		return device.getName() == null ? device.getAddress() : (device
				.getName() + " [" + device.getAddress() + "]");
	}

	@Override
	public View getView(final int position, final View convertView,
			final ViewGroup parent) {
		final LayoutInflater inflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View rowView = inflater.inflate(R.layout.btrow, null, true);

		final TextView textView = (TextView) rowView.findViewById(R.id.label);
		final BtDeviceScan sd = getItem(position);
		textView.setText(getStringByDevice(sd.device));
		textView.setEnabled(mIsEnable);
		CustomFont.overrideFonts(mContext, textView);
		sd.view = rowView;
		return rowView;
	}

	private final Runnable EnableUpdateRunnable = new Runnable() {
		public void run() {
			for (int i = 0; i < getCount(); i++) {
				if (getItem(i).view != null) {
					getItem(i).view.setEnabled(mIsEnable);
				}
			}
			notifyDataSetChanged();
		}
	};

	public void setEnable(final boolean b) {
		mIsEnable = b;
		mhandler.post(EnableUpdateRunnable);
	}

}
