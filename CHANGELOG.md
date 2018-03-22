## v0.9.0-beta (2018-03-22)
- Added possibility to customize toolbar colors (#46)
- Added # to filtering of configuration file (#41)
- Added check for the user's agreements on resume (#27)
- Added classloader to Parcel.readStack to avoid using the default one (#16)
- Added API routes for SPiD actions (#20)
- Added Smartlock feature (#12)
- Changed default state of keep me logged in option to true (#47)
- Changed behavior to set the language of the application depending on the uiConfiguration (#45)
- Changed location of deeplink parameters definition (#44)
- Changed keyboard behavior to always be hidden when a screen starts (#23)
- Changed keyboard ime button to act as the continue button (#25)
- Changed identification screen UI (#24)
- Changed UIs to have the visibility of the close button configurable (#21)
- Changed kotlin version from 1.2.21 -> 1.2.30 (#19)
- Fixed incorrect deprecation of oneTimeCode (#48)
- Fixed issue where parsing profile data would fail (#31)
- Fixed issue where pro norway environment was wrong (#32)
- Removed method related to keyboard visibility management (#17)
- Removed ResultCallbackData and replace it with ResultCallback (#22)

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
