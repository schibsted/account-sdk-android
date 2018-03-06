## 0.7.0 (2018-03-02)
**NOTE:** Using versions 0.7.0 and older, uses the following package names:
```
implementation "com.schibsted.identity:identity-sdk-android-core:<VERSION>"
implementation "com.schibsted.identity:identity-sdk-android-ui:<VERSION>"
implementation "com.schibsted.identity:identity-sdk-android-pulse:<VERSION>"
```

- Added client id to profile summary link
- Added functionality to pre-fill identifiers
- Added login only error text to UiConfiguration
- Added newBuilder function on UiConfigration
- Added teaser text to the UIs
- Changed message from unknown SPiD errors
- Changed parsing of legacy userid. Now preferring idtoken
- Changed target and compile version along with buildtools version to 27.0.3
- Changed the deep linking implementation to be more sturdy
- Changed the teaser text style as per the design specs
- Changed UiTracking to use legacy ID instead of subject ID
- Fixed bug in UIs build.gradle where it did not read host properly
- Fixed issue where activity would be re-added to the navigation stack
- Fixed issue where error text would be shown when editing an input field
- Fixed issue where input fields would not trim the content
- Fixed issue where invalid tokens would be accepted
- Fixed issue where parsing of SPiD errors would fail
- Fixed issue where password input was using text suggestions from dictionary
- Fixed issue where reading token compat would ignore some tokens
- Fixed issue where token would not be updated after refresh
- Fixed issue where WebFragment would not launch deep links
- Fixed possible issue with classpath inspection where LocalBroadcastManager would be obfuscated
- Fixed proguard issue occurring with instrumentation tests
- Fixed typo in core/README.md
- Removed password validation when signing in
- Removed UiOptions and merged it with UiConfiguration


## 0.6.0 (2018-02-20)
- Added copyright notice to source files
- Added deploy script for updating the generated docs to gh-pages
- Added getAccountSummaryLink to Profile
- Added legacy ID to the UserId object
- Added missing string translations to the UI
- Added option to pre-fill identifiers
- Added possibility to cancel ongoing requests
- Added teaser text to UIs
- Added UI tests
- Changed parsing of legacy userid. Now preferring result from ID token
- Changed UiTracking to use legacy ID instead of subject ID
- Fixed an issue with logout where token would be refreshed before
- Fixed build issue with invalid task name
- Fixed crash when localizedMessage would be null
- Fixed issue when refreshing a token which had an empty refresh token
- Fixed issue where input fields would not trim the content
- Fixed issue where token would not be updated after refresh
- Fixed typo in core/README.md
- Removed identifier from UI tracking. Passing account ID when available
- Removed obfuscation for released artifacts
- Removed UiOptions and merged it with UiConfiguration


## 0.5.1 (2018-02-14)
- Added proguard rule to properly access the identityUiOptions class


## 0.5.0 (2018-02-13)
- Added an example application
- Added contributors file
- Added LICENSE and additional info files
- Added login only feature flag
- Added option to allow non-whitelisted domains for authenticated requests
- Added refresh event to the broadcast manager
- Added SDK and device info to user agent for internal requests
- Changed displayName no not be auto generated any longer
- Changes in preparation of open sourcing the SDK
- Deprecated `bind(builder, urls, allowNonHttps)` in favor of `bind(builder, urls)` and `bind(builder, urls, allowNonHttps, allowNonWhitelistedUrls)`
- Fixed issue where password field was not honoring it's IME options
- Fixed issue where password was not hidden by default
- Removed unneeded ktlint executable


## 0.4.1 (2018-02-07)
- Added register/unregister methods to the identity broadcast manager
- Fixed issue where PasswordlessActivity's getCallingIntent method wan unreachable form Java
- Fixed issue where User's session would not be cleared from service holder
- Fixed multiple errors in EncryptionKeyProvider
- Fixed publishing and removed unneeded intent filter
- Removed the app and pulse modules to separate repositories
- Security fix for validating code parameter in deep link
- Updated password screen to UI version 4


