package com.schibsted.account.persistence

import android.util.Base64

internal fun ByteArray.encodeBase64(): ByteArray = Base64.encode(this, Base64.DEFAULT)

internal fun ByteArray.decodeBase64(): ByteArray = Base64.decode(this, Base64.DEFAULT)
