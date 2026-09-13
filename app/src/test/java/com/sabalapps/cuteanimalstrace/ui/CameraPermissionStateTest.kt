package com.sabalapps.cuteanimalstrace.ui

import org.junit.Assert.assertEquals
import org.junit.Test

class CameraPermissionStateTest {
    @Test fun firstRequest_isNotMistakenForPermanentDenial() {
        assertEquals(CameraPermissionState.NotRequested, cameraPermissionState(false, false, false))
    }
    @Test fun deniedButRequestable_showsRetry() {
        assertEquals(CameraPermissionState.Denied, cameraPermissionState(false, true, true))
    }
    @Test fun deniedWithoutRationale_afterRequest_showsSettings() {
        assertEquals(CameraPermissionState.Blocked, cameraPermissionState(false, true, false))
    }
    @Test fun grantFromSettings_overridesDenialHistory() {
        assertEquals(CameraPermissionState.Granted, cameraPermissionState(true, true, false))
    }
}
