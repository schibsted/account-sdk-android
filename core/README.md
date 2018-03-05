# Identity SDK Core
This is the core part of the Identity SDK, which simplifies accessing and using the SPiD platform. The core SDK has two responsibilities: Controlling login flows for users and providing easy to use network calls. This provides the login engine, which is used by our UIs and can optionally be used to do custom implementation

Please note that using the APIs alone is neither recommended or supported by the Identity team. If you choose not to use the controllers, you do so at your own peril. The APIs will not be covered in this readme, but you can have look at the _service package_, which provides Retrofit2 calls for the SPiD APIs.


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
To configure the SDK, you are required to have a `schibstedid.conf` file in your assets. This must contain all values to be able to function. An error will be thrown if the configuration is missing. You can however manually override the configuration if you choose to store your configuration some other way (we'd recommend that you don't store secrets in the manifest).

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

### Persisting and resuming sessions
The SDK does not persist your user sessions automatically, so you need to choose when and which user sessions to persist. Usually this would be when you receive an `onLoginCompleted` event. To persist or resume sessions, you should use the `UserPersistence` class. This provides everything necessary to resume a session and will check if the session is still valid.

__Example__
```java
UserPersistence persistence = new UserPersistence(getApplicationContext());

// Storing a user
persistence.persist(user);

// Resuming a specific session
User resumedUser = persistence.resume("someUserId-123");

// Resuming the last session
User lastUser = persistence.resumeLast();

// Remove user form persistence (should be done on logout etc)
persistence.remove("someUserId-123");
```

### Logging out
To logout a user, you call the `logout(...)` function on the `User` object. This will invalidate the token with SPiD and your session will no longer be valid. To catch this event, you can listen for the logout event broadcast with the id found in `IdentityEventManager.LOGOUT_EVENT_ID`, you can read more about [listening for events](#listening-for-events).

## Advanced usage

### Listening for events
We are broadcasting events locally in the application, so to listen for events, you are going to need to include the `support-core-utils` library from Google.
```
implementation "com.android.support:support-core-utils:<VERSION>"
```

We will inspect the class path and only broadcast if this is available. Furthermore, you should create an instance of `IdentityEventManager` and keep a reference to this in you main `Application` class. This will ensure that it stays loaded as long as you application is active. Please note that you should pass in the application context, not the application itself. You can now register `BroadcastReceivers` for any event you're interested in. The list of event IDs can be found as static constants in `IdentityEventManager`.

### Authenticated requests
You can authenticate your requests by binding a session to an `OkHttpClient`. As long as the user session is not logged out, this will manage authentication requests and keep your session alive. To bind a session, you need to provide an `OkHttpClient.Builder` to the User session which we will attach the required interceptor.

You must also specify which hosts you will use the authenticated requests for. For security reasons you __should not attach session information to requests that does not need to be authenticated__, as this would allow third parties to hijack a session from a user.
