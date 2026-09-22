package com.sabalapps.cuteanimalstrace.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import com.google.android.play.core.review.ReviewManagerFactory
import com.sabalapps.cuteanimalstrace.R

/** Play listing used by both the review fallback and the share message. */
internal fun storeUrl(context: Context) =
    "https://play.google.com/store/apps/details?id=" + context.packageName

/**
 * Asks Play for the in-app review flow. Play decides whether a card is actually shown and never
 * reports the outcome, so every failure path quietly falls back to the store listing instead.
 */
internal fun launchInAppReview(context: Context, onFinished: () -> Unit = {}) {
    val activity = context.activity()
    if (activity == null) {
        openStoreListing(context)
        onFinished()
        return
    }
    val done = java.util.concurrent.atomic.AtomicBoolean(false)
    fun finishOnce(fallback: Boolean) {
        if (done.compareAndSet(false, true)) {
            if (fallback) openStoreListing(context)
            onFinished()
        }
    }
    try {
        val manager = ReviewManagerFactory.create(context)
        manager.requestReviewFlow()
            .addOnSuccessListener { info ->
                try {
                    manager.launchReviewFlow(activity, info)
                        .addOnSuccessListener { finishOnce(fallback = false) }
                        .addOnFailureListener { finishOnce(fallback = true) }
                } catch (_: Exception) { finishOnce(fallback = true) }
            }
            .addOnFailureListener { finishOnce(fallback = true) }
    } catch (_: Exception) {
        finishOnce(fallback = true)
    }
}

private fun openStoreListing(context: Context) {
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW,
            Uri.parse("market://details?id=" + context.packageName)))
    } catch (_: ActivityNotFoundException) {
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(storeUrl(context))))
        } catch (_: ActivityNotFoundException) {
            Toast.makeText(context, R.string.rate_placeholder, Toast.LENGTH_LONG).show()
        }
    }
}

/** Android Sharesheet; the same message Settings sends. */
internal fun shareApp(context: Context, message: String, chooserTitle: String) {
    val send = Intent(Intent.ACTION_SEND).setType("text/plain")
        .putExtra(Intent.EXTRA_TEXT, message + "\n" + storeUrl(context))
    try { context.startActivity(Intent.createChooser(send, chooserTitle)) }
    catch (_: ActivityNotFoundException) {
        Toast.makeText(context, R.string.share_unavailable, Toast.LENGTH_SHORT).show()
    }
}
