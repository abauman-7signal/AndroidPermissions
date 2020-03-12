package com.sevensignal.permissions.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import com.sevensignal.permissions.R;
import com.sevensignal.permissions.logging.LoggingConstants;
import com.sevensignal.permissions.utils.PermissionRequester;

public class PermissionClarificationDialog extends DialogFragment {
	private static final String CLASS_NAME = PermissionClarificationDialog.class.getSimpleName();
	private static String KEY_FOR_DIALOGS_TITLE_ARGUMENT = "KEY_FOR_DIALOGS_TITLE_ARGUMENT";
	private static String KEY_FOR_DIALOGS_MESSAGE_ARGUMENT = "KEY_FOR_DIALOGS_MESSAGE_ARGUMENT";
	private static String KEY_FOR_PERMISSION_REQUEST_CODE_ARGUMENT = "KEY_FOR_PERMISSION_REQUEST_CODE_ARGUMENT";

	public static PermissionClarificationDialog build(String titleForDialog, String messageForDialog, PermissionRequester.Permissions permissionBeingRequested) {
		PermissionClarificationDialog infoDialog = new PermissionClarificationDialog();
		Bundle arguments = new Bundle();
		arguments.putString(KEY_FOR_DIALOGS_TITLE_ARGUMENT, titleForDialog);
		arguments.putString(KEY_FOR_DIALOGS_MESSAGE_ARGUMENT, messageForDialog);
		arguments.putInt(KEY_FOR_PERMISSION_REQUEST_CODE_ARGUMENT, permissionBeingRequested.getRequestCode());
		infoDialog.setArguments(arguments);
		return infoDialog;
	}

	private String getDialogsTitle() {
		return getArguments().getString(KEY_FOR_DIALOGS_TITLE_ARGUMENT, getResources().getString(R.string.not_available));
	}

	private String getDialogsMessage() {
		return getArguments().getString(KEY_FOR_DIALOGS_MESSAGE_ARGUMENT, getResources().getString(R.string.not_available));
	}

	private PermissionRequester.Permissions getPermissionRequested() {
		return PermissionRequester.Permissions.getById(
				getArguments().getInt(KEY_FOR_PERMISSION_REQUEST_CODE_ARGUMENT));
	}

	@Override
	public AlertDialog onCreateDialog (Bundle savedInstanceState) {
		super.onCreateDialog (savedInstanceState);
		AlertDialog.Builder builder = new AlertDialog.Builder (getActivity());
		builder.setMessage (getDialogsMessage())
				.setTitle (getDialogsTitle())
				.setPositiveButton (R.string.ok_button_label, new DialogInterface.OnClickListener() {
					public void onClick (DialogInterface dialog, int id) {
						Log.d (LoggingConstants.PERM_APP, CLASS_NAME + " Clicked positive action button");
						PermissionRequester permissionRequester = PermissionRequester.getPermissionRequester();
						permissionRequester.finishRequestingPermission(getActivity(), getPermissionRequested());
					}
				});
		return builder.create();
	}

	@Override
	public void onAttach (Activity activity) {
		super.onAttach (activity);
		// Do not need any listeners for this dialog since it is only an informative modal dialog.
	}
}
