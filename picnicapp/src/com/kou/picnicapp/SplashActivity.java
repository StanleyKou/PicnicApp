package com.kou.picnicapp;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.kou.picnicapp.base.BaseActivity;
import com.kou.picnicapp.utils.LogWrapper;
import com.kou.picnicapp.utils.Utils;

public class SplashActivity extends BaseActivity {
	private final String TAG = SplashActivity.class.getSimpleName();
	private final int SPLASH_TIME = 800;

	private RelativeLayout rlSplash;
	private RelativeLayout rlLogin;
	private RelativeLayout rlVersionCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		LogWrapper.d(TAG, TAG, "onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		rlSplash = (RelativeLayout) findViewById(R.id.rlSplash);
		rlVersionCode = (RelativeLayout) findViewById(R.id.rlVersionCode);

		if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
		BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

		if (bluetoothAdapter == null) {
			Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
			finish();
		}

		mHandler.postDelayed(mSplashRunnable, SPLASH_TIME);

		Utils.installShortcut(this);
		Utils.createDirIfNotExists(this);
	}

	private Handler mHandler = new Handler();
	private Runnable mSplashRunnable = new Runnable() {
		@Override
		public void run() {

			ObjectAnimator splashUpAnim = ObjectAnimator.ofFloat(rlSplash, "translationY", -320).setDuration(1000);
			splashUpAnim.setStartDelay(1000);
			splashUpAnim.start();

			ObjectAnimator splashAnim = ObjectAnimator.ofFloat(rlSplash, "alpha", 1, 0).setDuration(1000);
			splashAnim.setStartDelay(1500);
			splashAnim.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					rlVersionCode.setVisibility(View.GONE);
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {
					rlSplash.setVisibility(View.GONE);

					Intent i = new Intent(SplashActivity.this, TabMainActivity.class);
					startActivity(i);
					finish();
				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			splashAnim.start();

			// ObjectAnimator loginPositionAnim = ObjectAnimator.ofFloat(rlLogin, "translationY", 200).setDuration(0);
			// loginPositionAnim.setStartDelay(0);
			// loginPositionAnim.start();

			// ObjectAnimator loginUpAnim = ObjectAnimator.ofFloat(rlLogin, "translationY", -200).setDuration(1000);
			// loginUpAnim.setStartDelay(800);
			// loginUpAnim.start();

			ObjectAnimator loginAlphaAnim = ObjectAnimator.ofFloat(rlLogin, "alpha", 0, 1).setDuration(1000);
			loginAlphaAnim.setStartDelay(1500);
			loginAlphaAnim.addListener(new AnimatorListener() {

				@Override
				public void onAnimationStart(Animator animation) {
					// rlLogin.setVisibility(View.VISIBLE);
				}

				@Override
				public void onAnimationRepeat(Animator animation) {
				}

				@Override
				public void onAnimationEnd(Animator animation) {

				}

				@Override
				public void onAnimationCancel(Animator animation) {
				}
			});
			loginAlphaAnim.start();

			// ObjectAnimator loginMoveToLeftAnim = ObjectAnimator.ofFloat(rlLogin, "translationX", 400, 0).setDuration(1200);
			// loginMoveToLeftAnim.setStartDelay(1500);
			// loginMoveToLeftAnim.setInterpolator(new BounceInterpolator());
			// loginMoveToLeftAnim.start();

		}
	};

	@Override
	protected void onResume() {
		super.onResume();

	}

}
