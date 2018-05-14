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
import android.widget.Toast;

import com.schibsted.account.AccountService;
import com.schibsted.account.Events;
import com.schibsted.account.engine.integration.ResultCallback;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.network.response.ProfileData;
import com.schibsted.account.session.User;
import com.schibsted.account.ui.AccountUi;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.smartlock.SmartlockImpl;
import com.schibsted.account.ui.smartlock.SmartlockMode;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    final static int PASSWORD_REQUEST_CODE = 1;

    private User user;
    private TextView userState;
    private Button button;

    private AccountSdkReceiver accountSdkReceiver;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userState = findViewById(R.id.example_app_user_state_view);
        button = findViewById(R.id.example_app_button);

        final TextView sdkVersion = findViewById(R.id.example_app_sdk_version_view);
        sdkVersion.setText(BuildConfig.VERSION_NAME + " - " + BuildConfig.BUILD_TYPE.toUpperCase(Locale.getDefault()));

        // Bind the AccountService
        final AccountService accountService = new AccountService(getApplicationContext());
        getLifecycle().addObserver(accountService);

        // To listen for logout events (optional)
        accountSdkReceiver = new AccountSdkReceiver();

        // Attempt to resume any previous sessions, that includes sessions coming from deeplink or smartlock
        User.resumeLastSession(getApplicationContext(), new ResultCallback<User>() {
            @Override
            public void onSuccess(User result) {
                user = result;
                updateUi();
            }

            @Override
            public void onError(ClientError error) {
                user = null;
                updateUi();
            }
        });
    }

    private void updateUi() {
        if (user != null) {
            userState.setText(getString(R.string.example_app_user_logged_in, "<fetching>"));

            user.getProfile().get(new ResultCallback<ProfileData>() {
                @Override
                public void onSuccess(ProfileData result) {
                    userState.setText(getString(R.string.example_app_user_logged_in, result.getDisplayName()));
                }

                @Override
                public void onError(ClientError error) {
                    Toast.makeText(getApplicationContext(), "Failed to get profile: " + error.getMessage(), Toast.LENGTH_LONG).show();
                }
            });

            button.setText(R.string.example_app_logout);
            button.setOnClickListener(logoutListener);
        } else {
            userState.setText(getString(R.string.example_app_user_logged_out));
            button.setText(R.string.example_app_login);
            button.setOnClickListener(loginListener);
        }
    }

    private View.OnClickListener loginListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // Create the intent for the desired flow

            AccountUi.preInitialize(new ResultCallback<Void>() {
                @Override
                public void onSuccess(Void result) {
                    final Intent intent = AccountUi.getCallingIntent(getApplicationContext(), AccountUi.FlowType.PASSWORD,
                            new AccountUi.Params(getString(R.string.example_teaser_text), null, SmartlockMode.ENABLED));
                    startActivityForResult(intent, PASSWORD_REQUEST_CODE);
                }

                @Override
                public void onError(ClientError error) {
                    Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    };


    private View.OnClickListener logoutListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            // We want log out the user, but we don't care about the result as we catch this in
            // our broadcast listener
            user.logout(null);
        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(accountSdkReceiver, new IntentFilter(Events.ACTION_USER_LOGOUT));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(getApplicationContext()).unregisterReceiver(accountSdkReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PASSWORD_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // when the flow was performed without any issue, you can get the newly created user.
                user = data.getParcelableExtra(BaseLoginActivity.EXTRA_USER);
                updateUi();

            } else if (resultCode == SmartlockImpl.SMARTLOCK_FAILED) {
                //start the flow without smartlock
                AccountUi.preInitialize(new ResultCallback<Void>() {
                    @Override
                    public void onSuccess(Void result) {
                        final Intent intent = AccountUi.getCallingIntent(getApplicationContext(), AccountUi.FlowType.PASSWORD,
                                new AccountUi.Params(getString(R.string.example_teaser_text), null, SmartlockMode.DISABLED));
                        startActivityForResult(intent, PASSWORD_REQUEST_CODE);
                    }

                    @Override
                    public void onError(ClientError error) {
                        Toast.makeText(MainActivity.this, error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    private class AccountSdkReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null && action.equals(Events.ACTION_USER_LOGOUT)) {
                user = null;
                updateUi();
            }
        }
    }
}
