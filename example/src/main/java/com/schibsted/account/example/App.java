package com.schibsted.account.example;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.schibsted.account.session.User;
import com.schibsted.account.ui.AccountUiHook;

public class App extends Application implements AccountUiHook {
    @Override
    public void onUiClosing(@NonNull User user, @NonNull OnProceedListener onProceedListener) {
        Log.d("XXX", "IU can see the UIs are closing! " + user.getUserId().getId());
        onProceedListener.proceed();
    }
}
