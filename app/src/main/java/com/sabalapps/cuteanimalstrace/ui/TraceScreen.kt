package com.sabalapps.cuteanimalstrace.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.DrawingTemplate

private tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

@Composable
fun TraceScreen(drawing: DrawingTemplate?) {
    val overlayState = rememberSaveable(drawing?.id, saver = TracingOverlayState.Saver) { TracingOverlayState() }
    val context = LocalContext.current
    val activity = context.activity()
    val lifecycleOwner = LocalLifecycleOwner.current
    var requested by rememberSaveable { mutableStateOf(false) }
    fun readPermission() = cameraPermissionState(
        granted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED,
        requested = requested,
        rationale = activity?.let { ActivityCompat.shouldShowRequestPermissionRationale(it, Manifest.permission.CAMERA) } == true,
    )
    var permission by remember { mutableStateOf(readPermission()) }
    var settingsFailed by remember { mutableStateOf(false) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        permission = readPermission()
    }
    val refreshPermission by rememberUpdatedState({ permission = readPermission() })
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) refreshPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(Modifier.fillMaxSize().testTag("screen_trace")) {
        Text(
            drawing?.let { stringResource(R.string.trace_selected, it.name) }
                ?: stringResource(R.string.drawing_unavailable),
            Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
            style = MaterialTheme.typography.bodyMedium,
        )
        if (permission == CameraPermissionState.Granted) {
            CameraPreview(Modifier.weight(1f)) {
                if (drawing != null) TracingOverlay(drawing, overlayState)
            }
            if (drawing != null) OverlayControls(overlayState)
        } else {
            Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState()).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)) {
                Text(stringResource(R.string.camera_permission_title), style = MaterialTheme.typography.headlineSmall)
                Text(stringResource(when (permission) {
                    CameraPermissionState.Blocked -> R.string.camera_permission_blocked
                    CameraPermissionState.Denied -> R.string.camera_permission_denied
                    else -> R.string.camera_permission_explanation
                }))
                if (permission == CameraPermissionState.Blocked) {
                    Button(onClick = {
                        settingsFailed = runCatching {
                            context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)))
                        }.isFailure
                    }, modifier = Modifier.testTag("camera_settings")) {
                        Text(stringResource(R.string.open_app_settings))
                    }
                    if (settingsFailed) Text(stringResource(R.string.camera_settings_failed))
                } else {
                    Button(onClick = {
                        requested = true
                        launcher.launch(Manifest.permission.CAMERA)
                    }, modifier = Modifier.testTag("allow_camera")) {
                        Text(stringResource(R.string.allow_camera))
                    }
                }
            }
        }
    }
}
