package com.kou.picnicapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MainActivity extends Activity implements OnClickListener {

	private Button btnScanStart;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		btnScanStart = (Button) findViewById(R.id.btnScanStart);
		btnScanStart.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.btnScanStart:

			Intent intent = new Intent(this, DeviceScanActivity.class);
			startActivity(intent);

			break;
		}

	}
}
