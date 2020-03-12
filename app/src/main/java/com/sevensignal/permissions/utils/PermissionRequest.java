package com.sevensignal.permissions.utils;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class PermissionRequest implements Serializable {
	@Getter
	private PermissionRequester.Permissions permission;
	@Getter @Setter
	private PermissionRequestingStates requestingState;

	PermissionRequest(PermissionRequester.Permissions permission, PermissionRequestingStates requestingState) {
		this.permission = permission;
		this.requestingState = requestingState;
	}
}
