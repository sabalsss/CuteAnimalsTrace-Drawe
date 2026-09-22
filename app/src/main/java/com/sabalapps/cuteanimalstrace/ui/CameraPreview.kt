package com.sabalapps.cuteanimalstrace.ui

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.Observer
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sabalapps.cuteanimalstrace.R

@Composable
internal fun CameraPreview(modifier: Modifier = Modifier, overlay: @Composable () -> Unit = {}) {
    val context = LocalContext.current
    val owner = LocalLifecycleOwner.current
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    val previewView = remember(context) {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    var attempt by remember { mutableIntStateOf(0) }
    var camera by remember { mutableStateOf<Camera?>(null) }
    var error by remember { mutableStateOf(false) }
    var streaming by remember { mutableStateOf(false) }
    var ready by remember { mutableStateOf(false) }
    var torchOn by remember { mutableStateOf(false) }
    var torchPending by remember { mutableStateOf(false) }
    var torchError by remember { mutableStateOf(false) }
    // Incremented on each binding/disposal to ignore completions from a former camera session.
    var session by remember { mutableIntStateOf(0) }

    DisposableEffect(owner, previewView, attempt) {
        session += 1
        var disposed = false
        var provider: ProcessCameraProvider? = null
        var boundCamera: Camera? = null
        val preview = Preview.Builder().build()
        camera = null
        ready = false
        streaming = false
        error = false
        torchOn = false
        torchPending = false
        torchError = false
        val streamObserver = Observer<PreviewView.StreamState> { streaming = it == PreviewView.StreamState.STREAMING }
        previewView.previewStreamState.observe(owner, streamObserver)
        val torchObserver = Observer<Int> { torchOn = it == TorchState.ON }
        val stateObserver = Observer<CameraState> {
            ready = it.type == CameraState.Type.OPEN
            error = it.error != null
        }
        val stopObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                streaming = false
                boundCamera?.cameraControl?.enableTorch(false)
            }
        }
        owner.lifecycle.addObserver(stopObserver)
        val future = ProcessCameraProvider.getInstance(context)
        future.addListener({
            if (!disposed) {
                try {
                    val currentProvider = future.get()
                    provider = currentProvider
                    if (!currentProvider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA)) {
                        error = true
                    } else {
                        preview.setSurfaceProvider(previewView.surfaceProvider)
                        val currentCamera = currentProvider.bindToLifecycle(owner, CameraSelector.DEFAULT_BACK_CAMERA, preview)
                        boundCamera = currentCamera
                        camera = currentCamera
                        currentCamera.cameraInfo.torchState.observe(owner, torchObserver)
                        currentCamera.cameraInfo.cameraState.observe(owner, stateObserver)
                    }
                } catch (_: Exception) {
                    error = true
                }
            }
        }, executor)
        onDispose {
            disposed = true
            session += 1
            owner.lifecycle.removeObserver(stopObserver)
            previewView.previewStreamState.removeObserver(streamObserver)
            boundCamera?.let {
                it.cameraInfo.torchState.removeObserver(torchObserver)
                it.cameraInfo.cameraState.removeObserver(stateObserver)
                it.cameraControl.enableTorch(false)
            }
            // Unbind only this screen's use case; never disturb another camera owner.
            provider?.unbind(preview)
            preview.setSurfaceProvider(null)
        }
    }

    val previewStatus = stringResource(if (streaming) R.string.camera_live else R.string.camera_starting)
    val torchStatus = stringResource(if (torchOn) R.string.flashlight_enabled else R.string.flashlight_disabled)
    Box(
        modifier.fillMaxSize().testTag("camera_preview")
            .semantics { stateDescription = previewStatus },
    ) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        if (!error) {
            overlay()
            if (!streaming) CircularProgressIndicator(Modifier.align(Alignment.Center))
            val currentCamera = camera
            if (currentCamera?.cameraInfo?.hasFlashUnit() == true) {
                // Floating, translucent, and clear of the traced area in the middle.
                Surface(
                    shape = CircleShape,
                    color = if (torchOn) MaterialTheme.colorScheme.primary.copy(alpha = 0.92f)
                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
                    contentColor = if (torchOn) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface,
                    shadowElevation = 4.dp,
                    modifier = Modifier.align(Alignment.TopEnd).padding(12.dp).size(48.dp),
                ) {
                    IconToggleButton(
                        checked = torchOn,
                        enabled = ready && !error && !torchPending,
                        modifier = Modifier.fillMaxSize().testTag("flashlight")
                            .semantics { stateDescription = torchStatus },
                        onCheckedChange = {
                            torchPending = true
                            torchError = false
                            val activeSession = session
                            val future = currentCamera.cameraControl.enableTorch(!torchOn)
                            future.addListener({
                                if (session == activeSession) {
                                    torchPending = false
                                    torchError = runCatching { future.get() }.isFailure
                                }
                            }, executor)
                        },
                    ) {
                        Icon(
                            AppIcons.flashlight,
                            stringResource(if (torchOn) R.string.flashlight_off else R.string.flashlight_on),
                            Modifier.size(22.dp),
                        )
                    }
                }
                if (torchError) {
                    Surface(
                        shape = MaterialTheme.shapes.small,
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier.align(Alignment.TopEnd).padding(top = 68.dp, end = 12.dp),
                    ) {
                        Text(stringResource(R.string.flashlight_failed),
                            Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }
        } else {
            Surface(Modifier.fillMaxSize()) {
                Column(
                    Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    Text(stringResource(R.string.camera_unavailable),
                        style = MaterialTheme.typography.titleLarge)
                    Text(stringResource(R.string.camera_retry_help),
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Button(onClick = { attempt += 1 }, shape = MaterialTheme.shapes.large,
                        modifier = Modifier.heightIn(min = 50.dp).testTag("retry_camera")) {
                        Text(stringResource(R.string.retry_camera))
                    }
                }
            }
        }
    }
}
