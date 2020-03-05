package com.sevensignal.permissions;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.sevensignal.permissions.utils.PermissionRequester;

public class MainActivity extends AppCompatActivity {

	private static final String TAG = "aab";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();

		PermissionRequester permissionRequester = new PermissionRequester();
//		permissionRequester.requestPermissions(this, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO);
        permissionRequester.requestAllPermissions(this);
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
		Log.w(TAG, "received onRequestPermissionsResult");
		PermissionRequester permissionRequester = new PermissionRequester();

		if (requestCode == PermissionRequester.REQUEST_CODE_FOR_ALL_PERMISSIONS) {
		    if (permissionRequester.areAllRequiredPermissionsGranted(this)) {
		        Log.d(TAG, "All permissions granted");
            } else {
		        Log.w(TAG, "Not all required permissions are granted");
            }
        } else {
            if (!permissionRequester.isPermissionGranted(requestCode, permissions, grantResults)) {
                Log.w(TAG, "User denied permission for " + PermissionRequester.Permissions.getById(requestCode).getDescription());
            } else {
                Log.d(TAG, "User granted permission for " + PermissionRequester.Permissions.getById(requestCode).getDescription());
            }
        }
	}

}
