/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.schibsted.account.engine.integration.ResultCallback;
import com.schibsted.account.model.NoValue;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.AccountService;
import com.schibsted.account.session.User;
import com.schibsted.account.ui.UiConfiguration;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.login.flow.password.PasswordActivity;
import com.schibsted.account.ui.smartlock.SmartlockImpl;

import java.util.Locale;

import static com.schibsted.account.ui.login.BaseLoginActivity.EXTRA_USER;

public class MainActivity extends AppCompatActivity {
    final static int PASSWORD_REQUEST_CODE = 1;

    private AccountService accountService;
    private User user;
    private TextView userState;
    private Button logoutButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //_____________________IDENTITY SDK INIT__________________

        accountService = new AccountService(getApplicationContext());
        getLifecycle().addObserver(accountService);

        // Get the UiConfiguration
        final UiConfiguration uiConfiguration = UiConfiguration.Builder.fromManifest(getApplicationContext())
                .enableSignUp()
                .logo(R.drawable.ic_example_logo)
                .locale(new Locale("nb", "NO"))
                .teaserText(getString(R.string.example_teaser_text))
                .build();

        // Create the intent for the desired flow
        final Intent passwordIntent = PasswordActivity.getCallingIntent(this, uiConfiguration);

        // Start the flow
        if (savedInstanceState == null && !getIntent().hasExtra(EXTRA_USER)) {
            startActivityForResult(passwordIntent, PASSWORD_REQUEST_CODE);
        }

        //____________________________________________________

        final TextView sdkVersion = findViewById(R.id.example_app_sdk_version_view);
        userState = findViewById(R.id.example_app_user_state_view);
        logoutButton = findViewById(R.id.example_app_logout_button);

        sdkVersion.setText(BuildConfig.VERSION_NAME + " - " + BuildConfig.BUILD_TYPE.toUpperCase(Locale.getDefault()));
        userState.setText(getString(R.string.example_app_user_logout));
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //We want to intentionally logout the user
                user.logout(new ResultCallback<NoValue>() {
                    @Override
                    public void onSuccess(NoValue res) {
                        userState.setText(getString(R.string.example_app_user_logout));
                        logoutButton.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError(@NonNull ClientError error) {
                        Toast.makeText(MainActivity.this, error.getErrorType().toString(), Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        //User can be sent through an Intent coming from a deeplink
        user = getIntent().getParcelableExtra(BaseLoginActivity.EXTRA_USER);

        if (user != null) {
            logoutButton.setVisibility(View.VISIBLE);
            userState.setText(R.string.example_app_user_logged_in);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PASSWORD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // when the flow was performed without any issue, you can get the newly created user.
                user = data.getParcelableExtra(BaseLoginActivity.EXTRA_USER);

                userState.setText(getString(R.string.example_app_user_logged_in, user.getUserId().getId()));
                logoutButton.setVisibility(View.VISIBLE);
            } else if (resultCode == SmartlockImpl.SMARTLOCK_FAILED) {
                startActivityForResult(data, PASSWORD_REQUEST_CODE);
            }
        }
    }
}
