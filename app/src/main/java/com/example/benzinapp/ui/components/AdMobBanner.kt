package com.example.benzinapp.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

/**
 * A reusable Composable that embeds a Google AdMob banner ad.
 * By default, it uses the official Google AdMob test banner ad unit ID.
 */
@Composable
fun AdMobBanner(
    modifier: Modifier = Modifier,
    adUnitId: String = "ca-app-pub-3940256099942544/6300978111" // Official AdMob Test Ad Unit ID
) {
    AndroidView(
        modifier = modifier.fillMaxWidth(),
        factory = { context ->
            AdView(context).apply {
                // Set the ad size
                setAdSize(AdSize.BANNER)
                // Set the ad unit ID
                setAdUnitId(adUnitId)
                // Request and load the ad
                loadAd(AdRequest.Builder().build())
            }
        }
    )
}
