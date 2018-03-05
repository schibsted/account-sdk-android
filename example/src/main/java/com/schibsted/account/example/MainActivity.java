package com.schibsted.account.example;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.schibsted.account.engine.integration.ResultCallback;
import com.schibsted.account.model.UserId;
import com.schibsted.account.model.error.ClientError;
import com.schibsted.account.persistence.UserPersistence;
import com.schibsted.account.session.User;
import com.schibsted.account.session.event.EventManager;
import com.schibsted.account.ui.UiConfiguration;
import com.schibsted.account.ui.login.BaseLoginActivity;
import com.schibsted.account.ui.login.flow.password.PasswordActivity;

import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    final static int PASSWORD_REQUEST_CODE = 1;
    private User user;
    private UserPersistence userPersistence;
    private EventManager eventManager;
    private IdentityReceiver identityReceiver;
    private TextView userState;
    private Button logoutButton;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //_____________________IDENTITY SDK INIT__________________

        // Get the UiConfiguration
        final UiConfiguration uiConfiguration = UiConfiguration.Builder.fromManifest(getApplicationContext())
                .enableSignUp()
                .teaserText(getString(R.string.example_teaser_text))
                .build();

        // Create the intent for the desired flow
        final Intent passwordIntent = PasswordActivity.getCallingIntent(this, uiConfiguration);

        // We want to manage user persistence
        userPersistence = new UserPersistence(getApplicationContext());

        // We want to listen to identity events
        eventManager = new EventManager(getApplicationContext());
        identityReceiver = new IdentityReceiver();

        // Start the flow
        startActivityForResult(passwordIntent, PASSWORD_REQUEST_CODE);

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
                user.logout(new ResultCallback() {
                    @Override
                    public void onSuccess() {
                        //we remove the user from persistence
                        userPersistence.remove(user.getUserId().getId());

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
            //persist the user if it was found
            userPersistence.persist(user);
        } else {
            // if not try to get the user from the storage
            user = userPersistence.resumeLast();
        }

        if (user != null) {
            logoutButton.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        eventManager.registerReceiver(identityReceiver, new IntentFilter(EventManager.LOGOUT_EVENT_ID));
    }

    @Override
    protected void onStop() {
        super.onStop();
        eventManager.unregisterReceiver(identityReceiver);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PASSWORD_REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            // when the flow was performed without any issue, you can get the newly created user.
            user = data.getParcelableExtra(BaseLoginActivity.EXTRA_USER);
            // Persist the user if possible
            persistUser();

            userState.setText(getString(R.string.example_app_user_logged_in, user.getUserId().getId()));
            logoutButton.setVisibility(View.VISIBLE);
        }
    }

    private boolean persistUser() {
        if (user.isPersistable()) {
            userPersistence.persist(user);
            return true;
        }
        return false;
    }

    private class IdentityReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                switch (action) {
                    //We want to know if the user was logged out by the sdk
                    case EventManager.LOGOUT_EVENT_ID: {
                        final UserId userId = intent.getParcelableExtra(EventManager.EXTRA_USER_ID);
                        Log.d("IdentityReceiver", "User " + userId.getId() + "was logged out");
                        // remove user from persistence, update view state...
                    }
                    //We want to know when the user token is refreshed
                    case EventManager.REFRESH_EVENT_ID: {
                        final UserId userId = intent.getParcelableExtra(EventManager.EXTRA_USER_ID);
                        Log.d("IdentityReceiver", "User " + userId.getId() + "token was refreshed");

                        persistUser();
                    }
                    default:
                        Log.e("IdentityReceiver", "Can't handle this event");
                }
            }
        }
    }

}
