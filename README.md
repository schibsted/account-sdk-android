These SDKs allows for connecting to Schibsted Account. The functionality of the modules are described below and exists primarily in two variants; with and without UIs. 

**For support, please contact [support@spid.no](mailto:support@spid.no)**

### Modules
**UI module**<br>
[Documentation](https://schibsted.github.io/account-sdk-android/ui/)
|
[API Reference](https://schibsted.github.io/account-sdk-android/ui/docs/)
<br>This module provides complete UIs for creating and logging in to accounts. using this is the recommended approach, as it handles all things the GDPR requirements for transparency. These UIs are highly customizable, so that you can get the same look and feel throughout your application. **You should be familiar with the documentation of the core module as well, before implementing.**

**Core module**<br>
[Documentation](https://schibsted.github.io/account-sdk-android/core/)
|
[API Reference](https://schibsted.github.io/account-sdk-android/core/docs/)
<br>The core module contains all networking, models and business logic for the SDK. The UI module is built on top of this, implementing the controllers available in this module. You can implement custom UIs on top of this as well, although this is generally not recommended.

**SmartLock module**<br>
[Documentation](https://schibsted.github.io/account-sdk-android/ui/#smartlock)
<br>The SmartLock module builds on top of the UI module, allowing the users to log in using SmartLock. When enabled, this will fall back to using the UIs, unless the mode is specifically set to `FORCED`, in which case it will return an error on failure.

**Common module**<br>
[API Reference](https://schibsted.github.io/account-sdk-android/common/docs/)
<br>This is a module containing cross module interfaces and common utilities used by the other modules. If you're implementing a custom tracker for the UIs, this is where you'll find the interface to implement.


## Getting started
To get started with either SDK, you'll need to request access to SPiD before you can start using them. This process is documented on the [SPiD Techdocs](https://techdocs.spid.no/selfservice/access/) site. Once you have access and have created your client so that you have access to your client ID and secret, you should head over to the [documentation pages](http://schibsted.github.io/account-sdk-android).

### Gradle setup
```
dependencies {
    implementation "com.schibsted.account:account-sdk-android-core:<VERSION>"
    // or
    implementation "com.schibsted.account:account-sdk-android-ui:<VERSION>"
}
```

### SDK setup
To configure the SDK, you are required to have a `schibsted_account.conf` file in your assets. This must contain all values to be able to function. An error will be thrown if the configuration is missing. You can however manually override the configuration if you choose to store your configuration some other way (we'd recommend that you don't store secrets in the manifest).

```yaml
environment: PRE
clientId: 58xxxxxxxxxxxxxxxx27
clientSecret: k8xxxxxxxxxxxxxLm
```

The environment can be one of `DEV|PRE|PRO|PRO_NORWAY|<CUSTOM_URL>`.

#### How can I debug my implementation?
By default, the SDK will output information about any errors which occurs with detailed information about the exception, network request and the context. To see these, please ensure your log level is set to debug for the errors, and to verbose if you want to know the context of them as well as seeing the operations the SDK is performing. You can filter on the `SCHACC` tag in Logcat. The debug mode of the SDK is taken from the `BuildConfig.DEBUG` fields, but can be overwritten by changing the value of `Logger.loggingEnabled` so that you can enable logging in a release version as well.
