# Account SDK Core
This is the core part of the Account SDK, which simplifies accessing and using the SPiD platform. The core SDK has two responsibilities: Controlling login flows for users and providing easy to use network calls. This provides the login engine, which is used by our UIs and can optionally be used to do custom implementation

Please note that using the APIs alone is neither recommended or supported by the Account team. If you choose not to use the controllers, you do so at your own peril. The APIs will not be covered in this readme, but you can have look at the _service package_, which provides Retrofit2 calls for the SPiD APIs.


## Getting started
There currently available flows are:
* **Passwordless** authenticates using email or phone number end a confirmation code which needs to be validated.
* **Login** uses traditional login with username(email or phone number) and password.
* **Sign-up** is for creating an account and logging in with username and password

To initialize the login flows you need to implement the corresponding contract, `PasswordlessContract`, `LoginContract` or `SignUpContract`. These contain all the required steps which might be requested by the engine, to which a user must provide additional input. The callbacks themselves receive tasks as parameters, on which you can do different actions, like provide additional fields, accept user agreements etc. 

Once you have implemented the contracts, you should initialize the controller you want to use, i.e. `PasswordlessController` or `LoginController`. To start the login routine, call the `perform(...)` function and the controller will try to perform as many tasks as possible automatically and will prompt the contract if it needs additional input.

Any errors which occurs when executing a task will will be propagated to the callbacks provided to those actions. 

When the login flow is completed, the `onLoginCompleted` function will be called with your `User` object, which also contains the session.

### Configuration
To configure the SDK, you are required to have a `schibsted_account.conf` file in your assets. This must contain all values to be able to function. An error will be thrown if the configuration is missing. You can however manually override the configuration if you choose to store your configuration some other way (we'd recommend that you don't store secrets in the manifest).

```yaml
environment: PRE
clientId: 58xxxxxxxxxxxxxxxx27
clientSecret: k8xxxxxxxxxxxxxLm
```

The environment can be one of `DEV|PRE|PRO|PRO_NORWAY|<CUSTOM_URL>`.

### Managing user sessions
After a successful login, you get a `User` object. This object is parcelable, so it will survive rotations etc. Please note that the core SDK does not persist your sessions automatically, but provides a way to persist and resume this. 

The user object is responsible for doing user actions like updating profile data. It also has a reference to its session, from which you can request one time login codes or logout the current session. After logging out a session, you should not perform any actions on either the session or the user, as the token will now be invalidated by SPiD.

You can also bind a user session to an `OkHttpClient`, so that you can perform authenticated requests. Please read more about this in the [Authenticated requests](#authenticated-requests) section.

### Persisting user sessions
The SDK provides the `AccountService` which includes the `UserPersistenceService`. This automates persisting and refreshing the stored user on token updates etc as well as logouts. To use this, you should bind this anywhere where your user is active. 

__Note:__ Remember to add this service to your manifest if you're using the `AccountService`.
```xml
<service android:name="com.schibsted.account.persistence.UserPersistenceService" />
```

__Example__

```java
public class MyActivity extends AppCompatActivity {
    private AccountService accountService;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        accountService = new AccountService(getApplicationContext());
        getLifecycle().addObserver(accountService)
    }

    // If you for some reason don't have the most recent support library, you can call these manually instead
    // @Override
    // protected void onStart() {
    //     accountService.bind();
    // }

    // @Override
    // protected void onStop() {
    //     accountService.unbind();
    // }
}
```

### Resuming user sessions
You can manage user sessions by using the static functions of the `User` class. Normal use cases are usually limited to resuming sessions, but it's also possible to remove previous sessions or clear all sessions. Please note that if your intention is to log out the user, you should call `user.logout(...)` to ensure the tokens are invalidated with the back-end.

__Example__
```java
User user = User.resumeSession(context, myUserId, new new ResultCallback<User>() { ... })

User user = User.resumeLastSession(context, new new ResultCallback<User>() { ... })
```

### Logging out
To logout a user, you call the `logout(...)` function on the `User` object. This will invalidate the token with SPiD and your session will no longer be valid. The `AccountService` will pick this up and remove the session from persistence.

## Advanced usage

### Listening for events
If you need to respond to events when a user logs in etc, you can register a `BroadcastReceiver` to the `LocalBroadcastManager` to get the event. The event actions are available in the `Events` class
- User logs in (Extra: User)
- User logs out (Extra: UserId)
- User's token refreshes (Extra: User)

### Authenticated requests
You can authenticate your requests by binding a session to an `OkHttpClient`. As long as the user session is not logged out, this will manage authentication requests and keep your session alive. To bind a session, you need to provide an `OkHttpClient.Builder` to the User session which we will attach the required interceptor.

You must also specify which hosts you will use the authenticated requests for. For security reasons you __should not attach session information to requests that does not need to be authenticated__, as this would allow third parties to hijack a session from a user.
