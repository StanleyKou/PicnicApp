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

public class SettingsFragment extends Fragment implements OnClickListener {
	private static final String TAG = SettingsFragment.class.getSimpleName();
	private static final String ARG_POSITION = "position";
	private static final int REQUEST_ENABLE_BT = 1;
	private static final long SCAN_PERIOD = 60000;

	private int position;
	private View mainView;

	private Handler handler = new Handler();

	public static SettingsFragment newInstance(int position) {
		SettingsFragment f = new SettingsFragment();
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

		mainView = inflater.inflate(R.layout.fragment_setting, null);

		Context context = getActivity();
		return mainView;
	}

	@Override
	public void onResume() {
		super.onResume();

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnScan:
			break;

		}
	}
}