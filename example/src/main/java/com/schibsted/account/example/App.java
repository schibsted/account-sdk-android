package com.schibsted.account.example;

import android.app.Application;
import android.support.annotation.NonNull;

import com.schibsted.account.common.util.Logger;
import com.schibsted.account.session.User;
import com.schibsted.account.ui.AccountUiHook;

public class App extends Application implements AccountUiHook {
    @Override
    public void onLoginCompleted(@NonNull User user, @NonNull OnProceedListener onProceedListener) {
        Logger.debug("IU can see the UIs are closing! " + user.getUserId().getId());
        onProceedListener.proceed();
    }

    @Override
    public void onLoginAborted(OnProceedListener onProceedListener) {
        onProceedListener.proceed();
    }
}
