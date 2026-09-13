package com.sabalapps.cuteanimalstrace.ui

internal enum class CameraPermissionState { Granted, NotRequested, Denied, Blocked }

// shouldShowRationale is also false before the first request, so track the request separately.
internal fun cameraPermissionState(granted: Boolean, requested: Boolean, rationale: Boolean) = when {
    granted -> CameraPermissionState.Granted
    !requested -> CameraPermissionState.NotRequested
    rationale -> CameraPermissionState.Denied
    else -> CameraPermissionState.Blocked
}
