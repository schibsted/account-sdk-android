## 0.10.0 (2018-04-25)
- Added documentation for session code and url (#108)
- Added support for multiple simultaneous users (#116)
- Added tracking of keepMeLoggedIn and teaser (#121)
- Added a persist function to the user (#117)
- Added a new option to force login using Smarlock (#114)
- Changed the tagging implementation to latest plan (#132)
- Fixed issue where newBuilder wasn't setting the smartlock option properly (#113)
- Fixed issue where a wrong dimension value was assigned to loadingbutton widget (#110)
- Fixed wrong UI module's dependency (#146)
- Fixed issue where UI spacing wasn't correct when using client logo (#147)
- Fixed lint issue about account service registration (#137)
- No longer sending identifiers through redirect URIs (#112)

## 0.9.0 (2018-04-10)
- Added hooks to UI events (#70)
- Added AccountService to the core SDK (#72)
- Added SDK type and version header to InfoInterceptor (#75)
- Added invalid code as a client error (#76)
- Changed READMEs to reflect latest changes (#98)
- Changed contributing guide (#85)
- Changed Logger to improve Java interoperability (#73)
- Changed example app to be easier to understand (#81)
- Changed smartlock implementation to avoid tracking an implicit ui closing (#79)
- Changed translations to match last design copies (#71)
- Changed kotlin version and gradle plugin version to the latest (#69)
- Changed log messages in SessionStorageDelegate (#84)
- Changed location of functions for session handling to the User class (#87)
- Fixed issue with dokka not supporting Gradle > 4.4 (#100)
- Fixed issues where the displayed error message was wrong (#94)
- Fixed issues where UI wasn't displayed properly on landscape mode (#90)
- Fixed issue where interceptor was not cleared on logout (#80)
- Fixed issue where smartlock was running despite the configuration was set to disabled (#68)
- Fixed issue where user was not logged in after a successful deeplink signup redirection (#63)
- Removed unneeded gradle settings (#74)

## 0.9.0-beta2 (2018-03-23)
- Fixed crash caused by missing smartlock dependency (#55)

## 0.9.0-beta1 (2018-03-22)
- Added possibility to customize toolbar colors (#46)
- Added # to filtering of configuration file to allow for comments in the file(#41)
- Added check for the user's agreements on resume (#27)
- Added API routes for SPiD actions (#20)
- Added Smartlock feature (#12)
- Changed default state of keep me logged in option to true (#47)
- Changed behavior to set the language of the application depending on the uiConfiguration (#45)
- Changed keyboard behavior to always be hidden when a screen starts (#23)
- Changed keyboard ime button to act as the continue button (#25)
- Changed identification screen UI to reflect the updated design guide(#24)
- Changed UIs to have the visibility of the close button configurable (#21)
- Changed kotlin version from 1.2.21 -> 1.2.30 (#19)
- Fixed bug where redirection after successful registration did not work (#44)
- Fixed bug where classloader was causing a crash on Samsung (#16)
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
