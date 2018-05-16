package com.schibsted.account.example;

import android.app.Application;
import android.support.annotation.NonNull;

import com.schibsted.account.common.util.Logger;
import com.schibsted.account.session.User;
import com.schibsted.account.ui.AccountUiHook;
import com.schibsted.account.ui.OptionalConfiguration;

import java.util.Locale;

public class App extends Application implements AccountUiHook, OptionalConfiguration.UiConfigProvider {
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
    public OptionalConfiguration getUiConfig() {
        return new OptionalConfiguration.Builder()
                .locale(new Locale("nb", "NO"))
                .clientLogo(R.drawable.ic_example_logo)
                .build();
    }
}
