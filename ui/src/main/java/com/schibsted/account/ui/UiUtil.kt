/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.ui

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.annotation.ColorRes
import android.support.v4.content.ContextCompat
import android.support.v4.graphics.drawable.DrawableCompat
import android.telephony.TelephonyManager
import com.schibsted.account.common.tracking.TrackingData
import com.schibsted.account.engine.input.Identifier
import com.schibsted.account.ui.login.screen.LoginScreen
import java.util.Locale

object UiUtil {
    @JvmStatic
    fun getTrackingScreen(loginScreen: LoginScreen): TrackingData.Screen? = when (loginScreen) {
        LoginScreen.IDENTIFICATION_SCREEN -> TrackingData.Screen.IDENTIFICATION
        LoginScreen.PASSWORD_SCREEN -> TrackingData.Screen.PASSWORD
        LoginScreen.VERIFICATION_SCREEN -> TrackingData.Screen.VERIFICATION_CODE
        LoginScreen.TC_SCREEN -> TrackingData.Screen.AGREEMENTS
        LoginScreen.REQUIRED_FIELDS_SCREEN -> TrackingData.Screen.REQUIRED_FIELDS
        LoginScreen.CHECK_INBOX_SCREEN -> TrackingData.Screen.ACCOUNT_VERIFICATION
        else -> null
    }

    @JvmStatic
    fun getTrackerIdType(idType: Identifier.IdentifierType): TrackingData.IdentifierType = when (idType) {
        Identifier.IdentifierType.EMAIL -> TrackingData.IdentifierType.EMAIL
        Identifier.IdentifierType.SMS -> TrackingData.IdentifierType.PHONE
    }

    @JvmStatic
    fun setLanguage(context: Context, locale: Locale) {
        val resources = context.resources
        val conf = resources.configuration
        conf.locale = locale
        resources.updateConfiguration(conf, resources.displayMetrics)
    }

    @JvmStatic
    fun getTintDrawable(context: Context, drawable: Drawable, @ColorRes color: Int): Drawable {
        val tintDrawable = DrawableCompat.wrap(drawable)
        DrawableCompat.setTint(tintDrawable, ContextCompat.getColor(context, color))
        return tintDrawable
    }

    private val countryPrefixes = mapOf(
            "BE" to 32,
            "BZ" to 501,
            "BJ" to 229,
            "BT" to 975,
            "BO" to 591,
            "BA" to 387,
            "BW" to 267,
            "BR" to 55,
            "BN" to 673,
            "BG" to 359,
            "BF" to 226,
            "MM" to 95,
            "BI" to 257,
            "KH" to 855,
            "CM" to 237,
            "CA" to 1,
            "CV" to 238,
            "CF" to 236,
            "TD" to 235,
            "CL" to 56,
            "CN" to 86,
            "CX" to 61,
            "CC" to 61,
            "CO" to 57,
            "KM" to 269,
            "CG" to 242,
            "CD" to 243,
            "CK" to 682,
            "CR" to 506,
            "HR" to 385,
            "CU" to 53,
            "CY" to 357,
            "AF" to 93,
            "AL" to 355,
            "DZ" to 213,
            "AD" to 376,
            "AO" to 244,
            "AQ" to 672,
            "AR" to 54,
            "AM" to 374,
            "AW" to 297,
            "AU" to 61,
            "AT" to 43,
            "AZ" to 994,
            "BH" to 973,
            "BD" to 880,
            "BY" to 375,
            "CZ" to 420,
            "DK" to 45,
            "DJ" to 253,
            "TL" to 670,
            "EC" to 593,
            "EG" to 20,
            "SV" to 503,
            "GQ" to 240,
            "FI" to 358,
            "FR" to 33,
            "ER" to 291,
            "EE" to 372,
            "ET" to 251,
            "FK" to 500,
            "FO" to 298,
            "FJ" to 679,
            "PF" to 689,
            "GA" to 241,
            "GM" to 220,
            "GE" to 995,
            "DE" to 49,
            "GH" to 233,
            "GI" to 350,
            "GR" to 30,
            "GL" to 299,
            "GT" to 502,
            "GN" to 224,
            "GW" to 245,
            "GY" to 592,
            "HT" to 509,
            "HN" to 504,
            "HK" to 852,
            "HU" to 36,
            "IN" to 91,
            "ID" to 62,
            "IR" to 98,
            "IQ" to 964,
            "IE" to 353,
            "IM" to 44,
            "IL" to 972,
            "IT" to 39,
            "CI" to 225,
            "JP" to 81,
            "JO" to 962,
            "KZ" to 7,
            "KE" to 254,
            "KI" to 686,
            "KW" to 965,
            "KG" to 996,
            "LA" to 856,
            "LV" to 371,
            "LB" to 961,
            "LS" to 266,
            "LR" to 231,
            "LY" to 218,
            "LI" to 423,
            "LT" to 370,
            "LU" to 352,
            "MO" to 853,
            "MK" to 389,
            "MG" to 261,
            "MW" to 265,
            "MY" to 60,
            "MV" to 960,
            "ML" to 223,
            "MT" to 356,
            "MH" to 692,
            "MR" to 222,
            "MU" to 230,
            "YT" to 262,
            "MX" to 52,
            "FM" to 691,
            "MD" to 373,
            "MC" to 377,
            "MN" to 976,
            "ME" to 382,
            "MA" to 212,
            "MZ" to 258,
            "NA" to 264,
            "NR" to 674,
            "NP" to 977,
            "NL" to 31,
            "AN" to 599,
            "NC" to 687,
            "NZ" to 64,
            "NI" to 505,
            "NE" to 227,
            "NG" to 234,
            "NU" to 683,
            "KP" to 850,
            "NO" to 47,
            "OM" to 968,
            "PK" to 92,
            "PW" to 680,
            "PA" to 507,
            "PG" to 675,
            "PY" to 595,
            "PE" to 51,
            "PH" to 63,
            "PN" to 870,
            "PL" to 48,
            "PT" to 351,
            "PR" to 1,
            "QA" to 974,
            "RO" to 40,
            "RU" to 7,
            "RW" to 250,
            "BL" to 590,
            "WS" to 685,
            "SM" to 378,
            "ST" to 239,
            "SA" to 966,
            "SN" to 221,
            "RS" to 381,
            "SC" to 248,
            "SL" to 232,
            "SG" to 65,
            "SK" to 421,
            "SI" to 386,
            "SB" to 677,
            "SO" to 252,
            "ZA" to 27,
            "KR" to 82,
            "ES" to 34,
            "LK" to 94,
            "SH" to 290,
            "PM" to 508,
            "SD" to 249,
            "SR" to 597,
            "SZ" to 268,
            "SE" to 46,
            "CH" to 41,
            "SY" to 963,
            "TW" to 886,
            "TJ" to 992,
            "TZ" to 255,
            "TH" to 66,
            "TG" to 228,
            "TK" to 690,
            "TO" to 676,
            "TN" to 216,
            "TR" to 90,
            "TM" to 993,
            "TV" to 688,
            "AE" to 971,
            "UG" to 256,
            "GB" to 44,
            "UA" to 380,
            "UY" to 598,
            "US" to 1,
            "UZ" to 998,
            "VU" to 678,
            "VA" to 39,
            "VE" to 58,
            "VN" to 84,
            "WF" to 681,
            "YE" to 967,
            "ZM" to 260,
            "ZW" to 263)

    @JvmStatic
    fun getSimCountry(context: Context): Int? {
        val telephonyService = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        val countryCode = telephonyService.simCountryIso.toUpperCase()
        return countryPrefixes[countryCode]
    }
}
