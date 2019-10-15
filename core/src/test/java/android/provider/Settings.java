package android.provider;

import android.content.ContentResolver;

public final class Settings {
    public static final class Secure {
        public static String getString(ContentResolver resolver, String name) {
            return "secureString-" + name;
        }
    }
}