## 0.4.0 (2018-02-01)
**Please note**: The artifact IDs has been changed, starting with this release. You should now use the following
```
implementation "com.schibsted.identity:account-sdk-android-core:<VERSION>"
implementation "com.schibsted.identity:account-sdk-android-ui:<VERSION>"
implementation "com.schibsted.identity:identity-sdk-android-pulse:<VERSION>"
```

**Added**
- Added a CODEOWNERS file ot require reviews from owners
- Added article on the user lifecycle in hte docs folder
- Added client id to tracked fields in pulsetracker
- Added ClientConfiguration to proguard keep
- Added customizable colors for the UI in public.xml
- Added display name as a required field
- Added DSL builder for tracking events
- Added error handling in EncryptionKeyProvider
- Added fetch agreement operations to controllers
- Added functionality to pre-fill required fields if already available
- Added hashing to tracking identifier
- Added loading indicator to the primary action button
- Added method to enable visibility of current action button in of error when onFlowReady is called
- Added method to handle view visibility on navigation back
- Added missing required fields
- Added more logging to the Auth interceptor for easier debugging
- Added option to create user from one time code
- Added option to navigate back multiple steps
- Added prefix to all SDK resources
- Added readme to the pulse module
- Added rules to not obfuscate agreements
- Added the common module
- Added tracking implementation to UIs
- Added UiConfiguration to proguard
- Added validation to allow only digits for birthday field

**Changed**
- Changed all UI resources' visibility to private
- Changed architecture of core SDK
- Changed back navigation to avoid re-evaluation of the contract when not needed
- Changed controllers to have a finalize function before completing
- Changed exception handling in key store to catch all key store exceptions
- Changed it so that user cannot be persisted if not set specifically
- Changed Kotlin version to 1.2.21
- Changed localization strings to reflect recent changes
- Changed name of the Core SDK Proguard file
- Changed nullity check location in TermsFragment
- Changed path were schibstedid.conf has to be generated
- Changed persistence to be more readable
- Changed proguard rule to keep class members in responses package
- Changed setTextAppearence to use the appcompat version
- Changed the contents of the UI README to reflect the latest changes
- Changed the email validation rule to use android.util.Patterns
- Changed UIs to not show soft keyboard when focusing inputview

**Fixed**
- Fixed issue where the client configuration and utils would be obfuscated
- Fixed a crash when attempting to persist an empty user collection
- Fixed bug related to byte array size in user persistence
- Fixed compatibility issues with API 16
- Fixed issue where agreements texts were not being shown
- Fixed issue where IME options was incorrect in required fields
- Fixed issue where required fields container would be hidden behind the continue button
- Fixed issue where session classes was obfuscated
- Fixed issue where the action button's visibility was not set after animations
- Fixed issue where the sign-up controller would ask for required fields even if the client had no field requirements
- Fixed issue where the UIs would request unsupported required fields
- Fixed issue where updating profile would erase non-provided fields
- Fixed issue where User would not be parcelized
- Fixed issue with UI input text not being selectable
- Fixed issues with the deep link implementation
- Fixed issues with the tracking implementation to match the updated tagging plan
- Fixed multiple issues with deep links it's format
- Fixed proguard issue for preconditions
- Fixed proguard issue with pulsetracking
- Fixed proguard settings to provide access to session classes
- Fixed the hockey app publishing
- Fixed UIs to not display forgot password when user is registering

**Removed**
- Removed identifier option in password flow for now
- Removed irrelevant docs and non working links
- Removed logger from core, re-added to the common module
- Removed parcelize annotation to be compatible with older API levels
- Removed singletask activity parameter for the UI
- Removed unnecessary fields from values.xml
- Removed useless condition in IdentificationPresenter


