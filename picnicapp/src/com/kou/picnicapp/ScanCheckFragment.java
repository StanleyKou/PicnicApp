package com.kou.picnicapp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.kou.picnicapp.model.BeaconBluetoothDevice;
import com.kou.picnicapp.model.TargetData;
import com.kou.picnicapp.model.TargetData.CHECK_STATE;
import com.kou.picnicapp.utils.LogWrapper;
import com.kou.picnicapp.utils.PreferenceUtils;

public class ScanCheckFragment extends Fragment implements OnClickListener {
	private static final String TAG = ScanCheckFragment.class.getSimpleName();
	private static final String ARG_POSITION = "position";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 60000;

	private int position;
	private View mainView;

	private BluetoothAdapter bluetoothAdapter;
	private TextView tvNodata;
	private ListView lvDevice;
	private TargetListAdapter listAdapter;

	private Button btnScan;
	private boolean isScanning;
	private Handler handler = new Handler();

	public static ScanCheckFragment newInstance(int position) {
		ScanCheckFragment f = new ScanCheckFragment();
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

		tvNodata = (TextView) mainView.findViewById(R.id.tvNodata);
		lvDevice = (ListView) mainView.findViewById(R.id.lvDevice);
		lvDevice.setDivider(null);

		listAdapter = new TargetListAdapter();
		lvDevice.setAdapter(listAdapter);
		lvDevice.setOnItemClickListener(onItemClickListener);

		btnScan = (Button) mainView.findViewById(R.id.btnScan);
		btnScan.setOnClickListener(this);

		return mainView;
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

		setTargetData();
	}

	private void performStartScan() {

		// FIXME: Checked state를 모두 리셋 해야 함.
		setTargetData();

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
		private ArrayList<TargetData> targetDatalist = new ArrayList<TargetData>();
		private LayoutInflater inflator;

		public TargetListAdapter() {
			super();
			inflator = getActivity().getLayoutInflater();
		}

		public void checkTarget(BeaconBluetoothDevice beacon) {

			for (TargetData t : targetDatalist) {
				if (t.getCheckState() == CHECK_STATE.FOUND) {
					break;
				} else {

					if (t.getUuid().equals(beacon.getUUID())//
							&& t.getMajor() == beacon.getMajor()//
							&& t.getMinor() == beacon.getMinor()) {

						t.setCheckState(CHECK_STATE.FOUND);

					}

				}
			}

			// targetDataList

			// if (!leTargetItems.contains(device)) {
			// // leDevices.add(device);
			//
			// }
		}

		public void setTargetData(ArrayList<TargetData> targetDatalist) {
			this.targetDatalist = targetDatalist;
		}

		public TargetData getDevice(int position) {
			return targetDatalist.get(position);
		}

		public void clear() {
			targetDatalist.clear();
		}

		@Override
		public int getCount() {
			return targetDatalist.size();
		}

		@Override
		public Object getItem(int i) {
			return targetDatalist.get(i);
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
				viewHolder.ivFound = (ImageView) view.findViewById(R.id.ivFound);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

			TargetData target = targetDatalist.get(i);

			viewHolder.tvTargetNumber.setText(target.number + "");
			viewHolder.tvTargetName.setText(target.name);
			viewHolder.tvTargetPhone.setText(target.phoneNumber);

			if (target.getCheckState() == CHECK_STATE.UNKNOWN) {
				viewHolder.ivFound.setImageResource(R.drawable.check_state_unknown);
			} else {
				viewHolder.ivFound.setImageResource(R.drawable.check_state_found);
			}

			return view;
		}
	}

	static class ViewHolder {
		TextView tvTargetNumber;
		TextView tvTargetName;
		TextView tvTargetPhone;
		ImageView ivFound;
	}

	HashMap<String, KalmanFilter> filteredList = new HashMap<String, KalmanFilter>();

	// Device scan callback.
	private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {

		@Override
		public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {

			final BeaconBluetoothDevice beacon = new BeaconBluetoothDevice(device, rssi, scanRecord);

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
					listAdapter.checkTarget(beacon);
					listAdapter.notifyDataSetChanged();
				}
			});
		}
	};

	private void setTargetData() {
		String strData = PreferenceUtils.getCheckList(getActivity());

		if (strData == null || strData.length() == 0) {
			tvNodata.setVisibility(View.VISIBLE);
			lvDevice.setVisibility(View.GONE);
		} else {
			Gson gson = new Gson();
			java.lang.reflect.Type listType = new TypeToken<List<TargetData>>() {
			}.getType();
			ArrayList<TargetData> targetDataList = gson.fromJson(strData, listType);
			listAdapter.setTargetData(targetDataList);
		}
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