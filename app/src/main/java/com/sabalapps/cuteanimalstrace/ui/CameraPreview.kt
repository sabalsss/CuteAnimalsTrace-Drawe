package com.sabalapps.cuteanimalstrace.ui

import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.CameraState
import androidx.camera.core.Preview
import androidx.camera.core.TorchState
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
internal fun CameraPreview(modifier: Modifier = Modifier) {
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
    Column(modifier.fillMaxWidth()) {
        Box(Modifier.fillMaxWidth().weight(1f).testTag("camera_preview").semantics { stateDescription = previewStatus }) {
            AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
            if (!error) {
                Box(Modifier.matchParentSize().padding(28.dp)
                    .border(1.dp, Color.White.copy(alpha = 0.65f), MaterialTheme.shapes.large),
                    contentAlignment = Alignment.BottomCenter) {
                    Surface(color = Color.Black.copy(alpha = 0.65f), contentColor = Color.White,
                        shape = MaterialTheme.shapes.small, modifier = Modifier.padding(12.dp)) {
                        Text(stringResource(R.string.overlay_placeholder), Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
                if (!streaming) CircularProgressIndicator(Modifier.align(Alignment.Center))
            } else {
                Surface(Modifier.fillMaxSize()) {
                    Column(Modifier.verticalScroll(rememberScrollState()).padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Text(stringResource(R.string.camera_unavailable), style = MaterialTheme.typography.titleLarge)
                        Text(stringResource(R.string.camera_retry_help))
                        Button(onClick = { attempt += 1 }, modifier = Modifier.testTag("retry_camera")) {
                            Text(stringResource(R.string.retry_camera))
                        }
                    }
                }
            }
        }
        Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally) {
            val currentCamera = camera
            if (currentCamera?.cameraInfo?.hasFlashUnit() == true) {
                Button(enabled = ready && !error && !torchPending,
                    modifier = Modifier.testTag("flashlight"),
                    onClick = {
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
                    }) {
                    Text(stringResource(if (torchOn) R.string.flashlight_off else R.string.flashlight_on))
                }
                if (torchError) Text(stringResource(R.string.flashlight_failed))
            } else if (currentCamera != null) {
                Text(stringResource(R.string.flashlight_unsupported), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
