-printusage

-keep class com.schibsted.account.network.response.* { *; }
-keep class com.schibsted.account.network.response.*$** { *; }
-keep class com.schibsted.account.persistence.UserPersistence.Session { *; }

-keepnames class android.support.v4.content.LocalBroadcastManager

-dontwarn com.schibsted.account.util.DeepLinkHandler$loginFromDeepLink$2$3
