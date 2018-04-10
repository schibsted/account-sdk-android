---
layout: docs
title: Example usage
sidebar: navigation
---
```kotlin
class Example(val appContext : Context) : PasswordlessContract {
    private var user : User? = null
    private var verifyUser : Boolean = true
    
    init {
            User.resumeLastSession(appContext, object :ResultCallback<User>{
                override fun onSuccess(result: User) {
                    user = result
                }
                override fun onError(error: ClientError) {
                    // can't get the user, starting the login flow
                    LoginController(verifyUser).start(this@Example)
                }
            })
    }
    
    override fun onFlowReady(callbackProvider: CallbackProvider<LoginResult>) {
        callbackProvider.provide(object : ResultCallback<LoginResult> {
            override fun onSuccess(result: LoginResult) {
                user = result.user
            }

            override fun onError(error: ClientError) {
                // Getting the user failed
            }
        })
    }

    override fun onIdentifierRequested(provider: InputProvider<Identifier>) {
        provider.provide(Identifier(Identifier.IdentifierType.EMAIL, "user@example.com"), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                // Identifier is valid
            }

            override fun onError(error: ClientError) {
                // Identifier is not valid
            }
        })
    }

    override fun onVerificationCodeRequested(verificationCodeProvider: InputProvider<VerificationCode>, identifier: Identifier) {
        verificationCodeProvider.provide(VerificationCode("12345", true), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                // the validation was successful
            }

            override fun onError(error: ClientError) {
                // the validation was unsuccessful
            }
        })
    }

    /**
     * Triggered if validate user is set to true
     */
    override fun onAgreementsRequested(agreementsProvider: InputProvider<Agreements>, agreementLinks: AgreementLinksResponse) {
        agreementsProvider.provide(Agreements(true), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                // agreements acceptance was successful
            }

            override fun onError(error: ClientError) {
                // agreements acceptance was unsuccessful
            }
        })
    }

    /**
     * Triggered if validate user is set to true
     */
    override fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>) {

        requiredFieldsProvider.provide(RequiredFields(mutableMapOf()), object : ResultCallback<NoValue> {
            override fun onSuccess(result: NoValue) {
                // required user fields were successfully provided
            }

            override fun onError(error: ClientError) {
                // required user fields were not successfully provided
            }
        })
    }
}
```
