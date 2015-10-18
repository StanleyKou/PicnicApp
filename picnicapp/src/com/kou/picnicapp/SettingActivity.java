package com.kou.picnicapp;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

import com.kou.picnicapp.base.BaseActivity;

public class SettingActivity extends BaseActivity implements OnClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting);

	}

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		}

	}
}
