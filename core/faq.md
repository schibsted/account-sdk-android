---
layout: docs
title: FAQ
sidebar: navigation
---

#### How do I know that a network error has occurred?
All errors are propagated to the `onError(...)` function and can also be caught by the callback provided to tasks. The `ClientError` class has a field called `errorType` which has an enum. If the value of this is `NETWORK_ERROR`, you know that a network error occurred in the SDK. If debug mode is enabled, this will be logged as a `NetworkError` with a -1 response code.

#### How do I authenticate a WebView?
To authenticate a WebView, you first need to have a logged in `User`. Once you have this, you can call `user.getAuth().oneTimeSessionUrl(...)` to receive a URL which you pass to your WebView. The WebView will now authenticate with SPiD and will redirect you to the URL you provided. Please note that you should provide the client ID for the client you are using in the WebView. Normally this is a JavaScript client. The redirect URL you provide must be whitelisted in that client's list of redirect URLs.
