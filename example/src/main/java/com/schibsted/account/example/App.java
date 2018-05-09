package com.schibsted.account.example;

import android.app.Application;
import android.support.annotation.NonNull;

import com.schibsted.account.common.util.Logger;
import com.schibsted.account.session.User;
import com.schibsted.account.ui.AccountUiHook;
import com.schibsted.account.ui.UiConfig;

import java.util.Locale;

public class App extends Application implements AccountUiHook, UiConfig.UiConfigProvider {
    @Override
    public void onLoginCompleted(@NonNull User user, @NonNull OnProceedListener onProceedListener) {
        Logger.debug("XXX", "IU can see the UIs are closing! " + user.getUserId().getId());
        onProceedListener.proceed();
    }

    @Override
    public void onLoginAborted(OnProceedListener onProceedListener) {
        onProceedListener.proceed();
    }

    @NonNull
    @Override
    public UiConfig getUiConfig() {
        return new UiConfig.Builder()
                .locale(new Locale("nb", "NO"))
                .clientLogo(R.drawable.ic_example_logo)
                .build();
    }
}
