# identity-sdk-android
This is the SDK which simplifies connecting to SPiD and comes in two variants. The core part of the SDK contains the login engine and APIs needed to connect to SPiD. You can use this to do your custom UI implementation. While the networking APIs are exposed, you use these at your own peril as no support will be given to this.

The recommended way to use these SDKs is to implement the UI module. This is highly customizable and will give you complete UIs out of the box. You can read more about the [UI SDK](ui) and the [Core SDK](sdk) modules in the README file contained in their module folders.

Additional information can be found on the **[documentation pages](http://schibsted.github.io/account-sdk-android)**

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
    implementation "com.schibsted.account:account-sdk-android-pulse:<VERSION>"
}
```
