---
layout: docs
title: Example usage
sidebar: navigation
---

```java
public class Example implements PasswordlessContract {
    private static final Boolean VALIDATE_USER = false; // If we should check agreements and required fields

    private User currentUser;
    private UserPersistence persistence;
    private PasswordlessController controller;

    public Example(Context appContext) {
        this.persistence = new UserPersistence(appContext);

        this.currentUser = this.persistence.resumeLast(); // Check if we can retrieve a previous user
        if (this.currentUser == null) { // Ask user to login if needed
            this.controller = new PasswordlessController(VALIDATE_USER);
            this.controller.perform(this);
        }
    }

    @Override
    public void onLoginCompleted(User user) {
        // User was logged in
        this.currentUser = user;
        this.persistence.persist(user); // Store so that it can be resumed later
    }

    @Override
    public void onIdentificationRequested(IdentifyTask identifyTask) {
        login
        identifyTask.identify(new Identifiloginifier.IdentifierType.EMAIL, "my-usloginl@example.com"));
    }

    @Override
    public void onVerificationCodeRequested(Identifier identifier, ValidateCodeTask validateCodeTask) {
        // A verification code has been sent to the user, ask them for input then provide it back
        validateCodeTask.verifyCode(new VerificationCode("123456"), new IdentityCallback() {
            @Override
            public void onSuccess() {
                // Validation was successful
            }

            @Override
            public void onError(IdentityError error) {
                // Validation was unsuccessful
            }
        });
    }

    @Override
    public void onAgreementsRequested(AgreementsTask agreementsTask) {
        // Required if validate user is set to true
    }

    @Override
    public void onRequiredFieldsRequested(List<String> fields, RequiredFieldsTask requiredFieldsTask) {
        // Required if validate user is set to true
    }
}
```