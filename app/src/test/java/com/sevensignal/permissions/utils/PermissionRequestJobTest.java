package com.sevensignal.permissions.utils;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertFalse;

public class PermissionRequestJobTest {
	private PermissionRequestJob subject;

	@Test(expected = IllegalArgumentException.class)
	public void whenNullListOfPermissionsRequested_shouldThrowRuntimeException() throws RuntimeException {
		subject = new PermissionRequestJob(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenEmptyListOfPermissionsRequested_shouldThrowRuntimeException() throws RuntimeException {
		subject = new PermissionRequestJob(Collections.EMPTY_LIST);
	}

	@Test
	public void whenCreated_shouldNotHaveAllPermissionsRequested() {
		List<PermissionRequester.Permissions> permissions = new ArrayList<PermissionRequester.Permissions>() {{
			add(PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO);
		}};
		subject = new PermissionRequestJob(permissions);
		assertFalse(subject.hasAllPermissionsBeenRequested());
	}
}
