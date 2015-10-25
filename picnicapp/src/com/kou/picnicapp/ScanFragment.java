package com.kou.picnicapp;

import java.util.ArrayList;
import java.util.HashMap;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
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

import com.kou.picnicapp.model.TargetData;
import com.kou.picnicapp.utils.LogWrapper;

public class ScanFragment extends Fragment implements OnClickListener {
	private static final String TAG = ScanFragment.class.getSimpleName();
	private static final String ARG_POSITION = "position";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 60000;

	private int position;
	private View mainView;

	private BluetoothAdapter bluetoothAdapter;
	private ListView lvDevice;
	private TargetListAdapter listAdapter;

	private Button btnScan;
	private boolean isScanning;
	private Handler handler = new Handler();

	public static ScanFragment newInstance(int position) {
		ScanFragment f = new ScanFragment();
		Bundle b = new Bundle();
		b.putInt(ARG_POSITION, position);
		f.setArguments(b);
		return f;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		position = getArguments().getInt(ARG_POSITION);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		mainView = inflater.inflate(R.layout.fragment_scan, null);

		Context context = getActivity();

		if (context != null) {
			if (!getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
				Toast.makeText(getActivity(), R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			}

			final BluetoothManager bluetoothManager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
			bluetoothAdapter = bluetoothManager.getAdapter();

			if (bluetoothAdapter == null) {
				Toast.makeText(getActivity(), R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			}
		}

		lvDevice = (ListView) mainView.findViewById(R.id.lvDevice);
		lvDevice.setDivider(null);

		listAdapter = new TargetListAdapter();
		lvDevice.setAdapter(listAdapter);
		lvDevice.setOnItemClickListener(onItemClickListener);

		btnScan = (Button) mainView.findViewById(R.id.btnScan);
		btnScan.setOnClickListener(this);

		return mainView;
		// LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		//
		// FrameLayout fl = new FrameLayout(getActivity());
		// fl.setLayoutParams(params);
		//
		// final int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics());
		//
		// TextView v = new TextView(getActivity());
		// params.setMargins(margin, margin, margin, margin);
		// v.setLayoutParams(params);
		// v.setLayoutParams(params);
		// v.setGravity(Gravity.CENTER);
		// v.setBackgroundResource(R.drawable.background_card);
		// v.setText("CARD " + (position + 1));
		//
		// fl.addView(v);
		// return fl;
	}

	@Override
	public void onResume() {
		super.onResume();

		if (!bluetoothAdapter.isEnabled()) {
			if (!bluetoothAdapter.isEnabled()) {
				Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
			}
		}

		scanLeDevice(true);

		setDummyData();

	}

	private void performStartScan() {
		scanLeDevice(true);
	}

	private void performStopScan() {
		scanLeDevice(false);
	}

	private void scanLeDevice(final boolean enable) {
		if (enable) {
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					isScanning = false;
					bluetoothAdapter.stopLeScan(mLeScanCallback);
				}
			}, SCAN_PERIOD);

			isScanning = true;
			bluetoothAdapter.startLeScan(mLeScanCallback);
		} else {
			isScanning = false;
			bluetoothAdapter.stopLeScan(mLeScanCallback);
		}
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

	// Adapter for holding devices found through scanning.
	private class TargetListAdapter extends BaseAdapter {
		private ArrayList<TargetData> targetlist = new ArrayList<TargetData>();
		private LayoutInflater inflator;

		public TargetListAdapter() {
			super();
			inflator = getActivity().getLayoutInflater();
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

			getActivity().runOnUiThread(new Runnable() {
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

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnScan:
			performStartScan();
			break;

		}
	}
}