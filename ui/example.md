---
layout: docs
title: Example usage
sidebar: navigation
---

### Launch the password login flow
First create the `UiConfiguration` object

```java
   final UiConfiguration uiConfiguration = UiConfiguration.Builder.fromManifest(getApplicationContext())
                   .enableSignUp()
                   .logo(R.drawable.ic_example_logo)
                   .locale(new Locale("nb", "NO"))
                   .teaserText(getString(R.string.example_teaser_text))
                   .build();
```
To continue edit your `AndroidManifest.xml` as following:

```
<application>
    <service android:name="com.schibsted.account.persistence.UserPersistenceService" />

    <meta-data
        android:name="schacc_client_name"
        android:value="myCompanyName" />
    <meta-data
        android:name="schacc_phone_prefix"
        android:value="47" />
</application>
```
Then edit your `String.xml` and add :
```xml
   <string name="schacc_redirect_host">host</string>
   <string name="schacc_redirect_scheme">spid-scheme</string>
```
Finally start the flow :
 ```java
final Intent passwordIntent = PasswordActivity.getCallingIntent(getApplicationContext(), uiConfiguration);
startActivityForResult(passwordIntent, yourCode);
```

### Get the result of a successful flow
Override the `onActivityResult(final int requestCode, final int resultCode, final Intent data)` method and if the result
code is equal to `Activity.RESULT_OK` then you can get the user by calling `data.getParcelableExtra(EXTRA_USER);`

```java
  if (requestCode == yourCode) {
    if (resultCode == Activity.RESULT_OK) {
        // when the flow was performed without any issue, you can get the newly created user.
        user = data.getParcelableExtra(BaseLoginActivity.EXTRA_USER);
    } else if (resultCode == SmartlockImpl.SMARTLOCK_FAILED) {
        //in case of smartlock failure,relaunch the flow without smartlock
        startActivityForResult(data, PASSWORD_REQUEST_CODE);
    }   
  }
```
