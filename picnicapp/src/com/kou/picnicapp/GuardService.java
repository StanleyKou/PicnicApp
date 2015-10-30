package com.kou.picnicapp;

import java.util.Timer;
import java.util.TimerTask;

import com.kou.picnicapp.utils.LogWrapper;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothAdapter.LeScanCallback;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;

// FIXME: Always running service. MUST BE RESURRECT ALWAYS.

// http://stackoverflow.com/questions/15758980/android-service-need-to-run-alwaysnever-pause-or-stop

public class GuardService extends Service {
	private final static String TAG = GuardService.class.getSimpleName();

	private BluetoothAdapter mBluetoothAdapter;
	private boolean isRunning = false;

	static final int MSG_START_SCAN = 1010;
	static final int MSG_STOP_SCAN = 1020;
	static final int MSG_UNBIND = 1030;
	static final int MSG_SCAN_RESULT = 1040;
	static final String SCAN_RESULT_DEVICE = "SCAN_RESULT_DEVICE";
	static final String SCAN_RESULT_RSSI = "SCAN_RESULT_RSSI";
	static final String SCAN_RESULT_SCANRECORD = "SCAN_RESULT_SCANRECORD";

	private final int LONG_SCAN_PERIOD = 3050;
	private final int BLE_RESULT_REFRESH_TIME = 5000;

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return START_STICKY; // run until explicitly stopped.
	}

	public boolean isRunning() {
		return isRunning;
	}

	@Override
	public void onDestroy() {
		isRunning = false;
	}

	private Timer drawRefreshTimer = null;
	private Messenger mReplyToMessenger = null;
	final Messenger mMessenger = new Messenger(new Handler(new Handler.Callback() {

		@Override
		public boolean handleMessage(Message msg) {

			switch (msg.what) {
			case MSG_START_SCAN:

				leScancallBack = new LeScanCallback() {
					@Override
					public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
						sendScanResult(device, rssi, scanRecord);
					}
				};

				mReplyToMessenger = msg.replyTo;

				if (drawRefreshTimer == null) {
					drawRefreshTimer = new Timer();
				}

				drawRefreshTimer.schedule(new TimerTask() {
					@Override
					public void run() {
						startLeScan();
					}

				}, 0, BLE_RESULT_REFRESH_TIME);

				break;
			case MSG_STOP_SCAN:
				mReplyToMessenger = msg.replyTo;
				stopLeScan();
				break;

			case MSG_UNBIND:
				mReplyToMessenger = msg.replyTo;
				stopLeScan();
				break;
			}

			return false;
		}

	}));

	public void startLeScan() {
		try {
			if (isRunning == false) {
				mBluetoothAdapter.startLeScan(leScancallBack);
				isRunning = true;
			}
			stopLeScanOverPeriod();

		} catch (Exception e) {
			isRunning = false;
		}
	}

	private Handler mHandler = new Handler();

	public void stopLeScanOnce() {
		isRunning = false;
		mBluetoothAdapter.stopLeScan(leScancallBack);
	}

	public void stopLeScanOverPeriod() {
		if (BLE_RESULT_REFRESH_TIME > LONG_SCAN_PERIOD) {
			mHandler.postDelayed(new Runnable() {
				@Override
				public void run() {
					stopLeScanOnce();
				}
			}, LONG_SCAN_PERIOD);
		}
	}

	public void stopLeScan() {
		try {
			stopLeScanOnce();

			if (drawRefreshTimer != null) {
				drawRefreshTimer.cancel();
				drawRefreshTimer.purge();
				drawRefreshTimer = null;
			}
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		mBluetoothAdapter = bluetoothManager.getAdapter();
		return mMessenger.getBinder();
	}

	@Override
	public boolean onUnbind(Intent intent) {
		stopSelf();
		return super.onUnbind(intent);
	}

	private static LeScanCallback leScancallBack;

	private void sendScanResult(BluetoothDevice device, int rssi, byte[] scanRecord) {

		LogWrapper.d(TAG, "sendScanResult");

		Bundle b = new Bundle();
		b.putParcelable(SCAN_RESULT_DEVICE, device);
		b.putInt(SCAN_RESULT_RSSI, rssi);
		b.putByteArray(SCAN_RESULT_SCANRECORD, scanRecord);

		Message msg = Message.obtain(null, MSG_SCAN_RESULT);
		msg.setData(b);
		try {
			mReplyToMessenger.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

}