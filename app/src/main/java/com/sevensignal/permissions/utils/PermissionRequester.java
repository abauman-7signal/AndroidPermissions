package com.sevensignal.permissions.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.sevensignal.permissions.R;
import com.sevensignal.permissions.view.PermissionClarificationDialog;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;

import static com.sevensignal.permissions.logging.LoggingConstants.PERM_REQUEST_PERM;

public class PermissionRequester {

	private final static String PERMISSION_REQUESTER_DIALOG_TAG = "permissionRequesterDialogTag";

	private PermissionRequester() {}

	public static PermissionRequester getPermissionRequester() {
		return new PermissionRequester();
	}

	public static final int REQUEST_CODE_FOR_ALL_PERMISSIONS = 999;

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

	public boolean requestNextPermission(final Activity activity, final PermissionRequestJob permissionRequestJob) {
		if (permissionRequestJob == null) {
			throw new IllegalArgumentException();
		}
		List<PermissionRequest> permissionRequestList = permissionRequestJob.getPermissionsToRequest();
		for (PermissionRequest permissionRequest : permissionRequestList) {
			switch (permissionRequest.getRequestingState()) {
				case WAITING:
					if (shouldRequestPermission(activity, permissionRequest.getPermission())) {
						permissionRequest.setRequestingState(PermissionRequestingStates.REQUESTING);
						return true;
					} else {
						permissionRequest.setRequestingState(PermissionRequestingStates.REQUESTED);
						return false;
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
		return false;
	}

	private boolean shouldRequestPermission(Activity activity, Permissions permission) {
		return (!checkPermission(activity, permission) &&
				shouldShowRequestPermissionRationale(activity, permission));
	}

	boolean checkPermission(Context context, Permissions permission) {
		return (ContextCompat.checkSelfPermission(context, permission.getDescription()) == PackageManager.PERMISSION_GRANTED);
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
		boolean permissionsGranted = true;
		for (Permissions permission : getAllValidPermissions()) {
			permissionsGranted = permissionsGranted && checkPermission(context, permission);
		}
		return permissionsGranted;
	}

	public void requestPermissions(Activity activity, final Permissions permission) {
		if (!checkPermission(activity, permission)) {
			showRequestPermissionRationale(activity, permission);
		}
	}

	public void finishRequestPermissions(final Activity activity, final Permissions permission) {
			String[] permissions = {permission.getDescription()};
			ActivityCompat.requestPermissions(activity, permissions, permission.getRequestCode());
	}

	private void showRequestPermissionRationale(final Activity activity, final Permissions permission) {
		if (!checkPermission(activity, permission) &&
			shouldShowRequestPermissionRationale(activity, permission)) {

			PermissionClarificationDialog.build(
					"",
					activity.getResources().getString(permission.getReasonId()),
					permission)
					.show(activity.getFragmentManager(), PERMISSION_REQUESTER_DIALOG_TAG);
		}
	}

	private boolean shouldShowRequestPermissionRationale(Activity activity, Permissions permission) {
		return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission.getDescription());
	}

	public void requestAllPermissions(final Activity activity) {
		if (!areAllRequiredPermissionsGranted(activity)) {
			for (Permissions permission : getAllValidPermissions()) {
				requestPermissions(activity, permission);
			}
		}
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
