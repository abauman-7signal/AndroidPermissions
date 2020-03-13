package com.sevensignal.permissions.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class PermissionRequesterOrig {

    public static final int REQUEST_CODE_FOR_ALL_PERMISSIONS = 999;

	public enum Permissions {
		PERMISSION_TO_READ_ACCOUNT_INFO(101, Manifest.permission.GET_ACCOUNTS),
		PERMISSION_TO_READ_LOCATION_INFO(102, Manifest.permission.ACCESS_COARSE_LOCATION),
		PERMISSION_TO_READ_PHONE_INFO(103, Manifest.permission.READ_PHONE_STATE),
		PERMISSION_INVALID(999, "android.permission.invalid");

		@Getter private final int requestCode;
		@Getter private final String description;

		Permissions(int requestCode, String description) {
			this.requestCode = requestCode;
			this.description = description;
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

	boolean checkPermission(Context context, Permissions permission) {
		return (ContextCompat.checkSelfPermission(context, permission.getDescription()) == PackageManager.PERMISSION_GRANTED);
	}

	public boolean areAllRequiredPermissionsGranted(final Context context) {
		boolean permissionsGranted = true;
		for (Permissions permission : Permissions.values()) {
			if (permission != Permissions.PERMISSION_INVALID) {
				permissionsGranted = permissionsGranted && checkPermission(context, permission);
			}
		}
		return permissionsGranted;
	}

	public void requestPermissions(Activity activity, Permissions permission) {
		if (!checkPermission(activity, permission)) {
			String[] permissions = {permission.getDescription()};
			ActivityCompat.requestPermissions(activity, permissions, permission.getRequestCode());
		}
	}

	public void requestAllPermissions(Activity activity) {
	    if (!areAllRequiredPermissionsGranted(activity)) {
            String[] permissions = {
                    Permissions.PERMISSION_TO_READ_ACCOUNT_INFO.getDescription(),
                    Permissions.PERMISSION_TO_READ_LOCATION_INFO.getDescription(),
                    Permissions.PERMISSION_TO_READ_PHONE_INFO.getDescription()
            };
            ActivityCompat.requestPermissions(activity, permissions, REQUEST_CODE_FOR_ALL_PERMISSIONS);
        }
	}

	public boolean isPermissionGranted(final int requestCode, final String[] permissions, final int[] grantResults) {
		final Permissions requestedPermission = Permissions.getById(requestCode);
		if (requestedPermission == Permissions.PERMISSION_INVALID) {
			return false;
		} else {
			if (grantResults.length > 0) {
				for (int i = 0 ; i < grantResults.length ; i++) {
					if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
						if (permissions[i].equals(requestedPermission.getDescription())) {
							return true;
						} else {
							return false;
						}
					}
				}
				return false;
			} else {
				return false;
			}
		}
	}
}
