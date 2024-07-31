package com.example.android.eggtimernotificationcompose.util

import android.content.Intent
import android.net.Uri

/**
 * Opens a URL in the default browser
 *
 * @param context, activity context.
 * @param url, URL to be opened.
 */
fun openUrl(context: android.content.Context, url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    context.startActivity(intent)
}