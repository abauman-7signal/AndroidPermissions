package com.sevensignal.permissions.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import lombok.Getter;

public class PermissionRequestJob {

	// TODO -- convert to set to disallow duplicates
	@Getter
	private List<PermissionRequest> permissionsToRequest;

	private PermissionRequestJob() {}

	public PermissionRequestJob(final List<PermissionRequester.Permissions> permissions) {
		if (permissions == null || permissions.isEmpty()) {
			throw new IllegalArgumentException("invalid value for list of permissions");
		}
		ArrayList<PermissionRequest> permissionsToRequest = new ArrayList<>();
		for (PermissionRequester.Permissions permission : permissions) {
			permissionsToRequest.add(new PermissionRequest(permission, PermissionRequestingStates.WAITING));
		}
		this.permissionsToRequest = Collections.unmodifiableList(permissionsToRequest);
	}

	public boolean hasAllPermissionsBeenRequested() {
		boolean result = true;
		for (PermissionRequest permissionToRequest : permissionsToRequest) {
			result = result & (permissionToRequest.getRequestingState() == PermissionRequestingStates.REQUESTED);
		}
		return result;
	}
}
