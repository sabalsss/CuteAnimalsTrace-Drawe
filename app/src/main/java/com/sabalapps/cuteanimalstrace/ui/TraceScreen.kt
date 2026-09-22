package com.sabalapps.cuteanimalstrace.ui

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.ui.Alignment
import kotlinx.coroutines.CancellationException
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.LocalConfiguration
import com.sabalapps.cuteanimalstrace.data.UserPreferences
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

/** How long the overlay must stay live before the session counts as a real trace. */
private const val TraceSuccessDelayMillis = 4000L

internal tailrec fun Context.activity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.activity()
    else -> null
}

/** Back control drawn over the camera; the shell hides its app bar on this route. */
@Composable
private fun FloatingBack(onBack: () -> Unit) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 4.dp,
        modifier = Modifier.size(48.dp),
    ) {
        IconButton(onClick = onBack, modifier = Modifier.fillMaxSize()) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.back),
                Modifier.size(22.dp))
        }
    }
}

@Composable
private fun TraceTitle(text: String, modifier: Modifier = Modifier) {
    Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = modifier,
    ) {
        Text(text, Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
            style = MaterialTheme.typography.labelLarge, maxLines = 1)
    }
}

/** Lightweight, dismissible first-use guidance; the Settings toggle suppresses it for good. */
@Composable
private fun TracingTips(onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        shape = MaterialTheme.shapes.extraLarge,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
        contentColor = MaterialTheme.colorScheme.onSurface,
        shadowElevation = 6.dp,
        modifier = modifier.fillMaxWidth().testTag("tracing_tip"),
    ) {
        Column(Modifier.padding(start = 14.dp, end = 6.dp, top = 8.dp, bottom = 14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(AppIcons.sparkle, null, Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary)
                Text(stringResource(R.string.tracing_tips_title), Modifier.weight(1f),
                    style = MaterialTheme.typography.titleSmall)
                IconButton(onClick = onDismiss, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.Default.Close, stringResource(R.string.dismiss_tips),
                        Modifier.size(18.dp))
                }
            }
            val tips = listOf(
                R.string.tracing_tip_move,
                R.string.tracing_tip_pinch,
                R.string.tracing_tip_opacity,
                R.string.tracing_tip_lock,
            )
            tips.chunked(2).forEachIndexed { row, pair ->
                Row(Modifier.padding(end = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pair.forEachIndexed { column, tip ->
                        TipStep(row * 2 + column + 1, stringResource(tip), Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

/** One numbered step, so the order reads at a glance without long sentences. */
@Composable
private fun TipStep(number: Int, text: String, modifier: Modifier = Modifier) {
    Row(
        modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(14.dp))
            .heightIn(min = 44.dp).padding(horizontal = 10.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(Modifier.size(22.dp).background(MaterialTheme.colorScheme.primary, CircleShape),
            contentAlignment = Alignment.Center) {
            Text(number.toString(), style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary)
        }
        Text(text, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun TraceChrome(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
        .testTag("screen_trace")) {
        content()
        Row(
            Modifier.align(Alignment.TopStart).fillMaxWidth().statusBarsPadding()
                .padding(start = 12.dp, top = 8.dp, end = 68.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FloatingBack(onBack)
            TraceTitle(title, Modifier.weight(1f, fill = false))
        }
    }
}

@Composable
fun TraceScreen(drawing: DrawingTemplate?, preferences: UserPreferences = UserPreferences(),
    imageUri: String? = null, onChooseImage: () -> Unit = {}, onExplore: () -> Unit = {},
    onTraceSuccess: () -> Unit = {}, onBack: () -> Unit = {}) {
    val overlayState = rememberSaveable(drawing?.id, imageUri, saver = TracingOverlayState.Saver) { TracingOverlayState(opacity = preferences.defaultOpacity) }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val roomForTips = configuration.screenHeightDp >= 480 && configuration.fontScale < 1.6f
    var imageAttempt by remember { mutableIntStateOf(0) }
    var tipsDismissed by rememberSaveable(drawing?.id, imageUri) { mutableStateOf(false) }
    val image by produceState<Pair<Bitmap?, Boolean>>(null to false, drawing?.traceImagePath, imageUri, imageAttempt) {
        value = null to false
        value = try {
            val bitmap = if (imageUri != null) loadUserImage(context.contentResolver, Uri.parse(imageUri))
                else if (drawing != null) TemplateBitmapLoader.load(context.assets, drawing.traceImagePath, 1024)
                else null
            bitmap to (bitmap == null)
        } catch (cancelled: CancellationException) { throw cancelled }
        catch (_: Exception) { null to true }
    }
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

    val view = LocalView.current
    DisposableEffect(view, lifecycleOwner, preferences.keepAwake, permission, image.first != null) {
        val previous = view.keepScreenOn
        fun updateAwake() {
            view.keepScreenOn = previous || (preferences.keepAwake && image.first != null &&
                permission == CameraPermissionState.Granted &&
                lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        }
        val observer = LifecycleEventObserver { _, _ -> updateAwake() }
        lifecycleOwner.lifecycle.addObserver(observer)
        updateAwake()
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            view.keepScreenOn = previous
        }
    }

    // A session counts once the overlay is actually live on camera and the user stays with it,
    // so merely opening a detail page or bouncing off the permission screen never counts.
    val reportSuccess by rememberUpdatedState(onTraceSuccess)
    var counted by rememberSaveable(drawing?.id, imageUri) { mutableStateOf(false) }
    val tracingLive = image.first != null && permission == CameraPermissionState.Granted
    LaunchedEffect(tracingLive, counted) {
        if (tracingLive && !counted) {
            kotlinx.coroutines.delay(TraceSuccessDelayMillis)
            counted = true
            reportSuccess()
        }
    }

    val title = drawing?.let { stringResource(R.string.trace_selected, it.name) }
        ?: stringResource(R.string.your_image)

    TraceChrome(title, onBack) {
        when {
            image.first == null -> {
                Column(
                    Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp).padding(top = 72.dp, bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
                ) {
                    if (image.second) {
                        Text(stringResource(R.string.image_open_failed),
                            style = MaterialTheme.typography.headlineSmall)
                        Text(stringResource(R.string.image_open_help),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { imageAttempt++ }, shape = MaterialTheme.shapes.large,
                            modifier = Modifier.heightIn(min = 50.dp)) {
                            Text(stringResource(R.string.retry_camera))
                        }
                        OutlinedButton(onClick = if (imageUri != null) onChooseImage else onExplore,
                            shape = MaterialTheme.shapes.large,
                            modifier = Modifier.heightIn(min = 50.dp)) {
                            Text(stringResource(if (imageUri != null) R.string.choose_another_image
                                else R.string.explore_drawings))
                        }
                    } else {
                        CircularProgressIndicator()
                        Text(stringResource(R.string.artwork_loading),
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            permission == CameraPermissionState.Granted -> {
                CameraPreview(Modifier.fillMaxSize()) {
                    TracingOverlay(drawing, overlayState, image.first)
                }
                Column(
                    Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                        .widthIn(max = 760.dp).navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    AnimatedVisibility(
                        visible = preferences.showTips && roomForTips && !tipsDismissed,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically(),
                    ) {
                        TracingTips({ tipsDismissed = true })
                    }
                    OverlayControls(overlayState)
                }
            }
            else -> {
                Column(
                    Modifier.fillMaxSize().systemBarsPadding().verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp).padding(top = 72.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp),
                ) {
                    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(72.dp)) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(AppIcons.photo, null, Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                    Text(stringResource(R.string.camera_permission_title),
                        style = MaterialTheme.typography.headlineSmall)
                    Text(stringResource(when (permission) {
                        CameraPermissionState.Blocked -> R.string.camera_permission_blocked
                        CameraPermissionState.Denied -> R.string.camera_permission_denied
                        else -> R.string.camera_permission_explanation
                    }), color = MaterialTheme.colorScheme.onSurfaceVariant)
                    if (permission == CameraPermissionState.Blocked) {
                        Button(onClick = {
                            settingsFailed = runCatching {
                                context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    Uri.fromParts("package", context.packageName, null)))
                            }.isFailure
                        }, shape = MaterialTheme.shapes.large,
                            modifier = Modifier.heightIn(min = 52.dp).testTag("camera_settings")) {
                            Text(stringResource(R.string.open_app_settings))
                        }
                        if (settingsFailed) Text(stringResource(R.string.camera_settings_failed),
                            style = MaterialTheme.typography.bodySmall)
                    } else {
                        Button(onClick = {
                            requested = true
                            launcher.launch(Manifest.permission.CAMERA)
                        }, shape = MaterialTheme.shapes.large,
                            modifier = Modifier.heightIn(min = 52.dp).testTag("allow_camera")) {
                            Text(stringResource(R.string.allow_camera))
                        }
                    }
                }
            }
        }
    }
}
