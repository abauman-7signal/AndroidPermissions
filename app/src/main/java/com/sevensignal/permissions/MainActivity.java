package com.sevensignal.permissions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sevensignal.permissions.logging.LoggingConstants;
import com.sevensignal.permissions.utils.PermissionRequester;

public class MainActivity extends AppCompatActivity {

	private static final String CLASS_NAME = MainActivity.class.getSimpleName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d (LoggingConstants.PERM_UI, CLASS_NAME + " onCreate");
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.d (LoggingConstants.PERM_UI, CLASS_NAME + " onStart");

//		permissionRequester.requestPermissions(this, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO);
//        permissionRequester.requestAllPermissions(this);
		requestAllPermissions();
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.d(LoggingConstants.PERM_UI, CLASS_NAME + " onResume");
	}

	@Override
	protected void onSaveInstanceState(Bundle instanceState) {
		super.onSaveInstanceState(instanceState);
		Log.d(LoggingConstants.PERM_UI, CLASS_NAME + " onSaveInstanceState");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.d (LoggingConstants.PERM_UI, CLASS_NAME + " onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy(); // allow base classes to clean up resources also
		Log.d (LoggingConstants.PERM_UI, CLASS_NAME + " onDestroy()");
	}

	private void requestAllPermissions() {
		final PermissionRequester permissionRequester = PermissionRequester.getPermissionRequester();
		permissionRequester.requestAllPermissions(this);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
		Log.w(LoggingConstants.PERM_UI, "received onRequestPermissionsResult");
		final PermissionRequester permissionRequester = PermissionRequester.getPermissionRequester();

		if (requestCode == PermissionRequester.REQUEST_CODE_FOR_ALL_PERMISSIONS) {
		    if (permissionRequester.areAllRequiredPermissionsGranted(this)) {
		        Log.d(LoggingConstants.PERM_UI, "All permissions granted");
            } else {
		        Log.w(LoggingConstants.PERM_UI, "Not all required permissions are granted");
            }
        } else {
            if (!permissionRequester.isPermissionGranted(requestCode, permissions, grantResults)) {
                Log.w(LoggingConstants.PERM_UI, "User denied permission for " + PermissionRequester.Permissions.getById(requestCode).getDescription());
            } else {
                Log.d(LoggingConstants.PERM_UI, "User granted permission for " + PermissionRequester.Permissions.getById(requestCode).getDescription());
            }
        }
	}

}
