This is the UI part of the Account SDK, which provides customizable UI flows. The responsibility of UI flows is to provide an authenticated user back to the client thanks to a prebuilt UI.

The currently available UI's are:
 * Passwordless login 
    - Email or a phone number can be used.
    - A code is sent to verify the user
 * Signup or Login with password
    - Only email can be used 
    - A password is needed to login/sign-up.

__Note:__ You should familiarize yourself with the [Core SDK Readme](../core) which covers topics like persisting user sessions, logging out and debugging.


## Configuration
The minimal configuration of the SDK UIs are: client name, redirect scheme and redirect host. The client name is displayed on the terms and conditions screen as "I accept the terms and conditions for SPID and _yourAppName_", while the two other fields are required for deep linking and can be found in SelfService.

`strings.xml`
```xml
<string name="schacc_conf_client_name">My client name</string>
<string name="schacc_conf_redirect_scheme">spid-myclientid</string>
<string name="schacc_conf_redirect_host">login</string>
``` 


### Additional configuration parameters
You can further control the behavior of the UIs bu specifying any of the following attributes. These can be specified in your `AndroidManifest.xml` or by implementing `OptionalConfiguration.UiConfigProvider` in your `Application` class. Using only one of these is recommended, but if you were to use both, then the configuration provider will be resolved before the manifest.

- **Locale:** The locale to use for sending verification email and SMS from Schibsted Account. Defaults: `Locale.getDefault()`.
- **Sign-up enabled:** Whether or not creation of new accounts should be allowed. Please note that an error message must be specified in order to disable this. Default: true.
- **Sign-up disabled message:** The error message to show when a user attempts to create a new account if it's disabled. No default.
- **Cancellable:** When set to false, the UIs will no longer show the close button. Default: true.
- **Client logo:** The logo to display in the UIs. Default: 0.

#### Android manifest
```xml
<application>
    <meta-data android:name="@string/schacc_conf_locale" android:value="en_EN" />
    <meta-data android:name="@string/schacc_conf_signup_enabled" android:value="false" />
    <meta-data android:name="@string/schacc_conf_signup_disabled_message" android:value="Some reason" />
    <meta-data android:name="@string/schacc_conf_cancellable" android:value="false" />
    <meta-data android:name="@string/schacc_conf_client_logo" android:resource="@drawable/schacc_ic_cancel" />
</application>
```

#### Configuration provider
```java
public class App extends Application implements OptionalConfiguration.UiConfigProvider {
    @NonNull
    @Override
    public OptionalConfiguration getUiConfig() {
        return new UiConfig.Builder()
                .locale(new Locale("nb", "NO"))
                .clientLogo(R.drawable.ic_example_logo)
                .build();
    }
}
```

### Starting the UIs
The UIs can be started through the `getCallingIntent` function in the `AccountUi` class. This returns an intent, which you start for the result. The function takes parameters which changes the behavior.

- **Flow type:** Which UI flow to start. This can be the password flow, passwordless using email or passwordless using sms.
- **Params:** This contains the optional arguments for the UIs.
    - **Teaser text:** A limited text which will be displayed on the initial screen. Default: `null`.
    - **Pre-filled identifier:** Specifying this will pre-fill the identifier field in the UIs, skipping straight to the following step. Default: `null`.
    - **SmartLock mode:** Sets the mode of the SmartLock implementation. Can be `ENABLED`, `DISABLED` or `FORCED`. The latter will return an error if login could not be done using SmartLock alone. Default: `DISABLED`.
    - **OpenID Scope:** If you're using any non default scopes, these should be defined here. Default: `OIDCScope.SCOPE_OPENID`. 


```java
final Intent intent = AccountUi.getCallingIntent(
    getApplicationContext(),
    AccountUi.FlowType.PASSWORD,
    new AccountUi.Params(
        getString(R.string.example_teaser_text), 
        "user@example.com",
        SmartlockMode.DISABLED,
        new String[]{OIDCScope.SCOPE_OPENID}));

startActivityForResult(intent, PASSWORD_REQUEST_CODE);
```


