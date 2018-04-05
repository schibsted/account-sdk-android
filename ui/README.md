---
layout: docs
title: Account SDK UI
sidebar: navigation
---
This is the UI part of the Account SDK, which provides customizable UI flows. The responsibility of UI flows is to provide an authenticated user back to the client thanks to a prebuilt UI.

__Note:__ You should familiarize yourself with the [Core SDK Readme](https://github.com/schibsted/account-sdk-android/tree/master/core) which covers topics like persisting user sessions, logging out and debugging.

## Getting started
The currently available UI flows are:
 * Passwordless login 
    - Email or a phone number can be used.
    - A code is sent to verify the user
 * Signup or Login with password
    - Only email can be user 
    - A password is needed to login/sign-up.

### Configuration

`AndroidManifest.xml`
```xml
<application>
    <service android:name="com.schibsted.account.persistence.UserPersistenceService" />

    <meta-data
        android:name="schacc_client_name"
        android:value="@string/application_label" />
    <meta-data
        android:name="schacc_phone_prefix"
        android:value="47" />
</application>
```

 * `schacc_client_name` is the name of your application, for instance it is displayed on the terms and conditions screen as "I accept the terms and conditions for SPID and _yourAppName_"
 * `schacc_phone_prefix` is used as the default phone prefix to display in case of passwordless login using a phone number.
 
 `strings.xml`
 ```xml
 <resources>
    <string name="schacc_redirect_host">host</string>
    <string name="schacc_redirect_scheme">spid-scheme</string>
</resources>
```
 
 * `schacc_redirect_host` This can be found in Self Service. Example: `login`.
 * `schacc_redirect_scheme` This can be found in SPiD Self Service. Example: `spid-xxxxxxxxxxxxx`
 
To build the `UiConfiguration` : 
 ```java
    UiConfiguration.Builder.fromManifest(getApplicationContext()).build();
```

#### Additional configuration parameters
- `locale` the locale to use for the UIs. Defaults to `Locale.getDefault()`
- `identifierType` which identifier to use for the UIs. `Identifier.IdentifierType.EMAIL` or `Identifier.IdentifierType.SMS`. Defaults to email.
- `signUpEnabled` option to enable or disable sign-up, only allowing existing users. Defaults to true.
- `smartlockEnabled` option to enable or disable smartlock, Defaults to true.
- `logo` a drawable resource to display your brand icon.
- `teaserText` a text to display in the first screen. Limited to 3 lines.

 Example :
 ```java
 UiConfiguration.Builder.fromManifest(getApplicationContext())
                .enableSignUp()
                .logo(R.drawable.ic_example_logo)
                .build()
```
    
### Start the flow
* Create the desired intent
    - Login/Signup with password `PasswordActivity.getCallingIntent(@NonNull final Context context, final UiConfiguration uiConfiguration)`.
    - Login passwordless `PasswordlessActivity.getCallingIntent(@NonNull final Context context, final UiConfiguration uiConfiguration)`.

Once you have initialized the the UI flow, you can start it by calling
`startActivityForResult(myCreatedIntent, yourCode);`

### Get the authenticated user
 To get the `User` back when the flow is successfully finished you have to override `onActivityResult(final int requestCode, final int resultCode, final Intent data)` . Once the flow is finished the `User` can be retrieved by calling
 `data.getParcelableExtra(EXTRA_USER)`. please see the [user-lifecycle]({{ "/guides/user-lifecycle" | relative_url }}) guide for more information about the user.


## Tracking
The UIs support tracking for most events and interactions. To use this, you need to implement the `UiTracking` interface and pass the data off to your tracker of choice. We do provide a default implementation for this using [Pulse](https://github.schibsted.io/spt-identity/identity-sdk-android-internal), which unfortunately is only available internally in Schibsted.

```java
BaseLoginActivity.setTracker(myTracker);
```

You can read more about Pulse at [pulse.schibsted.io](https://pulse.schibsted.io).

## Smartlock
The smartlock feature can be added with the following dependency :
```
implementation "com.schibsted.account:account-sdk-android-smartlock:<VERSION>"
```
To enable the smartlock feature you have to call `UiConfiguration.Builder.fromManifest(getApplicationContext()).enableSmartlock()`

In case of failure you will be notified in `onActivityResult` with the result code `SmartlockImpl.SMARTLOCK_FAILED`.
Then you should re-start your flow without smartlock simply by calling `startActivityForResult(data, YOUR_REQUEST_CODE);` where `data` is the intent provided by
`onActivityResult`.

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
    <color name="schacc_toolbarIconColor">myColor</color>
```

### Customize the loader color
Next to the CTA button a loader appears when an action is performed, you might need to change this color to match your branding.
To apply you own color you need to override the following values in your res/colors.xml file:
```
    <color name="schacc_progressColor">myColor</color>
```

### Hooking UI events
If you need to perform some additional operations before closing the UIs, you can implement `UiHooks` in your application class. These functions will be called before certain events and will not continue before the `OnReadyListener`'s `onReady` function has been called.