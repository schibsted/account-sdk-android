---
layout: docs
title: Identity SDK UI
sidebar: navigation
---
This is the UI part of the Identity SDK, which provides customizable UI flows. The responsibility of UI flows is to provide an authenticated user back to the client thanks to a prebuilt UI.

__Note:__ You should familiarize yourself with the [Core SDK Readme]({{ "/core/" | relative_url }}) which covers topics like persisting user sessions, logging out and debugging.

## Getting started
The currently available UI flows are:
 * Passwordless login 
    - Email or a phone number can be used.
    - A code is sent to verify the user
 * Signup or Login with password
    - Only email can be user 
    - A password is needed to login/signup.
##### Add required properties
The ui relies on properties which need to be defined in your `AndroidManifest.xml file`
 * The `idsdk_client_name` which is the name of your application, for instance it is displayed on the terms and conditions screen as "I accept the terms and conditions for SPID and yourAppName"
 * The `idsdk_phone_prefix` used as the default phone prefix to display in case of passwordless login using a phone number.
 * The `idsdk_redirect_host` the host of your client, the value has to be `@string/idsdk_redirect_host` [See Gradle setup]({{ "/#gradle-setup" | relative_url}})
 * The `idsdk_redirect_scheme` the scheme of your client, the value has to be `@string/idsdk_redirect_scheme` [See Gradle setup]({{ "/#gradle-setup" | relative_url}})

##### Specify flow parameters
 The `options` parameter allow you to specify :
 * the identifier type, used to start the passwordless flow using a phone number or an email address
    - `Identifier.IdentifierType.EMAIL` or `Identifier.IdentifierType.SMS`
    - Call `new BaseLoginActivity.Options.Builder.identifierType(final Identifier.IdentifierType identifierType)` to do specify the identifier type.
* an icon to be displayed in the toolbar,
    - Call `new BaseLoginActivity.Options.Builder.headerResource(@DrawableRes final int res)` to specify the icon to be used.
    
#### Start the flow
* Create the desired intent
    - Login/Signup with password `PasswordActivity.getCallingIntent(@NonNull final Context context, final Options options)`.
    - Login passwordless `PasswordlessActivity.getCallingIntent(@NonNull final Context context, final Options options)`.

Once you have initialized the the UI flow, you can start it by calling
`startActivityForResult(myCreatedIntent, yourCode);`

#### Get the authenticated user
 To get the `User` back when the flow is successfully finished you have to override `onActivityResult(final int requestCode, final int resultCode, final Intent data)` . Once the flow is finished the `User` can be retrieved by calling
 `data.getParcelableExtra(EXTRA_USER)`. please see the [user-lifecycle]({{ "/guides/user-lifecycle" | relative_url }}) guide for more information about the user.


## Tracking
The UIs support tracking for most events and interactions. To use this, you need to implement the `IdentityUiTracking` interface and pass the data off to your tracker of choice. We do provide a default implementation for this using [Pulse](https://github.schibsted.io/spt-identity/identity-sdk-android-internal), which unfortunately is only available internally in Schibsted.

```java
BaseLoginActivity.setTracker(myTracker);
```

You can read more about Pulse at [pulse.schibsted.io](https://pulse.schibsted.io).

## Advanced usage

### Customize colors
The provided UI come with fully configurable colors. If you want to change these colors to match with your application you need to override the following values in your res/colors.xml file:

```
    <color name="idsdk_primaryEnabled">myColor</color>
    <color name="idsdk_primaryDisabled">myColor</color>
    <color name="idsdk_primaryHovered">myColor</color>
    <color name="idsdk_primaryActive">myColor</color>

    <color name="idsdk_secondaryEnabled">myColor</color>
    <color name="idsdk_secondaryDisabled">myColor</color>
    <color name="idsdk_secondaryHovered">myColor</color>
    <color name="idsdk_secondaryActive">myColor</color>
```
### Customize the toolbar
If needed the color of the toolbar text can be customized.
To apply you own color you need to override the following values in your res/colors.xml file:

```
    <color name="idsdk_primaryHeader">myColor</color>
    <color name="idsdk_secondaryHeader">myColor</color>
```
### Customize the loader color
Next to the CTA button a loader appears when an action is performed, you might need to change this color to match your branding.
To apply you own color you need to override the following values in your res/colors.xml file:
```
    <color name="idsdk_progressColor">myColor</color>
```
