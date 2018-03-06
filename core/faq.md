---
layout: docs
title: FAQ
sidebar: navigation
---

#### How do I know that a network error has occurred?
All errors are propagated to the `onError(...)` function and can also be caught by the callback provided to tasks. The `IdentityError` class has a field called `errorType` which has an enum. If the value of this is `NETWORK_ERROR`, you know that a network error occurred in the SDK. If debug mode is enabled, this will be logged as a `NetworkError` with a -1 response code.


#### How can I debug my implementation?
By default, the SDK will output information about any errors which occurs with detailed information about the exception, network request and the context. To see these, please ensure your log level is set to debug for the errors, and to verbose if you want to know the context of them as well as seeing the operations the SDK is performing. You can filter on the `IDSDK` tag in Logcat. The debug mode of the SDK is taken from the `BuildConfig.DEBUG` fields, but can be overwritten by changing the value of `Logger.loggingEnabled` so that you can enable logging in a release version as well.


#### How do I authenticate a WebView?
To authenticate a WebView, you first need to have a logged in `User`. Once you have this, you can call `user.getAuth().oneTimeSessionUrl(...)` to receive a URL which you pass to your WebView. The WebView will now authenticate with SPiD and will redirect you to the URL you provided. Please note that you should provide the client ID for the client you are using in the WebView. Normally this is a JavaScript client. The redirect URL you provide must be whitelisted in that client's list of redirect URLs.
