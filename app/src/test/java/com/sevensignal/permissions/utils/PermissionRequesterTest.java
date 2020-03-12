package com.sevensignal.permissions.utils;

import android.Manifest;
import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import com.sevensignal.permissions.shims.PermissionsAndroid;
import com.sevensignal.permissions.view.PermissionClarificationDialog;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static com.sevensignal.permissions.utils.PermissionRequester.Permissions.PERMISSION_TO_READ_ACCOUNT_INFO;
import static com.sevensignal.permissions.utils.PermissionRequester.Permissions.PERMISSION_TO_READ_LOCATION_INFO;
import static com.sevensignal.permissions.utils.PermissionRequester.Permissions.PERMISSION_TO_READ_PHONE_INFO;
import static junit.framework.Assert.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ContextCompat.class, ActivityCompat.class, PermissionClarificationDialog.class, PermissionsAndroid.class})
public class PermissionRequesterTest {

	private PermissionRequester subject;

	@Mock private Activity activityMocked;
	@Mock private Resources resourcesMocked;
	@Mock private FragmentManager fragmentManagerMocked;
	@Mock private PermissionsAndroid permissionsAndroidMocked;

	class PermissionClarificationDialogTester extends PermissionClarificationDialog {

		int numberOfTimesCalled_Show = 0;

		@Override
		public void show(FragmentManager fragmentManager, String tag) {
			numberOfTimesCalled_Show++;
		}
	}

	private void mockWhenRequestingPermissions(PermissionClarificationDialogTester permissionClarificationDialogTester) {
		PowerMockito.mockStatic(ContextCompat.class);
		when(permissionsAndroidMocked.checkPermission(any(Context.class), any(PermissionRequester.Permissions.class)))
				.thenReturn(false);
		when(activityMocked.getResources()).thenReturn(resourcesMocked);
		when(activityMocked.getFragmentManager()).thenReturn(fragmentManagerMocked);
		final String clarifyingMessage = "Clarifying message for this unit test";
		when(resourcesMocked.getString(anyInt()))
				.thenReturn(clarifyingMessage);
		PowerMockito.mockStatic(ActivityCompat.class);
		when(permissionsAndroidMocked.shouldShowRequestPermissionRationale(any(Activity.class), any(PermissionRequester.Permissions.class)))
				.thenReturn(true);
		PowerMockito.mockStatic(PermissionClarificationDialog.class);
		PowerMockito.when(PermissionClarificationDialog.build(anyString(), anyString(), any(PermissionRequester.Permissions.class)))
				.thenReturn(permissionClarificationDialogTester);
	}

	@Before
	public void setup() {
		PowerMockito.mockStatic(PermissionsAndroid.class);
		when(PermissionsAndroid.create()).thenReturn(permissionsAndroidMocked);
		subject = PermissionRequester.getPermissionRequester();
	}

	@Test
	public void shouldRequestPermissionsFromAndroid() {
		PermissionClarificationDialogTester permissionClarificationDialogTester = new PermissionClarificationDialogTester();

		mockWhenRequestingPermissions(permissionClarificationDialogTester);

		subject.requestPermissions(activityMocked, PERMISSION_TO_READ_ACCOUNT_INFO);
		assertEquals(1, permissionClarificationDialogTester.numberOfTimesCalled_Show);
	}

	@Test
	public void shouldFinishRequestPermissionsFromAndroid() {
		final String[] permissionsToRequest = {Manifest.permission.GET_ACCOUNTS};
		PowerMockito.mockStatic(ActivityCompat.class);

		subject.finishRequestPermissions(activityMocked, PERMISSION_TO_READ_ACCOUNT_INFO);

		verify(permissionsAndroidMocked, Mockito.times(1));
		permissionsAndroidMocked.requestPermissions(activityMocked, permissionsToRequest, PERMISSION_TO_READ_ACCOUNT_INFO.getRequestCode());
	}

	@Test
	public void shouldNotRequestPermissionsFromAndroid() {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(activityMocked, PERMISSION_TO_READ_ACCOUNT_INFO.getDescription()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);
		final String[] permissionsToRequest = {Manifest.permission.GET_ACCOUNTS};
		PowerMockito.mockStatic(ActivityCompat.class);

		subject.requestPermissions(activityMocked, PERMISSION_TO_READ_ACCOUNT_INFO);

		verify(permissionsAndroidMocked, Mockito.times(0));
		permissionsAndroidMocked.requestPermissions(activityMocked, permissionsToRequest, PERMISSION_TO_READ_ACCOUNT_INFO.getRequestCode());
	}

