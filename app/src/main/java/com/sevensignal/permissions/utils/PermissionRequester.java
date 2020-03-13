package com.sevensignal.permissions.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import com.sevensignal.permissions.R;
import com.sevensignal.permissions.shims.PermissionsAndroid;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static com.sevensignal.permissions.logging.LoggingConstants.PERM_REQUEST_PERM;

public class PermissionRequester {

	private PermissionRequester() {}

	public static PermissionRequester getPermissionRequester() {
		return new PermissionRequester();
	}

	public enum Permissions {
		PERMISSION_TO_READ_ACCOUNT_INFO(101, Manifest.permission.GET_ACCOUNTS, R.string.account_permission_reason),
		PERMISSION_TO_READ_LOCATION_INFO(102, Manifest.permission.ACCESS_COARSE_LOCATION, R.string.location_permission_reason),
		PERMISSION_TO_READ_PHONE_INFO(103, Manifest.permission.READ_PHONE_STATE, R.string.phone_permission_reason),
		PERMISSION_INVALID(999, "android.permission.invalid", R.string.all_permission_reason);

		@Getter private final int requestCode;
		@Getter private final String description;
		@Getter private final int reasonId;

		Permissions(int requestCode, String description, int reasonId) {
			this.requestCode = requestCode;
			this.description = description;
			this.reasonId = reasonId;
		}

		public static Permissions getById(final int id) {
			for (Permissions permission : Permissions.values()) {
				if (id == permission.getRequestCode()) {
					return permission;
				}
			}
			return Permissions.PERMISSION_INVALID;
		}
	}

	public PermissionRequestJob beginRequestingAllPermissions() {
		return new PermissionRequestJob(getAllValidPermissions());
	}

	public Permissions getNextPermissionToRequest(final Activity activity, final PermissionRequestJob permissionRequestJob) {
		if (permissionRequestJob == null) {
			throw new IllegalArgumentException();
		}
		List<PermissionRequest> permissionRequestList = permissionRequestJob.getPermissionsToRequest();
		for (PermissionRequest permissionRequest : permissionRequestList) {
			switch (permissionRequest.getRequestingState()) {
				case WAITING:
					if (shouldRequestPermission(activity, permissionRequest.getPermission())) {
						permissionRequest.setRequestingState(PermissionRequestingStates.REQUESTING);
						return permissionRequest.getPermission();
					} else {
						permissionRequest.setRequestingState(PermissionRequestingStates.REQUESTED);
						break;
					}

				case REQUESTED:
					break;

				case REQUESTING:
					permissionRequest.setRequestingState(PermissionRequestingStates.REQUESTED);
					break;

				default:
					Log.e(PERM_REQUEST_PERM, "Unexpected request permission state: " + permissionRequest.getRequestingState());
					break;
			}
		}
		return null;
	}

	private boolean shouldRequestPermission(Activity activity, Permissions permission) {
		PermissionsAndroid android = PermissionsAndroid.create();
		return (!android.checkPermission(activity, permission) &&
				android.shouldShowRequestPermissionRationale(activity, permission));
	}

	private List<Permissions> getAllValidPermissions() {
		List<Permissions> permissions = new ArrayList<>();
		for (Permissions permission : Permissions.values()) {
			if (permission != Permissions.PERMISSION_INVALID) {
				permissions.add(permission);
			}
		}
		return permissions;
	}

	public boolean areAllRequiredPermissionsGranted(final Context context) {
		PermissionsAndroid android = PermissionsAndroid.create();
		boolean permissionsGranted = true;
		for (Permissions permission : getAllValidPermissions()) {
			permissionsGranted = permissionsGranted && android.checkPermission(context, permission);
		}
		return permissionsGranted;
	}

	public void finishRequestingPermission(final Activity activity, final Permissions permission) {
		PermissionsAndroid android = PermissionsAndroid.create();
		String[] permissions = {permission.getDescription()};
		android.requestPermissions(activity, permissions, permission.getRequestCode());
	}

	public boolean isPermissionGranted(final int requestCode, final String[] permissions, final int[] grantResults) {
		final Permissions requestedPermission = Permissions.getById(requestCode);
		if (requestedPermission == Permissions.PERMISSION_INVALID ||
			permissions == null ||
			grantResults == null ||
			permissions.length != grantResults.length) {

			return false;
		} else {
			for (int i = 0 ; i < grantResults.length ; i++) {
				if (permissions[i].equals(requestedPermission.getDescription())) {
					return grantResults[i] == PackageManager.PERMISSION_GRANTED;
				}
			}
		}
		return false;
	}
}
