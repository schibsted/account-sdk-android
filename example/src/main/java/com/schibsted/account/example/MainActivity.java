/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.schibsted.account.AccountService;
import com.schibsted.account.Events;
import com.schibsted.account.session.User;
import com.schibsted.account.ui.UiConfiguration;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.login.flow.password.PasswordActivity;
import com.schibsted.account.ui.smartlock.SmartlockImpl;

import java.util.Locale;

import static com.schibsted.account.ui.login.BaseLoginActivity.EXTRA_USER;

public class MainActivity extends AppCompatActivity {
    final static int PASSWORD_REQUEST_CODE = 1;

    private User user;
    private TextView userState;
    private Button logoutButton;
    private LocalBroadcastManager localBroadcastManager;
    private AccountSdkReceiver accountSdkReceiver;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        userState = findViewById(R.id.example_app_user_state_view);
        logoutButton = findViewById(R.id.example_app_logout_button);

        final AccountService accountService = new AccountService(getApplicationContext());
        getLifecycle().addObserver(accountService);


        // Build the UiConfiguration
        final UiConfiguration uiConfiguration = UiConfiguration.Builder.fromManifest(getApplicationContext())
                .enableSignUp()
                .logo(R.drawable.ic_example_logo)
                .locale(new Locale("nb", "NO"))
                .teaserText(getString(R.string.example_teaser_text))
                .build();

        // Create the intent for the desired flow
        final Intent passwordIntent = PasswordActivity.getCallingIntent(this, uiConfiguration);

        //To listen for logout events
        localBroadcastManager = LocalBroadcastManager.getInstance(this);
        accountSdkReceiver = new AccountSdkReceiver();

        //User can be sent through an Intent coming from a deeplink or smartlock
        if (getIntent().hasExtra(EXTRA_USER)) {
            user = getIntent().getParcelableExtra(BaseLoginActivity.EXTRA_USER);
            logoutButton.setVisibility(View.VISIBLE);
            userState.setText(getString(R.string.example_app_user_logged_in, user.getUserId().getId()));
        } else if (savedInstanceState == null) {
            //if the flow wasn't already started, start it.
            startActivityForResult(passwordIntent, PASSWORD_REQUEST_CODE);
        }


        final TextView sdkVersion = findViewById(R.id.example_app_sdk_version_view);
        sdkVersion.setText(BuildConfig.VERSION_NAME + " - " + BuildConfig.BUILD_TYPE.toUpperCase(Locale.getDefault()));
        userState.setText(getString(R.string.example_app_user_logout));
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                user.logout(null);
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        localBroadcastManager.registerReceiver(accountSdkReceiver, new IntentFilter(Events.ACTION_USER_LOGOUT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        localBroadcastManager.unregisterReceiver(accountSdkReceiver);
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

    private class AccountSdkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(Events.ACTION_USER_LOGOUT)) {
                userState.setText(getString(R.string.example_app_user_logout));
                logoutButton.setVisibility(View.GONE);
            }
        }
    }
}
