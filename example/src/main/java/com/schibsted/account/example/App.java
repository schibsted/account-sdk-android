package com.schibsted.account.example;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

import com.schibsted.account.session.User;
import com.schibsted.account.ui.UiHoks;

public class App extends Application implements UiHoks {
    @Override
    public void onUiClosing(@NonNull User user, @NonNull OnReadyListener onReadyListener) {
        Log.d("XXX", "IU can see the UIs are closing! " + user.getUserId().getId());
        onReadyListener.onReady();
    }
}
