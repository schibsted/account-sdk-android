-printusage
-keepparameternames
-dontnote sun.misc.Unsafe
-keepattributes Exceptions,InnerClasses,Signature,Deprecated,EnclosingMethod

# For View sub-classes with default style parameter
-keepclasseswithmembers class * { public <init>(android.content.Context, android.util.AttributeSet, int, int);}
-keepclasseswithmembernames class * { public <init>(android.content.Context, android.util.AttributeSet); }
-keepclasseswithmembernames class * { public <init>(android.content.Context, android.util.AttributeSet, int); }
-keepclasseswithmembernames class * { native <methods>; }

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
    *;
}

## Android architecture components: Lifecycle
-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver { <init>(...); }
-keepclassmembers class * extends android.arch.lifecycle.ViewModel { <init>(...); }
-keepclassmembers class android.arch.lifecycle.Lifecycle$State { *; }
-keepclassmembers class android.arch.lifecycle.Lifecycle$Event { *; }
-keepclassmembers class * { @android.arch.lifecycle.OnLifecycleEvent *; }
-keepclassmembers class * implements android.arch.lifecycle.LifecycleObserver { <init>(...); }
-keep class * implements android.arch.lifecycle.LifecycleObserver { <init>(...); }
-keepclassmembers class android.arch.** { *; }
-keep class android.arch.** { *; }
-dontwarn android.arch.**

#Account SDK
-keep class com.schibsted.account.ui.login.flow.password.PasswordActivity { public *;}
-keep class com.schibsted.account.ui.login.flow.password.PasswordActivity$** { *; }
-keep class com.schibsted.account.ui.login.flow.passwordless.PasswordlessActivity { public *;}
-keep public class com.schibsted.account.ui.login.flow.passwordless.PasswordlessActivity$** { *; }
-keep public class com.schibsted.account.ui.login.BaseLoginActivity$** { *; }

-keep class com.schibsted.account.ui.AccountUi$** { *;}
-keep class com.schibsted.account.ui.AccountUi { *;}

-keep class com.schibsted.account.ui.login.screen.information.RequiredFields { *; }
-keep class com.schibsted.account.ui.ui.rule.** { *; }
-keep class com.schibsted.account.ui.smartlock.SmartlockMode{ *; }
-dontnote  com.schibsted.account.ui.login.flow.**
-dontnote  com.schibsted.account.ui.ui.component.**
-dontwarn com.schibsted.account.smartlock.SmartlockController