### Get the authenticated user
 To get the `User` back when the flow is successfully finished you have to 
 1. Override `onActivityResult(final int requestCode, final int resultCode, final Intent data)`.
 2. Check the result code, it is equal to `Activity.RESULT_OK` in case of success. 
 3. Extract the `User` from the intent by calling `data.getParcelableExtra(EXTRA_USER)`.
 
### Get the case of failure
To get the case of failure when the flow did not success you have to:
 1. Override `onActivityResult(final int requestCode, final int resultCode, final Intent data)`.
 2. Check the result code, it is equal to `AccountUi.RESULT_ERROR` in case of error.
 3. Extract the `NetworkError` from the intent by calling `data.getParcelableExtra(AccountUi.EXTRA_ERROR)`.


## Tracking
The UIs support tracking for most events and interactions. To use this, you need to implement the `UiTracking` interface and pass the data off to your tracker of choice.

```java
BaseLoginActivity.setTracker(myTracker);
```

### Pulse tracking (Available internally in Schibsted only)
We provide a default implementation for tracking, using _Pulse_. This is available in the [pulse module](https://github.schibsted.io/spt-identity/account-sdk-android-internal) and its dependency can be added the following way after adding the internal repository.

```
implementation "com.schibsted.account:account-sdk-android-pulse:<SCHACC-PULSE-VERSION>"
```

Please note that the `SCHACC-PULSE-VERSION` could differ from `SCHACC-VERSION` because the pulse library doesn't belong to the same repository and is not tied to the rest of the SDK.
Check out the pulse [changelog](https://github.schibsted.io/spt-identity/account-sdk-android-internal/blob/master/CHANGELOG.md) to find which version to use with the different SDK releases.

You can read more about Pulse at [pulse.schibsted.io](https://pulse.schibsted.io).


## Advanced usage

### Customize colors
The provided UI come with fully configurable colors. If you want to change these colors to match with your application you need to override the following values in your res/colors.xml file:

```
    <color name="schacc_primaryEnabled">myColor</color>
    <color name="schacc_primaryDisabled">myColor</color>
    <color name="schacc_primaryHovered">myColor</color>
    <color name="schacc_primaryActive">myColor</color>

    <color name="schacc_secondaryEnabled">myColor</color>
    <color name="schacc_secondaryDisabled">myColor</color>
    <color name="schacc_secondaryHovered">myColor</color>
    <color name="schacc_secondaryActive">myColor</color>

    <color name="schacc_backgroundColor">myColor</color>
```
### Customize the toolbar
If needed the color of the toolbar's elements can be customized.
To apply you own color you need to override the following values in your res/colors.xml file:

- To change the color of the primary and secondary text
```
    <color name="schacc_primaryHeader">myColor</color>
    <color name="schacc_secondaryHeader">myColor</color>
```
- To change the color of icons
```
    <color name="schacc_toolbarIconsColor">myColor</color>
```
- To change the color of the toolbar itself
```
    <color name="schacc_toolbarColor">myColor</color>
```

### Customize the loader color
Next to the CTA button a loader appears when an action is performed, you might need to change this color to match your branding.
To apply you own color you need to override the following values in your res/colors.xml file:
```
    <color name="schacc_progressColor">myColor</color>
```

### Hooking UI events
If you need to perform some additional operations before closing the UIs, you can implement `UiHooks` in your application class. These functions will be called before certain events and will not continue before the `OnProceedListener`'s `proceed` function has been called.

## FAQ
**How do I provide my own loading screen?**<br>
We will only show the loading indicator in the UIs if we need to retrieve client info. This can be pre-loaded if you want your own loading screen. To do so, use the `AccountUi.preInitialize(...)` function to perform the load. This will notify the callback when ready.
