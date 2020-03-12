package com.sevensignal.permissions;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.sevensignal.permissions.logging.LoggingConstants;
import com.sevensignal.permissions.utils.PermissionRequestJob;
import com.sevensignal.permissions.utils.PermissionRequester;
import com.sevensignal.permissions.view.PermissionClarificationDialog;

public class MainActivity extends AppCompatActivity {

	private static final String CLASS_NAME = MainActivity.class.getSimpleName();
	private static final String PERM_REQUEST_JOB_KEY = "PERM_REQUEST_JOB_KEY";
	private static final String PERM_REQUEST_IN_PROGRESS_KEY = "PERM_REQUEST_IN_PROGRESS_KEY";
	final static String PERMISSION_REQUESTER_DIALOG_TAG = "permissionRequesterDialogTag";
	private PermissionRequestJob permRequestJob;
	private boolean isPermRequestInProgress = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.v(LoggingConstants.PERM_UI, CLASS_NAME + " onCreate");
		Log.d(LoggingConstants.PERM_UI, "Restoring permRequestJob...");
		restorePermRequestState(savedInstanceState);
		setContentView(R.layout.activity_main);
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.v(LoggingConstants.PERM_UI, CLASS_NAME + " onStart");

		if (!isPermClarificationDialogFound() && !isPermRequestInProgress) {
			isPermRequestInProgress = beginRequestingPermissions();
			if (isPermRequestInProgress) {
				appendMessage("Requesting user to grant permissions...");
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(LoggingConstants.PERM_UI, CLASS_NAME + " onResume");
	}


	@Override
	protected void onSaveInstanceState(Bundle instanceState) {
		super.onSaveInstanceState(instanceState);
		Log.v(LoggingConstants.PERM_UI, CLASS_NAME + " onSaveInstanceState");
		savePermRequestState(instanceState);
	}

	private void savePermRequestState(Bundle instanceState) {
		if (permRequestJob != null) {
			Log.d(LoggingConstants.PERM_UI, "Saving permission request job state: " + permRequestJob);
			instanceState.putSerializable(PERM_REQUEST_JOB_KEY, permRequestJob);
		}
		Log.d(LoggingConstants.PERM_UI, "Saving permission request in progress state: " + isPermRequestInProgress);
		instanceState.putSerializable(PERM_REQUEST_IN_PROGRESS_KEY, isPermRequestInProgress);
	}

	private void restorePermRequestState(Bundle savedInstanceState) {
		if (savedInstanceState != null) {
			permRequestJob = (PermissionRequestJob) savedInstanceState.getSerializable(PERM_REQUEST_JOB_KEY);
			Log.d(LoggingConstants.PERM_UI, "Restored permission request job state: " + permRequestJob);
			isPermRequestInProgress = (boolean)savedInstanceState.getSerializable(PERM_REQUEST_IN_PROGRESS_KEY);
			Log.d(LoggingConstants.PERM_UI, "Restored permission request in progress state: " + isPermRequestInProgress);
		}
	}

	private void appendMessage(String message) {
		final TextView msgView = findViewById(R.id.message_text_view);
		if (msgView != null) {
			msgView.append(message + System.lineSeparator());
		}
	};

	@Override
	protected void onStop() {
		super.onStop();
		Log.v(LoggingConstants.PERM_UI, CLASS_NAME + " onStop");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy(); // allow base classes to clean up resources also
		Log.v(LoggingConstants.PERM_UI, CLASS_NAME + " onDestroy()");
	}

	private boolean beginRequestingPermissions() {
		final PermissionRequester permissionRequester = PermissionRequester.getPermissionRequester();
		if (!permissionRequester.areAllRequiredPermissionsGranted(this)) {
			if (permRequestJob == null) {
				Log.d(LoggingConstants.PERM_UI, "Initializing permRequestJob...");
				permRequestJob = permissionRequester.beginRequestingAllPermissions();
			}
			return requestNextPermission(permRequestJob);
		}
		return false;
	}

	private boolean isPermClarificationDialogFound() {
		PermissionClarificationDialog permClarificationDialog = (PermissionClarificationDialog)getFragmentManager().findFragmentByTag(PERMISSION_REQUESTER_DIALOG_TAG);
		if (permClarificationDialog != null) {
			Log.d(LoggingConstants.PERM_UI, "Found permission clarification dialog");
			return true;
		}
		Log.d(LoggingConstants.PERM_UI, "Could not find Permission clarification dialog");
		return false;
	}

	private void showRequestPermissionRationale(final Activity activity, final PermissionRequester.Permissions permission) {
		final String msg = "Requesting permission: " + permission.getDescription();
		appendMessage(msg);
		Log.d(LoggingConstants.PERM_UI, msg);
		PermissionClarificationDialog.build(
				"",
				activity.getResources().getString(permission.getReasonId()),
				permission)
				.show(activity.getFragmentManager(), PERMISSION_REQUESTER_DIALOG_TAG);
	}

	private boolean requestNextPermission(PermissionRequestJob permissionRequestJob) {
		final PermissionRequester permissionRequester = PermissionRequester.getPermissionRequester();
		if (permissionRequestJob != null) {
			final PermissionRequester.Permissions nextPermToRequest = permissionRequester.getNextPermissionToRequest(this, permissionRequestJob);
			if (nextPermToRequest != null) {
				showRequestPermissionRationale(this, nextPermToRequest);
				return true;
			}
		}
		return false;
	}

	@Override
	public void onRequestPermissionsResult(final int requestCode, final String[] permissions, final int[] grantResults) {
		Log.d(LoggingConstants.PERM_UI, "received onRequestPermissionsResult");

		if (!requestNextPermission(permRequestJob)) {
			String msg = "Finished requesting all permissions";
			Log.d(LoggingConstants.PERM_UI, msg);
			appendMessage(msg);
			isPermRequestInProgress = false;
			PermissionRequester permissionRequester = PermissionRequester.getPermissionRequester();
			if (!permissionRequester.areAllRequiredPermissionsGranted(this)) {
				msg = "Not all permissions granted";
				Log.w(LoggingConstants.PERM_UI, msg);
				appendMessage(msg);
			} else {
				msg = "All permissions granted";
				Log.d(LoggingConstants.PERM_UI, msg);
				appendMessage(msg);
			}
			permRequestJob = null;
		}
	}

}
