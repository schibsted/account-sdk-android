This is the SDK which simplifies connecting to SPiD and comes in two variants. The core part of the SDK contains the login engine and APIs needed to connect to SPiD. You can use this to do your custom UI implementation. While the networking APIs are exposed, you use these at your own peril as no support will be given to this.

The recommended way to use these SDKs is to implement the UI module. This is highly customizable and will give you complete UIs out of the box. You can read more about the [UI SDK](ui) and the [Core SDK](core) modules in the README file contained in their module folders.

For support, please contact [support@spid.no](mailto:support@spid.no)

## Getting started
To get started with either SDK, you'll need to request access to SPiD before you can start using them. This process is documented on the [SPiD Techdocs](https://techdocs.spid.no/selfservice/access/) site. Once you have access and have created your client so that you have access to your client ID and secret, you should head over to the [documentation pages](http://schibsted.github.io/account-sdk-android).

### Gradle setup
```
dependencies {
    implementation "com.schibsted.account:account-sdk-android-core:<VERSION>"
    // or
    implementation "com.schibsted.account:account-sdk-android-ui:<VERSION>"
    // (optional, available internally in Schibsted only)
    implementation "com.schibsted.account:account-sdk-android-pulse:<PULSE-VERSION>"
}
```
Please note that the `PULSE-VERSION` could differ from `VERSION` because the pulse library doesn't belong to the same repository and is not tied to the rest of the SDK.
Check out the pulse [changelog](https://github.schibsted.io/spt-identity/account-sdk-android-internal/blob/master/CHANGELOG.md) to find the latest version.

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
