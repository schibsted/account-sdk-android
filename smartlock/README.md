The smartlock module can be added to the UIs with the following dependency. The UIs will then automatically hook it in and respect the mode specified in the `Params` used in the `AccountUi.getCallingIntent` function.

```
implementation "com.schibsted.account:account-sdk-android-smartlock:<VERSION>"
```

There are three SmartLock modes:
- SmartlockMode.DISABLED: The SDK will not attempt to log the user in with SmartLock. 
- SmartlockMode.ENABLED: The SDK will attempt to log the user in with SmartLock. If it fails but SmartLock managed to get the user identifier, the usual login flow will be launched 
with identifier prefilled. For more information see [Google SmartLock flow](https://developers.google.com/identity/smartlock-passwords/android/overview)
- SmartlockMode.FORCED: The SDK will attempt to log user in with and only with SmartLock. 
- SmartlockMode.FAILED: Tell the SDK that you've attempted to log in using SmartLock, but that the user cancelled it or the attempt failed. This will allow the user to store new credentials.

In any case of failure when using the SmartlockMode.FORCED mode you will be notified in `onActivityResult` with the result code `AccountUi.SMARTLOCK_FAILED`. You might want to directly restart the flow, choosing to disable (`SmartlockMode.DISABLED`) SmartLock or tell the UIs that it failed (`SmartlockMode.FAILED`) for a seamless user experience. The former does not allow the user to store any new credentials, while the latter will allow the user to store any new login credential they provide.

Please note that if you're using SmartLock, you need to handle user logouts. To do this, either set up the `SmartlockReceiver` (as seen below) to listen for logout events or manually call `Credentials.getClient(activity, CredentialsOptions.DEFAULT).disableAutoSignIn()`.

```java
protected void onCreate(Bundle savedInstanceState) {
    smartlockReceiver = new SmartlockReceiver(this);
    
protected void onStart() {
    LocalBroadcastManager.getInstance(getApplicationContext())
        .registerReceiver(smartlockReceiver, new IntentFilter(Events.ACTION_USER_LOGOUT));

protected void onStop() {
    LocalBroadcastManager.getInstance(getApplicationContext())
        .unregisterReceiver(smartlockReceiver);
```
