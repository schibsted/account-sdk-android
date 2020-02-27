package com.schibsted.account.persistence

import android.content.Context
import android.content.SharedPreferences
import com.schibsted.account.common.util.Logger
import java.text.SimpleDateFormat
import java.util.*

/**
 * Storage for [AgreementCache], backed by [SharedPreferences].
 * Stores user ID and expiration date as string.
 */
internal class AgreementStorage(context: Context) {

    private val appContext: Context = context.applicationContext

    private val prefs: SharedPreferences by lazy {
        appContext.getSharedPreferences(PREFERENCE_FILENAME, Context.MODE_PRIVATE)
    }

    private var stored: String?
        get() = prefs.getString(KEY_AGR_CACHE, null)
        set(value) = prefs.edit().run {
            putString(KEY_AGR_CACHE, value)
            apply()
        }

    fun getAgreement(): Pair<String, Date>? {
        return stored?.run {
            runCatching {
                val (userId, date) = split(DELIMITER, limit = 2)
                userId to date.asDate()
            }.onFailure {
                // Storage might contain some legacy data that is not formatted properly.
                // Just remove it:
                stored = null
                Logger.error(TAG, "Failed to parse stored data; " +
                        "clearing agreement cache", it)
            }.getOrNull()
        }
    }

    fun storeAgreement(userId: String, expiration: Date) {
        stored = "$userId$DELIMITER${expiration.asString()}"
    }

    private fun Date.asString() = checkNotNull(sdf.get()).format(this)

    private fun String.asDate() = checkNotNull(sdf.get()).parse(this)

    companion object {
        private const val TAG = "AgreementCache"
        private const val PREFERENCE_FILENAME = "IDENTITY_PREFERENCES"
        private const val KEY_AGR_CACHE = "AGR_CACHE"
        private const val DELIMITER = "|"
        private const val DATE_PARSING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

        private val sdf = object : ThreadLocal<SimpleDateFormat>() {
            override fun initialValue() = SimpleDateFormat(DATE_PARSING_FORMAT, Locale.US)
        }
    }
}
