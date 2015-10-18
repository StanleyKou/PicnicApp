/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.kou.picnicapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.kou.picnicapp.base.BaseActivity;
import com.kou.picnicapp.model.TargetItem;
import com.kou.picnicapp.utils.LogWrapper;

public class DeviceScanActivity extends BaseActivity implements OnClickListener {

	private static final String TAG = DeviceScanActivity.class.getSimpleName();

	private BluetoothAdapter bluetoothAdapter;
	private boolean isScanning;
	private Handler handler = new Handler();

	private static final int REQUEST_ENABLE_BT = 1;
	// Stops scanning after 60 seconds.
	private static final long SCAN_PERIOD = 60000;

	private ListView lvDevice;
	private listAdapter listAdapter;

	private TextView tvBeaconScanNodata;

	private Button btnScanStart;
	private Button btnScanStop;
	private Button btnSetting;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_scan);

		// Use this check to determine whether BLE is supported on the device. Then you can
		// selectively disable BLE-related features.
		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		// Initializes a Bluetooth adapter. For API level 18 and above, get a reference to
		// BluetoothAdapter through BluetoothManager.
		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		bluetoothAdapter = bluetoothManager.getAdapter();

		// Checks if Bluetooth is supported on the device.
		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
			return;
		}

		lvDevice = (ListView) findViewById(R.id.lvDevice);
		lvDevice.setDivider(null);

		listAdapter = new listAdapter();
		lvDevice.setAdapter(listAdapter);
		lvDevice.setOnItemClickListener(onItemClickListener);

		tvBeaconScanNodata = (TextView) findViewById(R.id.tvBeaconScanNodata);

		btnScanStart = (Button) findViewById(R.id.btnScanStart);
		btnScanStop = (Button) findViewById(R.id.btnScanStop);
		btnSetting = (Button) findViewById(R.id.btnSetting);

		btnScanStart.setOnClickListener(this);
		btnScanStop.setOnClickListener(this);
		btnSetting.setOnClickListener(this);

	}

	// @Override
	// public boolean onCreateOptionsMenu(Menu menu) {
	// getMenuInflater().inflate(R.menu.main, menu);
	// if (!mScanning) {
	// menu.findItem(R.id.menu_stop).setVisible(false);
	// menu.findItem(R.id.menu_scan).setVisible(true);
	// menu.findItem(R.id.menu_refresh).setActionView(null);
	// } else {
	// menu.findItem(R.id.menu_stop).setVisible(true);
	// menu.findItem(R.id.menu_scan).setVisible(false);
	// menu.findItem(R.id.menu_refresh).setActionView(R.layout.actionbar_indeterminate_progress);
	// }
	// return true;
	// }

	// @Override
	// public boolean onOptionsItemSelected(MenuItem item) {
	// switch (item.getItemId()) {
	// case R.id.menu_scan:
	// performStartScan();
	// break;
	// case R.id.menu_stop:
	// performStopScan();
	// break;
	// }
	// return true;
	// }

	private void performStartScan() {
		listAdapter.clear();
		tvBeaconScanNodata.setVisibility(View.VISIBLE);
		lvDevice.setVisibility(View.GONE);
		scanLeDevice(true);
	}

	private void performStopScan() {
		scanLeDevice(false);
	}

	private void performSetting() {
		Intent intent = new Intent(DeviceScanActivity.this, SettingActivity.class);
		startActivity(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();

		// Ensures Bluetooth is enabled on the device. If Bluetooth is not currently enabled,
		// fire an intent to display a dialog asking the user to grant permission to enable it.
		if (!bluetoothAdapter.isEnabled()) {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		scanLeDevice(true);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// User chose not to enable Bluetooth.
		if (requestCode == REQUEST_ENABLE_BT && resultCode == Activity.RESULT_CANCELED) {
			finish();
			return;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	protected void onPause() {
		super.onPause();
		scanLeDevice(false);
		listAdapter.clear();
	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			final BluetoothDevice device = listAdapter.getDevice(position);
			if (device == null)
				return;
			// final Intent intent = new Intent(this, DeviceControlActivity.class);
			// intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_NAME, device.getName());
			// intent.putExtra(DeviceControlActivity.EXTRAS_DEVICE_ADDRESS, device.getAddress());
			// // if (mScanning) {
			// // mBluetoothAdapter.stopLeScan(mLeScanCallback);
			// // mScanning = false;
			// // }
			// startActivity(intent);
		}
	};

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnScanStart:
			performStartScan();
			break;

		case R.id.btnScanStop:
			performStopScan();
			break;

		case R.id.btnSetting:
			performSetting();
			break;
		}
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			// Stops scanning after a pre-defined scan period.
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isScanning = false;
					bluetoothAdapter.stopLeScan(mLeScanCallback);
					// invalidateOptionsMenu();
				}
			}, SCAN_PERIOD);

			isScanning = true;
			bluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			isScanning = false;
			bluetoothAdapter.stopLeScan(mLeScanCallback);
		}
		invalidateOptionsMenu();
	}

	// Adapter for holding devices found through scanning.
	private class listAdapter extends BaseAdapter {
		private ArrayList<TargetItem> leTargetItems;
		private LayoutInflater inflator;

		public listAdapter() {
			super();
			leTargetItems = new ArrayList<TargetItem>();
			inflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void checkTarget(BluetoothDevice device) {
			if (!leTargetItems.contains(device)) {
				// leDevices.add(device);

				tvBeaconScanNodata.setVisibility(View.GONE);
				lvDevice.setVisibility(View.VISIBLE);
			}
		}

		public TargetItem getDevice(int position) {
			return leTargetItems.get(position);
		}

		public void clear() {
			leTargetItems.clear();
		}

		@Override
		public int getCount() {
			return leTargetItems.size();
		}

		@Override
		public Object getItem(int i) {
			return leTargetItems.get(i);
		}

		@Override
		public long getItemId(int i) {
			return i;
		}

		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			ViewHolder viewHolder;
			// General ListView optimization code.
			if (view == null) {
				view = inflator.inflate(R.layout.listitem_device, null);
				viewHolder = new ViewHolder();
				viewHolder.deviceAddress = (TextView) view.findViewById(R.id.device_address);
				viewHolder.deviceName = (TextView) view.findViewById(R.id.device_name);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			// FIXME: Display

			// BluetoothDevice device = leTargetItems.get(i);
			// final String deviceName = device.getName();
			// if (deviceName != null && deviceName.length() > 0)
			// viewHolder.deviceName.setText(deviceName);
			// else
			// viewHolder.deviceName.setText(R.string.unknown_device);
			// viewHolder.deviceAddress.setText(device.getAddress());

			return view;
		}
	}

	HashMap<String, KalmanFilter> filteredList = new HashMap<String, KalmanFilter>();

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

			KalmanFilter found = filteredList.get(device.getAddress());
			int filteredRssi = rssi;
			if (found == null) {
				KalmanFilter k = new KalmanFilter(rssi);
				filteredList.put(device.getAddress(), k);
			} else {
				filteredRssi = (int) found.update(rssi);
			}

			LogWrapper.d(TAG, "mac: " + device.getAddress() + " rssi: " + rssi + " filt:" + filteredRssi);

			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					listAdapter.checkTarget(device);
					listAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	static class ViewHolder {
		TextView deviceName;
		TextView deviceAddress;
	}

}