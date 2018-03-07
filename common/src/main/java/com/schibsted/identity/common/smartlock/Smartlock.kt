package com.schibsted.account.common.smartlock

interface Smartlock {
    fun requestCredentials()

  //  //to save the user if in the first place no credentials was found,
  //  // then the user log-in using the our flow and if he wants -> save credentials.
      // ONLY FOR PASSWORD FLOW
  //  fun saveCredentials()
}
