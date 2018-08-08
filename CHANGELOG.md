## v.1.1.0 (2018-08-08)
- Added the possibility to copy-paste code during passwordless flow(#320)
- Added the possibility to get user's subscriptions(#319)
- Fixed an issue where the loading screen was displayed two times(#322)

## v1.0.4 (2018-07-27)
- Fixed issue where presenters were not initialized (#310)

## v1.0.3 (2018-07-13)
- Fixed issue where redirect_uri was not correctly built (#307)
- Fixed issue where the corrupted terms cache wasn't cleared (#304)

## v1.0.2 (2018-07-10)
- Fixed issue where the date was parsed incorrectly (#302)

## 1.0.1 (2018-06-29)
- Added cache for agreement status checks performed on resume (#297)
- Added type checking to Gson parsing to deal with boolean being parsed as string from SPiD (#295)
- Fixed issue where Gson parsed the id token incorrectly (#294)
- Fixed issue where language wasn't set according to the manifest (#293)

## 1.0.0 (2018-06-21)
- Added null pointer guard on removing invalid tokens to handle old, corrupt tokens (#278)
- Added option to override locale in Routes (#276)
- Added package information to the info interceptor (#275)
- Fixed a crash caused by preemptively dismissing the loading (#279)
- Fixed issue where going back from the required fields screen would go back two steps (#287)
- Updated translations (#288)

## v1.0.0-rc6 (2018-06-13)
- Add explicit error when the flow fails initialization (ex. networking) (#264)
- Changed the terms screen to be fully GDPR compliant (#150)
- Fixed an issue where the encryption key would not yet be valid due to different time zones (#263)
- Fixed an issue where the incorrect launch mode was preventing broadcast of the activity's result (#271)
- Fixed an issue where the passwordless flow wasn't launched correctly (#269)
- Removed age disclaimer when not signing up (#266)

## v1.0.0-rc5 (2018-06-08)
- Changed refresh token to be optional to prepare for the new OAuth service (#250)
- Changed security checks in AuthInterceptor to be more modular (#259)
- Fixed an unhandled KeyNotYetValidException in PersistenceEncryption (#253)
- Fixed crash when auth token was corrupt (#247)
- Fixed crash when user exited during initialization (#260)
- Fixed issue where automatic login did not work after signing up (#247)
- Fixed issue where deep link back from reset password would not be caught (#251)
- Fixed issue where loading dialog dismissal would cause a crash (#238)
- Fixed issue where SmartLock credentials could not be stored after user dismissed the dialog (#254)
- Fixed issue where SmartLock docs were not being deployed upon release (#249)
- Fixed margin on the continue button on the identification screen (#252)
- Removed headers which routed to the new OAuth service for now (#257)

## v1.0.0-rc4 (2018-06-01)
**Known issues**
- When logging in using SmartLock and dismissing the dialog, new credentials cannot be stored (#236)

**Changes**
- Added accessibility labels for the UIs (#222)
- Added builder pattern to AccountUi.Params for Java interoperability (#233)
- Added option to customize the background color (#213)
- Changed deep link validation to allow for all formats (#235)
- Changed FlowType.PASSWORDLESS_PHONE to FlowType.PASSWORDLESS_SMS (#225)
- Changed X-OIDC header to be conditionally sent as required by SPiD (#207)
- Fixed an issue where redirect URIs was ignored for account summary URL (#211)
- Fixed an issue where the webview wasn't scrollable (#212)
- Fixed an issue where vector images were being displayed incorrectly (#227)
- Fixed automatic publishing of gh-pages (#209)
- Fixed crash when the loading dialog was dismissed (#228)
- Fixed issue where auto login was not disabled after a user logged out (#224)
- Fixed issue where login with phone number couldn't be started (#217)
- Fixed issue where sessions could be bound more than once (#234)
- Fixed issue where the auth interceptor used an outdated token after user manually logged out (#232)

## v1.0.0-rc3 (2018-05-24)
- Fixed issues where app was crashing at launch (#205)
- Fixed issue where signup was failing due to scopes (#202)

## v1.0.0-rc2 (2018-05-22)
**Known issues**
- Automatically log the user in from a verification email doesn't work
- when the application is closed and opened via the verification email deep link app crashes

**Changes**
- Added missing tracking links (#199)
- Fixed issue where toolbar was updated before the UI was launched (#189)
- Fixed issue where deeplink was not triggered (#190)
- Fixed issue where required fields were not updated (#193)
- Fixed issue where back press link in webview was restarting the app (#197)

## v1.0.0-rc1 (2018-05-17)
**Note:** This release has completely reworked how the UIs are configured. Please see the readme for details
**Known issues**
- Not integrating the smartlock module results in a crash when app is launched

**Changes**
- Added links for GDPR compliant terms (#174)
- Added loading screen when retrieving client info (#183)
- Added module summary to the README (#166)
- Added scope parameter to resume user from session code method (#181)
- Added scopes to the UI module (#182)
- Added support for display name error (#167)
- Changed create account screen to be GDPR compliant (#145)
- Changed docs to reflect the configuration changes (#179)
- Changed image resources to match the latest UI copies (#165)
- Changed Kotlin runtime to 1.2.41 (#156)
- Changed README to specify where to find pulse version (#144)
- Changed required fields screen description to be GDPR compliant (#149)
- Changed string resources to match the latest UI changes (#161) (#164)
- Changed the configuration of the UI module (#172)
- Changed the error icon (#178)
- Changed the ID screen with the latest changes (#151)
- Changed the tracking implementation to match the latest tagging plan(#162)
- Changed verification logic from Java to Kotlin (#177)
- Fixed crash when unbinding a non-bound service
- Fixed crash when unbinding a non-bound service (#184)
- Fixed CTA button UI to match copies (#176)
- Fixed issue where KeyStore was not loaded successfully (#155)
- Fixed issue where network call was done on screen rotation (#158)
- Fixed issue where padding was wrong (#173)
- Removed docs publishing for UI and SmartLock module (temporary) (#185)

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