	@Test
	public void shouldGetPermissionFromId() {
		assertEquals("should get permission from an ID", PERMISSION_TO_READ_LOCATION_INFO, PermissionRequester.Permissions.getById(102));
	}

	@Test
	public void shouldGetInvalidPermissionWhenIdDoesNotExist() {
		assertEquals("should get invalid permission when ID does not exist", PermissionRequester.Permissions.PERMISSION_INVALID, PermissionRequester.Permissions.getById(-1));
	}

	@Test
	public void shouldKnowWhenAllRequiredPermissionsGranted() {
		PowerMockito.mockStatic(ContextCompat.class);
		when(permissionsAndroidMocked.checkPermission(any(Activity.class), any(PermissionRequester.Permissions.class)))
				.thenReturn(true);

		assertTrue("required permissions should indicate they are granted", subject.areAllRequiredPermissionsGranted(activityMocked));
	}

	@Test
	public void shouldKnowWhenOneRequiredPermissionNotGranted() {
		PowerMockito.mockStatic(ContextCompat.class);
		when(permissionsAndroidMocked.checkPermission(activityMocked, PERMISSION_TO_READ_ACCOUNT_INFO))
				.thenReturn(true);
		when(permissionsAndroidMocked.checkPermission(activityMocked, PERMISSION_TO_READ_LOCATION_INFO))
				.thenReturn(true);
		when(permissionsAndroidMocked.checkPermission(activityMocked, PERMISSION_TO_READ_PHONE_INFO))
				.thenReturn(false);

		assertFalse("required permissions should indicate they are not all granted", subject.areAllRequiredPermissionsGranted(activityMocked));
	}

