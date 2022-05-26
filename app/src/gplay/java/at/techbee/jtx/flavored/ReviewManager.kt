/*
 * Copyright (c) Techbee e.U.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 */

package at.techbee.jtx.flavored

import android.app.Activity
import androidx.preference.PreferenceManager
import at.techbee.jtx.BuildConfig
import at.techbee.jtx.flavored.ReviewManagerDefinition.Companion.PREFS_NEXT_REQUEST
import com.google.android.play.core.review.ReviewManagerFactory
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class JtxReviewManager(val activity: Activity) : ReviewManagerDefinition {

    private val daysToFirstDialog = if(BuildConfig.DEBUG) 1L else 30L
    private val daysToNextDialog = if(BuildConfig.DEBUG) 1L else 90L

    override var nextRequestOn: Long
        get() = PreferenceManager.getDefaultSharedPreferences(activity).getLong(PREFS_NEXT_REQUEST, 0L)
        set(value) {
            PreferenceManager.getDefaultSharedPreferences(activity).edit().putLong(PREFS_NEXT_REQUEST, value).apply()
        }


    override fun launch() {

        if(nextRequestOn == 0L) // first request for donation 30 days after install
            nextRequestOn = ZonedDateTime.now().plusDays(daysToFirstDialog).toInstant().toEpochMilli()

        // We request another review at the earliest 30 days after the previous review
        if(Instant.ofEpochMilli(nextRequestOn).atZone(ZoneId.systemDefault()) > ZonedDateTime.now())
            return

        val manager = ReviewManagerFactory.create(activity)
        val request = manager.requestReviewFlow()
        request.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val reviewInfo = task.result ?: return@addOnCompleteListener
                manager.launchReviewFlow(activity, reviewInfo)
                nextRequestOn = ZonedDateTime.now().plusDays(daysToNextDialog).toInstant().toEpochMilli()
            }
        }
    }
}