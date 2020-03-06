package com.sevensignal.permissions.utils;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.sevensignal.permissions.view.PermissionClarificationDialog;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContextCompat.class, ActivityCompat.class, PermissionClarificationDialog.class})
public class PermissionRequesterTest {

	private PermissionRequester subject;

	@Mock private Activity activityMocked;
	@Mock private Resources resourcesMocked;
	@Mock private FragmentManager fragmentManagerMocked;

	class PermissionClarificationDialogTester extends PermissionClarificationDialog {

		int numberOfTimesCalled_Show = 0;

		@Override
		public void show(FragmentManager fragmentManager, String tag) {
			numberOfTimesCalled_Show++;
		}
	}

	private void mockWhenRequestingPermissions(PermissionClarificationDialogTester permissionClarificationDialogTester) {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(any(Context.class), anyString()))
				.thenReturn(PackageManager.PERMISSION_DENIED);
		when(activityMocked.getResources()).thenReturn(resourcesMocked);
		when(activityMocked.getFragmentManager()).thenReturn(fragmentManagerMocked);
		final String clarifyingMessage = "Clarifying message for this unit test";
		when(resourcesMocked.getString(anyInt()))
				.thenReturn(clarifyingMessage);
		PowerMockito.mockStatic(ActivityCompat.class);
		PowerMockito.when(ActivityCompat.shouldShowRequestPermissionRationale(any(Activity.class), anyString()))
				.thenReturn(true);
		PowerMockito.mockStatic(PermissionClarificationDialog.class);
		PowerMockito.when(PermissionClarificationDialog.build(anyString(), anyString(), any(PermissionRequester.Permissions.class)))
				.thenReturn(permissionClarificationDialogTester);
	}

	@Test
	public void shouldRequestPermissionsFromAndroid() {
		PermissionClarificationDialogTester permissionClarificationDialogTester = new PermissionClarificationDialogTester();

		mockWhenRequestingPermissions(permissionClarificationDialogTester);

		subject = PermissionRequester.getPermissionRequester();
		subject.requestPermissions(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO);
		assertEquals(1, permissionClarificationDialogTester.numberOfTimesCalled_Show);
	}

	@Test
	public void shouldFinishRequestPermissionsFromAndroid() {
		final String[] permissionsToRequest = {Manifest.permission.GET_ACCOUNTS};
		PowerMockito.mockStatic(ActivityCompat.class);

		subject = PermissionRequester.getPermissionRequester();
		subject.finishRequestPermissions(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO);

		PowerMockito.verifyStatic(ActivityCompat.class, Mockito.times(1));
		ActivityCompat.requestPermissions(activityMocked, permissionsToRequest, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO.getRequestCode());
	}

	@Test
	public void shouldNotRequestPermissionsFromAndroid() {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO.getDescription()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);
		final String[] permissionsToRequest = {Manifest.permission.GET_ACCOUNTS};
		PowerMockito.mockStatic(ActivityCompat.class);

		subject = PermissionRequester.getPermissionRequester();
		subject.requestPermissions(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO);

		PowerMockito.verifyStatic(ActivityCompat.class, Mockito.times(0));
		ActivityCompat.requestPermissions(activityMocked, permissionsToRequest, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO.getRequestCode());
	}

	@Test
	public void shouldGetPermissionFromId() {
		assertEquals("should get permission from an ID", PermissionRequester.Permissions.PERMISSION_TO_READ_LOCATION_INFO, PermissionRequester.Permissions.getById(102));
	}

	@Test
	public void shouldGetInvalidPermissionWhenIdDoesNotExist() {
		assertEquals("should get invalid permission when ID does not exist", PermissionRequester.Permissions.PERMISSION_INVALID, PermissionRequester.Permissions.getById(-1));
	}

	@Test
	public void shouldKnowWhenAllRequiredPermissionsGranted() {
		subject = PermissionRequester.getPermissionRequester();
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(any(Activity.class), anyString()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);

		assertTrue("required permissions should indicate they are granted", subject.areAllRequiredPermissionsGranted(activityMocked));
	}

	@Test
	public void shouldKnowWhenOneRequiredPermissionNotGranted() {
		subject = PermissionRequester.getPermissionRequester();
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO.getDescription()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);
		PowerMockito.when(ContextCompat.checkSelfPermission(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_LOCATION_INFO.getDescription()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);
		PowerMockito.when(ContextCompat.checkSelfPermission(activityMocked, PermissionRequester.Permissions.PERMISSION_TO_READ_PHONE_INFO.getDescription()))
				.thenReturn(PackageManager.PERMISSION_DENIED);

		assertFalse("required permissions should indicate they are not all granted", subject.areAllRequiredPermissionsGranted(activityMocked));
	}

	@Test
	public void shouldIndicatePermissionGranted() {
		int requestCode = 103;
		String[] permissions = {Manifest.permission.READ_PHONE_STATE};
		int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		subject = PermissionRequester.getPermissionRequester();
		assertTrue("should indicate permission granted", subject.isPermissionGranted(requestCode, permissions, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGranted() {
		int requestCode = 103;
		String[] permissions = {Manifest.permission.READ_PHONE_STATE};
		int[] grantResults = {PackageManager.PERMISSION_DENIED};
		subject = PermissionRequester.getPermissionRequester();
		assertFalse("should indicate permission not granted", subject.isPermissionGranted(requestCode, permissions, grantResults));
	}

	@Test
	public void shouldIndicateNotGrantedWhenRequestCodeInvalid() {
		subject = PermissionRequester.getPermissionRequester();
		assertFalse("should indicate permission not granted when request code invalid", subject.isPermissionGranted(-1, null, null));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenGrantResultsEmpty() {
		int requestCode = 103;
		String[] permissions = {};
		int[] grantResults = {};
		subject = PermissionRequester.getPermissionRequester();
		assertFalse("should indicate permission not granted when results empty", subject.isPermissionGranted(requestCode, permissions, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenRequestCodeDoesNotMatchPermissionsResults() {
		subject = PermissionRequester.getPermissionRequester();
		final int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		final String[] permissionsResults = {Manifest.permission.ACCESS_COARSE_LOCATION};
		assertFalse("should indicate permission not granted when request code does not match permission results", subject.isPermissionGranted(103, permissionsResults, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenPermissionResultsAreNull() {
		subject = PermissionRequester.getPermissionRequester();
		final int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		assertFalse("should indicate permission not granted when permission results are null", subject.isPermissionGranted(103, null, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenGrantResultsAreNull() {
		subject = PermissionRequester.getPermissionRequester();
		final String[] permissionsResults = {Manifest.permission.ACCESS_COARSE_LOCATION};
		assertFalse("should indicate permission not granted when grant results are null", subject.isPermissionGranted(103, permissionsResults, null));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenSizeOfPermissionResultsAndSizeOfGrantResultsDoNotMatch() {
		subject = PermissionRequester.getPermissionRequester();
		final int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		final String[] permissionsResults = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION};
		assertFalse("should indicate permission not granted when size of permission results does not match size of grant results", subject.isPermissionGranted(103, permissionsResults, grantResults));
	}

	@Test
	public void shouldRequestAllPermissions() {
		PermissionClarificationDialogTester permissionClarificationDialogTester = new PermissionClarificationDialogTester();

		mockWhenRequestingPermissions(permissionClarificationDialogTester);

		subject = PermissionRequester.getPermissionRequester();
		subject.requestAllPermissions(activityMocked);

		assertEquals(3, permissionClarificationDialogTester.numberOfTimesCalled_Show);
	}

	@Test
	public void shouldNotRequestAllPermissions() {
		String[] permissionsToRequest = {
				PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO.getDescription(),
				PermissionRequester.Permissions.PERMISSION_TO_READ_LOCATION_INFO.getDescription(),
				PermissionRequester.Permissions.PERMISSION_TO_READ_PHONE_INFO.getDescription()
		};

		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(any(Context.class), anyString()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);
		PowerMockito.mockStatic(ActivityCompat.class);
		subject = PermissionRequester.getPermissionRequester();
		subject.requestAllPermissions(activityMocked);

		PowerMockito.verifyStatic(ActivityCompat.class, Mockito.times(0));
		ActivityCompat.requestPermissions(activityMocked, permissionsToRequest, PermissionRequester.REQUEST_CODE_FOR_ALL_PERMISSIONS);
	}
}