	@Test
	public void shouldIndicatePermissionGranted() {
		int requestCode = 103;
		String[] permissions = {Manifest.permission.READ_PHONE_STATE};
		int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		assertTrue("should indicate permission granted", subject.isPermissionGranted(requestCode, permissions, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGranted() {
		int requestCode = 103;
		String[] permissions = {Manifest.permission.READ_PHONE_STATE};
		int[] grantResults = {PackageManager.PERMISSION_DENIED};
		assertFalse("should indicate permission not granted", subject.isPermissionGranted(requestCode, permissions, grantResults));
	}

	@Test
	public void shouldIndicateNotGrantedWhenRequestCodeInvalid() {
		assertFalse("should indicate permission not granted when request code invalid", subject.isPermissionGranted(-1, null, null));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenGrantResultsEmpty() {
		int requestCode = 103;
		String[] permissions = {};
		int[] grantResults = {};
		assertFalse("should indicate permission not granted when results empty", subject.isPermissionGranted(requestCode, permissions, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenRequestCodeDoesNotMatchPermissionsResults() {
		final int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		final String[] permissionsResults = {Manifest.permission.ACCESS_COARSE_LOCATION};
		assertFalse("should indicate permission not granted when request code does not match permission results", subject.isPermissionGranted(103, permissionsResults, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenPermissionResultsAreNull() {
		final int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		assertFalse("should indicate permission not granted when permission results are null", subject.isPermissionGranted(103, null, grantResults));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenGrantResultsAreNull() {
		final String[] permissionsResults = {Manifest.permission.ACCESS_COARSE_LOCATION};
		assertFalse("should indicate permission not granted when grant results are null", subject.isPermissionGranted(103, permissionsResults, null));
	}

	@Test
	public void shouldIndicatePermissionNotGrantedWhenSizeOfPermissionResultsAndSizeOfGrantResultsDoNotMatch() {
		final int[] grantResults = {PackageManager.PERMISSION_GRANTED};
		final String[] permissionsResults = {Manifest.permission.READ_PHONE_STATE, Manifest.permission.ACCESS_COARSE_LOCATION};
		assertFalse("should indicate permission not granted when size of permission results does not match size of grant results", subject.isPermissionGranted(103, permissionsResults, grantResults));
	}

	@Test
	public void shouldRequestAllPermissions() {
		PermissionClarificationDialogTester permissionClarificationDialogTester = new PermissionClarificationDialogTester();

		mockWhenRequestingPermissions(permissionClarificationDialogTester);

		subject.requestAllPermissions(activityMocked);

		assertEquals(3, permissionClarificationDialogTester.numberOfTimesCalled_Show);
	}

	@Test
	public void shouldNotRequestAllPermissions() {
		String[] permissionsToRequest = {
				PERMISSION_TO_READ_ACCOUNT_INFO.getDescription(),
				PERMISSION_TO_READ_LOCATION_INFO.getDescription(),
				PERMISSION_TO_READ_PHONE_INFO.getDescription()
		};

		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.when(ContextCompat.checkSelfPermission(any(Context.class), anyString()))
				.thenReturn(PackageManager.PERMISSION_GRANTED);
		PowerMockito.mockStatic(ActivityCompat.class);
		subject.requestAllPermissions(activityMocked);

		PowerMockito.verifyStatic(ActivityCompat.class, Mockito.times(0));
		ActivityCompat.requestPermissions(activityMocked, permissionsToRequest, PermissionRequester.REQUEST_CODE_FOR_ALL_PERMISSIONS);
	}

	@Test
	public void whenRequestingAllPermissions_shouldNotBeDoneRequestingPermissions() {
		PermissionRequestJob permissionRequestJob = subject.beginRequestingAllPermissions();
		assertFalse(permissionRequestJob.hasAllPermissionsBeenRequested());
	}

	@Test(expected = IllegalArgumentException.class)
	public void whenRequestingNextPermission_shouldRequireValidArgument() {
		subject.getNextPermissionToRequest(activityMocked, null);
	}

	@Test
	public void shouldRequireCorrectAmountOfTimesToRequestNextPermission_TypicalUseCase() {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.mockStatic(ActivityCompat.class);

		mockCheckSelfPerm(false);
		mockShouldShowRequestPermRationale(true);

		PermissionRequestJob permReqJob = subject.beginRequestingAllPermissions();
		assertFalse(permReqJob.hasAllPermissionsBeenRequested());

		assertRequestingNextPerm(permReqJob, PERMISSION_TO_READ_ACCOUNT_INFO, false);
		assertRequestingNextPerm(permReqJob, PERMISSION_TO_READ_LOCATION_INFO, false);
		assertRequestingNextPerm(permReqJob, PERMISSION_TO_READ_PHONE_INFO, false);
		assertRequestingNextPerm(permReqJob, null, true);
	}

	@Test
	public void shouldRequireCorrectAmountOfTimesToRequestNextPermission_AllAlreadyGranted() {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.mockStatic(ActivityCompat.class);

		mockCheckSelfPerm(true);
		mockShouldShowRequestPermRationale(true);

		PermissionRequestJob permReqJob = subject.beginRequestingAllPermissions();
		assertFalse(permReqJob.hasAllPermissionsBeenRequested());

		assertRequestingNextPerm(permReqJob, null, true);
	}

	@Test
	public void shouldRequireCorrectAmountOfTimesToRequestNextPermission_AllAlreadyDeniedAndShouldNotRequest() {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.mockStatic(ActivityCompat.class);

		mockCheckSelfPerm(false);
		mockShouldShowRequestPermRationale(false);

		PermissionRequestJob permReqJob = subject.beginRequestingAllPermissions();
		assertFalse(permReqJob.hasAllPermissionsBeenRequested());

		assertRequestingNextPerm(permReqJob, null, true);
	}

	@Test
	public void shouldRequestNextPermission_whenInitialState() {
		PowerMockito.mockStatic(ContextCompat.class);
		PowerMockito.mockStatic(ActivityCompat.class);

		mockCheckSelfPerm(false);
		mockShouldShowRequestPermRationale(false);

		PermissionRequestJob permReqJob = subject.beginRequestingAllPermissions();
		assertFalse(permReqJob.hasAllPermissionsBeenRequested());

		assertRequestingNextPerm(permReqJob, null, true);
	}

	private void assertRequestingNextPerm(PermissionRequestJob permReqJob, PermissionRequester.Permissions permToRequest, boolean hasAllPermsBeenRequested) {
		assertEquals(permToRequest, subject.getNextPermissionToRequest(activityMocked, permReqJob));
		assertEquals(hasAllPermsBeenRequested, permReqJob.hasAllPermissionsBeenRequested());
	}

	private void mockCheckSelfPerm(boolean isPermGranted) {
		when(permissionsAndroidMocked.checkPermission(any(Context.class), any(PermissionRequester.Permissions.class)))
				.thenReturn(isPermGranted);
	}

	private void mockShouldShowRequestPermRationale(boolean shouldShow) {
		when(permissionsAndroidMocked.shouldShowRequestPermissionRationale(any(Activity.class), any(PermissionRequester.Permissions.class)))
				.thenReturn(shouldShow);
	}
}