## 0.4.0-preview-3 (2017-12-11)
- Added birthday as a required field
- Added proguard configuration
- Added support for rotation in the UIs
- Added Finnish string resources
- Added argument for signup redirect uri
- Added intent to start client application
- Added persistence example to readme
- Added deep link for confirmation email 
- Added encryption to persistence
- Added method to close or open keyboard depending on the number of fields
- Added tests for LoginController
- Added support of custom header
- Added function to check account status from identifier
- Added tests for the AuthInterceptor
- Added tests for client configuration
- Added deep link for forgot password feature
- Changed email validation rule
- Changed layout of terms dialog
- Changed names of contracts, tasks and providers
- Changed password controller to have separate login and sign up paths
- Changed Norwegian and Swedish translations
- Changed user token to allow for null tokens
- Removed useless dependencies
- Removed unnecessary exposure of 3rd party libraries
- Removed onError method from LoginContract
- Fixed issue where scope was not included when authenticating
- Fixed issues where maven dependencies were not added to POM after gradle update
- Fixed issue where snapshot versions would not correctly depend on sub-projects

## 0.4.0-preview-2 (2017-11-14)
- Added Password flow
- Added feature to broadcast logouts
- Added kotlintest to library versions
- Added methods to check if a session can be resumed in UserPersistence
- Added publishing of releases to hockey app
- Added username/password flow with signup to the UIs
- Changed Kotlin version to 1.1.60
- Changed SDK to align with its iOS counterpart
- Changed error field visibility on focus in UIs
- Changed location of onIdentifier interface
- Changed swap configuration properties
- Changed test setup to use KotlinTest instead of Spek
- Changed the user persistence and improved networking
- Fixed artifact publishing
- Fixed correctly adding bearer before access token
- Fixed issue where authorization header was missing the bearer keyword
- Fixed link in core SDK readme
- Fixed navigation onback + onpause/resume in UIs
- Fixed refresh issue with interceptor
- Fixed wrong parameter name in oauth service
- Removed extra interceptor parameter
- Removed old configuration object
- Removed temporary travis fix
- Renamed variable names in integration functions for clarity


## 0.4.0-preview-1 (2017-10-31)
**Added**
- Added error handling of SPiD errors
- Added function to request one-time-codes with a different back-end client ID
- Added IME actions to the login UI
- All login flow states is now fully parcelable
- Crash reporting in the example app
- Different icon per environment for the example app
- Integrated with QualityGate and TricklerDowner
- New translations: Norwegian and Swedish
- New UIs for required fields
- Now showing the agreements in a web view from the login flow
- Publishing the UI component
- Refreshing tokens are now done in serial
- SDK and device info is now being passed in the User-Agent header
- Started using Kotlin for development
- Support for multiple simultaneous logged in users
- Support for multiple users on the same system
- The core SDk now requires an implementation contract which it will query for input
- Wrote documentation after major SDK changes

**Changed**
- Changed email validation rules
- Changed how we handle phone prefixes
- Checking if a user has already agreed to the T&A in the login flow
- Configuration has been changed
- Continue button now visible by default
- Differentiating the UI when using phone vs email
- Improved transition animations
- Major code refactoring
- Major revamp of the build system
- Now releasing snapshots on all merges to master
- Now starting the UIs with startIntentForResult
- The back-stack navigation implementation has been improved
- The configuration fields in the manifest has been changed
- The network APIs have been refactored, now exposing their calls instead
- The UI implementation is now using the refactored core SDK
- Updated UIs to be compliant with the updated design guidelines

**Fixed**
- Fixed issue where we were not refreshing the tokens correctly
- Issues with JSON parsing is now fixed
- Keyboard was not closing on some Samsung devices
- Keyboard was not hidden when showing the agreements screen
- The verification view should now be more responsive on older devices

**Removed**
- Integration module has been removed for now
- Removed Spanish, Greek and Russian translations
- The Manager class and all its dependant code
- The previous login flows in the core SDK
