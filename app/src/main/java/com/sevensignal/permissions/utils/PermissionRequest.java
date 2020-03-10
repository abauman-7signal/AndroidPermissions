package com.sevensignal.permissions.utils;

import lombok.Getter;
import lombok.Setter;

public class PermissionRequest {
	@Getter
	private PermissionRequester.Permissions permission;
	@Getter @Setter
	private PermissionRequestingStates requestingState;

	PermissionRequest(PermissionRequester.Permissions permission, PermissionRequestingStates requestingState) {
		this.permission = permission;
		this.requestingState = requestingState;
	}
}
