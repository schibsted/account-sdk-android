---
layout: docs
title: Example usage
sidebar: navigation
---

### Launch the passwordless login flow
First create the `Option` object

```
final Options options = new BaseLoginActivity.Options.Builder().identifierType(Identifier.IdentifierType.SMS).build();
```
To continue create the `UiConfiguration` object, define in your manifest these values:
- `idsdk_client_name` the name of you application
- `idsdk_phone_prefix` the default phone prefix you want to use
- `idsdk_redirect_host` the host of your client, the value has to be `@string/idsdk_redirect_host`
- `idsdk_redirect_scheme` the scheme of your client, the value has to be `@string/idsdk_redirect_scheme`

```
<application ... >
    ...
    <meta-data
        android:name="idsdk_client_name"
        android:value="@string/application_label" />
    <meta-data
        android:name="idsdk_phone_prefix"
        android:value="47" />
    <meta-data
        android:name="idsdk_redirect_scheme"
        android:value="@string/idsdk_redirect_scheme" />
    <meta-data
        android:name="idsdk_redirect_host"
        android:value="@string/idsdk_redirect_host" />
</application>
```

To finish create the intent and start the flow
 ```
final Intent loginIntent = PasswordlessActivity.getCallingIntent(this, options);
startActivityForResult(loginIntent, yourCode);
```

### Get the result of a successful flow
First check the intent of your activity, since the user can be stored in. When a deep link is triggered by the UI SDK and the application has been force closed, the UI SDK start your application and put the user object in the intent if the user logged in successfully.

```
user = getIntent().getParcelableExtra(EXTRA_USER);
if (user == null) {
    user = userPersistence.resumeLast();
    if (user != null) {
        // do something
    }
} 
```

Override the `onActivityResult(final int requestCode, final int resultCode, final Intent data)` method and if the result
code is equal to `Activity.RESULT_OK` then you can get the user by calling `data.getParcelableExtra(EXTRA_USER);`
```
@Override
protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
    super.onActivityResult(requestCode, resultCode, data);

    switch (resultCode) {
        case Activity.RESULT_OK:
            user = data.getParcelableExtra(EXTRA_USER);
            break;
    }
}
```
