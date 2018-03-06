## 0.8.0 (2018-03-06)
**NOTE:** The artifact names has changed as of this release and can now be found on Bintray. This means that you can find the packages on JCenter
```
implementation "com.schibsted.account:account-sdk-android-core:<VERSION>"
implementation "com.schibsted.account:account-sdk-android-ui:<VERSION>"
implementation "com.schibsted.account:account-sdk-android-pulse:<VERSION>"
```

- Added publishing to Bintray
- Changed the name of the generated artifacts
- Changed the UIs to respect the remember me setting on sign-up
- Removed IdentityCallback, now named ResultCallback
- Removed IdentityError, now named ClientError
- Renamed the SDK to Schibsted Account SDK
