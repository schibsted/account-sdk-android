---
layout: docs
title: User life-cycle
sidebar: navigation
---
When using either of the SDKs, we return you a `User` object which represents the logged in user. The life-cycle of this user goes like this:

1. Resume a previous session if available
2. Create a new session, i.e. ask a user to log in 
3. Update profile, make authenticated calls etc
4. Persist the user for when the app opens again
5. Logging a user out

## Resuming a session

**Option A: Resuming a persisted session**<br />
To resume a persisted session, we've provided a `UserPersistence` class which handles all persistence of user sessions. To use this, you initialize the `UserPersistence` class with a context and call one of the resume functions. You can resume the last session or resume a specific user id. This is the recommended option.

Please note that if a user has not selected the _"remember me"_ option, the user's session will never be persisted in the first place.

```java
UserPersistence persistence = new UserPersistence(getApplicationContext());
User lastUser = persistence.resumeLast();
```

**Option B: Resuming a session from a session code**<br />
You might want to resume a session from for example a web view, different platform etc. To support this, we provide a function to create a user from a session code. The `redirectUri` must also be set in SPiD Self Service to be accepted. `isPersistable` should be set in according to the user's wishes. We **cannot** persist a user without their consent.

```java
User.fromSessionCode(code: String, redirectUri: String, isPersistable: Boolean, callback: IdentityCallbackData<User>)
```

**Option C: Resuming a session from a token**<br />
This would only ever be used in corner cases, but if you happen to have an access token and want to resume that session in the SDK, you can do so by using the default constructor for `User`.

```java
User user = new User(accessToken, userCanBePersisted);
```

## Creating a session
**Option A: Using the UIs**<br />
The SDK provides plug-and-play UIs which can be used to log users in. This is the preferred way to log in users. It returns the user as an activity result. For details, please see the [UI documentation]({{ "/ui/" | relative_url }}).

**Option B: Using the Core SDK**<br />
The Core SDK provides a set of controllers which will dynamically request what it needs to log in a user. You can implement your own UIs on top of this or very simply make a CLI if you want. We recommend having a look at the UI SDK first to see if that can fit your needs. Refer to the [Core documentation]({{ "/core/" | relative_url }}) for details.

**Option C: Using the APIs**<br />
While there should not be many reasons for doing this, it is still possible. The APIs can be found in the Core SDK and provides Retrofit calls for the SPiD functionality.


## Using a session
The user session can be used for any number of things. You might want to use it for getting the user profile, doping authenticated calls to your services or just want a way to identity your users. The `User` object gives quick access to this in, grouping actions together as follows:

- **Auth:** Used to request one time session urls and other actions on the user's session
- **Agreements:** Get agreements status, links or accept the terms and agreements
- **Profile:** Get or update the user profile

The user object itself provides functions to log out a user as well as a `bind` function which can be used to perform authenticated requests.


## Persisting a session
The Core SDK provides a `UserPersistence` class to deal with persisting users as mentioned previously. This can be used to persist a user session so that it can be resumed the next time the user opens your app. This respects the status and wishes of the user, so that a User's session cannot be persisted if:
- A. The user has not selected the *"keep me logged in"* option
- B. The user is already logged out

If any of these occurs, a warning will be given. Please note that if you want the user to be able to resume the session when they re-open your app, you should **always** persist the `User` object before closing the application, as its state could have been updated.

```java
UserPersistence persistence = new UserPersistence(getApplicationContext());
persistence.persist(user);
```

## Logging out
To log out a user, you call the `logout` function in the `User` object. This will destroy the current session and log the user out of SPiD, so that any future use of that user's token will no longer be valid.

If the user logs out, you should also remove that user id from persistence (or clear the persistence fully), using the `UserPersistence` class. While you could never use the user's session again anyways, doing this is considered good practice. 

```java
User user = ...;
UserPersistence persistence = new UserPersistence(getApplicationContext());

user.logout(logoutCallback);

persistence.remove(user.getUserId().getId());
// or
persistence.removeAll();
```

**Catching logouts:**<br />
The Core SDK will broadcast any user logouts if the `support-core-utils` are provided on the classpath. Please refer to the [Listening for events]({{ "/core/#listening-for-events" | relative_url }}) section in the Core SDK README for more details.