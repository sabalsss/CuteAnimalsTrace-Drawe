package com.sabalapps.cuteanimalstrace.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.sabalapps.cuteanimalstrace.R
import com.sabalapps.cuteanimalstrace.data.*
import kotlin.math.roundToInt

/** Hosted policy; the in-app summary dialog is only a fallback when no browser is installed. */
private const val PrivacyPolicyUrl = "https://sabalsss.github.io/CuteAnimalsTrace-DrawPrivacy/"

@Composable
fun SettingsScreen(preferences: UserPreferences,
    onUpdate: (suspend UserPreferencesRepository.() -> Unit) -> Unit) {
    val context = LocalContext.current
    val shareMessage = stringResource(R.string.share_app_message)
    val shareTitle = stringResource(R.string.share_app)
    var confirmClear by rememberSaveable { mutableStateOf(false) }
    var dialog by rememberSaveable { mutableStateOf<Int?>(null) }
    val version = remember(context) {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName.orEmpty()
    }
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            Modifier.widthIn(max = 640.dp).fillMaxSize().testTag("screen_settings")
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp).padding(top = 12.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(22.dp),
        ) {
            Column(Modifier.padding(top = 4.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(stringResource(R.string.settings_title),
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.semantics { heading() })
                Text(stringResource(R.string.settings_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            SettingsSection(stringResource(R.string.appearance), icon = AppIcons.palette) {
                Text(stringResource(R.string.settings_theme_hint),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp))
                Column(Modifier.selectableGroup()) {
                    Appearance.entries.forEach { option ->
                        val label = stringResource(when (option) {
                            Appearance.System -> R.string.theme_system
                            Appearance.Light -> R.string.theme_light
                            Appearance.Dark -> R.string.theme_dark
                        })
                        Row(Modifier.fillMaxWidth().heightIn(min = 52.dp)
                            .testTag("theme_${option.name}")
                            .selectable(preferences.appearance == option, role = Role.RadioButton,
                                onClick = { onUpdate { setAppearance(option) } })
                            .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = preferences.appearance == option, onClick = null)
                            Text(label, Modifier.padding(start = 12.dp),
                                style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                }
                RowDivider()
                PreferenceSwitch(stringResource(R.string.dynamic_color),
                    stringResource(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
                        R.string.dynamic_color_hint else R.string.dynamic_color_unavailable),
                    preferences.dynamicColor, "dynamic_color",
                    enabled = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    onUpdate { setDynamicColor(it) }
                }
            }

            SettingsSection(stringResource(R.string.tracing_settings), icon = AppIcons.opacity) {
                PreferenceSwitch(stringResource(R.string.keep_awake), null,
                    preferences.keepAwake, "keep_awake") { onUpdate { setKeepAwake(it) } }
                RowDivider()
                PreferenceSwitch(stringResource(R.string.show_tips), null,
                    preferences.showTips, "show_tips") { onUpdate { setShowTips(it) } }
                RowDivider()
                var opacity by remember(preferences.defaultOpacity) { mutableFloatStateOf(preferences.defaultOpacity) }
                val opacityLabel = stringResource(R.string.default_opacity)
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(opacityLabel, Modifier.weight(1f),
                            style = MaterialTheme.typography.bodyLarge)
                        Surface(shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer) {
                            Text(stringResource(R.string.overlay_opacity_percent, (opacity * 100).roundToInt()),
                                Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                    Slider(value = opacity, onValueChange = { opacity = it },
                        onValueChangeFinished = { onUpdate { setDefaultOpacity(opacity) } },
                        valueRange = 0.1f..1f,
                        colors = cuteSliderColors(),
                        modifier = Modifier.fillMaxWidth().testTag("default_opacity")
                            .semantics { contentDescription = opacityLabel })
                    Text(stringResource(R.string.default_opacity_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            SettingsSection(stringResource(R.string.notifications), icon = AppIcons.sparkle) {
                PreferenceSwitch(stringResource(R.string.daily_reminder),
                    stringResource(R.string.daily_reminder_hint),
                    preferences.dailyReminder, "daily_reminder") { onUpdate { setDailyReminder(it) } }
            }

            SettingsSection(stringResource(R.string.storage), iconVector = Icons.Default.Delete) {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(stringResource(R.string.clear_recent_confirm),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = { confirmClear = true },
                        enabled = preferences.recent.isNotEmpty(),
                        shape = MaterialTheme.shapes.large,
                        modifier = Modifier.heightIn(min = 48.dp).testTag("clear_recent")) {
                        Text(stringResource(R.string.clear_recent))
                    }
                }
            }

            SettingsSection(stringResource(R.string.support), iconVector = Icons.Default.Info) {
                SupportRow(R.string.how_to_trace) { dialog = R.string.how_to_trace }
                RowDivider()
                SupportRow(R.string.privacy_policy) {
                    try { context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(PrivacyPolicyUrl))) }
                    catch (_: android.content.ActivityNotFoundException) { dialog = R.string.privacy_policy }
                }
                RowDivider()
                SupportRow(R.string.share_app) {
                    val send = Intent(Intent.ACTION_SEND).setType("text/plain")
                        .putExtra(Intent.EXTRA_TEXT, shareMessage + "\nhttps://play.google.com/store/apps/details?id=" + context.packageName)
                    try { context.startActivity(Intent.createChooser(send, shareTitle)) }
                    catch (_: android.content.ActivityNotFoundException) {
                        Toast.makeText(context, R.string.share_unavailable, Toast.LENGTH_SHORT).show()
                    }
                }
                RowDivider()
                SupportRow(R.string.rate_app) {
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW,
                            Uri.parse("market://details?id=" + context.packageName)))
                    } catch (_: android.content.ActivityNotFoundException) {
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW,
                                Uri.parse("https://play.google.com/store/apps/details?id=" + context.packageName)))
                        } catch (_: android.content.ActivityNotFoundException) { dialog = R.string.rate_app }
                    }
                }
            }

            Text(stringResource(R.string.app_version, version),
                Modifier.align(Alignment.CenterHorizontally),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
    if (confirmClear) {
        AlertDialog(onDismissRequest = { confirmClear = false },
            shape = MaterialTheme.shapes.large,
            title = { Text(stringResource(R.string.clear_recent)) },
            text = { Text(stringResource(R.string.clear_recent_confirm)) },
            confirmButton = { TextButton(onClick = {
                onUpdate { clearRecent() }; confirmClear = false
            }) { Text(stringResource(R.string.clear_recent)) } },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text(stringResource(R.string.cancel)) } })
    }
    dialog?.let { title ->
        val text = when (title) {
            R.string.how_to_trace -> stringResource(R.string.how_to_trace_body)
            R.string.privacy_policy -> stringResource(R.string.privacy_placeholder)
            else -> stringResource(R.string.rate_placeholder)
        }
        AlertDialog(onDismissRequest = { dialog = null },
            shape = MaterialTheme.shapes.large,
            title = { Text(stringResource(title)) },
            text = { Text(text, Modifier.verticalScroll(rememberScrollState())) },
            confirmButton = { TextButton(onClick = { dialog = null }) { Text(stringResource(R.string.close)) } })
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: Painter? = null,
    iconVector: ImageVector? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(Modifier.padding(start = 4.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(30.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    if (iconVector != null) {
                        Icon(iconVector, null, Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    } else if (icon != null) {
                        Icon(icon, null, Modifier.size(17.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }
            }
            Text(title, style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.semantics { heading() })
        }
        Surface(
            Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
        ) {
            Column(Modifier.padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp), content = content)
        }
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
}

@Composable
private fun PreferenceSwitch(label: String, hint: String?, checked: Boolean, tag: String,
    enabled: Boolean = true, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().heightIn(min = 56.dp).testTag(tag)
        .toggleable(checked, enabled = enabled, role = Role.Switch, onValueChange = onChange)
        .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(label, style = MaterialTheme.typography.bodyLarge)
            if (hint != null) {
                Text(hint, style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        Switch(checked, onCheckedChange = null, enabled = enabled)
    }
}

@Composable
private fun SupportRow(label: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = androidx.compose.ui.graphics.Color.Transparent,
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
    ) {
        Row(Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(label), Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge)
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
