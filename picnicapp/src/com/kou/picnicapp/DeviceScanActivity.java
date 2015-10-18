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
import com.kou.picnicapp.model.TargetData;
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
	private TargetListAdapter listAdapter;

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

		listAdapter = new TargetListAdapter();
		lvDevice.setAdapter(listAdapter);
		lvDevice.setOnItemClickListener(onItemClickListener);

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

		setDummyData();

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

	}

	private OnItemClickListener onItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			// final BluetoothDevice device = listAdapter.getDevice(position);
			// if (device == null)
			// return;
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
	private class TargetListAdapter extends BaseAdapter {
		private ArrayList<TargetData> targetlist = new ArrayList<TargetData>();
		private LayoutInflater inflator;

		public TargetListAdapter() {
			super();
			inflator = DeviceScanActivity.this.getLayoutInflater();
		}

		public void checkTarget(BluetoothDevice device) {
			// if (!leTargetItems.contains(device)) {
			// // leDevices.add(device);
			//
			// }
		}

		public void setTargetData(ArrayList<TargetData> targetlist) {
			this.targetlist = targetlist;
		}

		public TargetData getDevice(int position) {
			return targetlist.get(position);
		}

		public void clear() {
			targetlist.clear();
		}

		@Override
		public int getCount() {
			return targetlist.size();
		}

		@Override
		public Object getItem(int i) {
			return targetlist.get(i);
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
				view = inflator.inflate(R.layout.listitem_target, null);
				viewHolder = new ViewHolder();
				viewHolder.tvTargetNumber = (TextView) view.findViewById(R.id.tvTargetNumber);
				viewHolder.tvTargetName = (TextView) view.findViewById(R.id.tvTargetName);
				viewHolder.tvTargetPhone = (TextView) view.findViewById(R.id.tvTargetPhone);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			// FIXME: Display

			TargetData target = targetlist.get(i);

			viewHolder.tvTargetNumber.setText(target.number + "");
			viewHolder.tvTargetName.setText(target.name);
			viewHolder.tvTargetPhone.setText(target.phoneNumber);

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

	static class ViewHolder {
		TextView tvTargetNumber;
		TextView tvTargetName;
		TextView tvTargetPhone;
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

	private void setDummyData() {
		ArrayList<TargetData> dummyTargetlist = new ArrayList<TargetData>();

		TargetData target1 = new TargetData();
		TargetData target2 = new TargetData();
		TargetData target3 = new TargetData();
		TargetData target4 = new TargetData();
		TargetData target5 = new TargetData();
		TargetData target6 = new TargetData();
		TargetData target7 = new TargetData();
		TargetData target8 = new TargetData();
		TargetData target9 = new TargetData();
		TargetData target10 = new TargetData();

		target1.sequence_id = 1;
		target2.sequence_id = 2;
		target3.sequence_id = 3;
		target4.sequence_id = 4;
		target5.sequence_id = 5;
		target6.sequence_id = 6;
		target7.sequence_id = 7;
		target8.sequence_id = 8;
		target9.sequence_id = 9;
		target10.sequence_id = 10;

		target1.number = "1";
		target2.number = "2";
		target3.number = "3";
		target4.number = "4";
		target5.number = "5";
		target6.number = "6";
		target7.number = "7";
		target8.number = "8";
		target9.number = "9";
		target10.number = "10";

		target1.name = "김일동";
		target2.name = "김이동";
		target3.name = "김삼동";
		target4.name = "김사동";
		target5.name = "김오동";
		target6.name = "김육동";
		target7.name = "김칠동";
		target8.name = "김팔동";
		target9.name = "김구동";
		target10.name = "김십동";

		target1.phoneNumber = "010-1111-0001";
		target2.phoneNumber = "010-1111-0002";
		target3.phoneNumber = "010-1111-0003";
		target4.phoneNumber = "010-1111-0004";
		target5.phoneNumber = "010-1111-0005";
		target6.phoneNumber = "010-1111-0006";
		target7.phoneNumber = "010-1111-0007";
		target8.phoneNumber = "010-1111-0008";
		target9.phoneNumber = "010-1111-0009";
		target10.phoneNumber = "010-1111-0010";

		target1.uuid = "1234";
		target2.uuid = "1234";
		target3.uuid = "1234";
		target4.uuid = "1234";
		target5.uuid = "1234";
		target6.uuid = "1234";
		target7.uuid = "1234";
		target8.uuid = "1234";
		target9.uuid = "1234";
		target10.uuid = "1234";

		target1.major = "16";
		target2.major = "16";
		target3.major = "16";
		target4.major = "16";
		target5.major = "16";
		target6.major = "16";
		target7.major = "16";
		target8.major = "16";
		target9.major = "16";
		target10.major = "16";

		target1.minor = "10002";
		target2.minor = "16";
		target3.minor = "16";
		target4.minor = "16";
		target5.minor = "16";
		target6.minor = "16";
		target7.minor = "16";
		target8.minor = "16";
		target9.minor = "16";
		target10.minor = "16";

		dummyTargetlist.add(target1);
		dummyTargetlist.add(target2);
		dummyTargetlist.add(target3);
		dummyTargetlist.add(target4);
		dummyTargetlist.add(target5);
		dummyTargetlist.add(target6);
		dummyTargetlist.add(target7);
		dummyTargetlist.add(target8);
		dummyTargetlist.add(target9);
		dummyTargetlist.add(target10);

		listAdapter.setTargetData(dummyTargetlist);

	}

	// <!-- http://www.andykhan.com/jexcelapi/index.html LGPL-->
	// Workbook workbook = null;
	// Sheet sheet = null;
	//
	// try {
	// InputStream is = getBaseContext().getResources().getAssets().open("notes.xlsx");
	// workbook = Workbook.getWorkbook(is);
	//
	// if (workbook != null) {
	// sheet = workbook.getSheet(0);
	//
	// if (sheet != null) {
	//
	// int nMaxColumn = 2;
	// int nRowStartIndex = 0;
	// int nRowEndIndex = sheet.getColumn(nMaxColumn - 1).length - 1;
	// int nColumnStartIndex = 0;
	// int nColumnEndIndex = sheet.getRow(2).length - 1;
	//
	// dbAdapter.open();
	// for (int nRow = nRowStartIndex; nRow <= nRowEndIndex; nRow++) {
	// String title = sheet.getCell(nColumnStartIndex, nRow).getContents();
	// String body = sheet.getCell(nColumnStartIndex + 1, nRow).getContents();
	// dbAdapter.createNote(title, body);
	// }
	// dbAdapter.close();
	// } else {
	// System.out.println("Sheet is null!!");
	// }
	// } else {
	// System.out.println("WorkBook is null!!");
	// }
	// } catch (Exception e) {
	// e.printStackTrace();
	// } finally {
	// if (workbook != null) {
	// workbook.close();
	// }
	// }

}