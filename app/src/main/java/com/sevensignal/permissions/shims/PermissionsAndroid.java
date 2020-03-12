package com.sevensignal.permissions.shims;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.sevensignal.permissions.utils.PermissionRequester;

public class PermissionsAndroid {

	private PermissionsAndroid() {}

	public static PermissionsAndroid create() {
		return new PermissionsAndroid();
	}

	public boolean checkPermission(Context context, PermissionRequester.Permissions permission) {
		return (ContextCompat.checkSelfPermission(context, permission.getDescription()) == PackageManager.PERMISSION_GRANTED);
	}

	public boolean shouldShowRequestPermissionRationale(Activity activity, PermissionRequester.Permissions permission) {
		return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.getDescription());
	}

	public void requestPermissions(Activity activity, String[] permissions, int requestCode) {
		ActivityCompat.requestPermissions(activity, permissions, requestCode);
	}

}
