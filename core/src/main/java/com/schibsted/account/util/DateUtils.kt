package com.schibsted.account.util

import com.schibsted.account.common.util.Logger
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

object DateUtils {
    private const val DATE_PARSING_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
    private const val TAG = "DateUtils"

    fun fromString(stringDate: String): Date? {
        var date: Date? = null
        try {
            date = SimpleDateFormat(DATE_PARSING_FORMAT, Locale.US).parse(stringDate)
        } catch (p: ParseException) {
            Logger.error(TAG, "Unable to parse the date", p)
        } finally {
            return date
        }
    }

    fun getLaterRandomDateAsString(minValue: Int, bound: Int): String? {
        val cal = Calendar.getInstance()
        val rand = Random().nextInt(bound) + minValue

        cal.time = Date()
        cal.add(Calendar.MINUTE, rand)

        return SimpleDateFormat(DATE_PARSING_FORMAT, Locale.US).format(cal.time)
    }
}