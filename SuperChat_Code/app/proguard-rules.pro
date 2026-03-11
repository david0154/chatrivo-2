# ============================================================
# Chatrivo ProGuard Rules
# Package: com.nexuzy.chatrivo
# ============================================================

# Keep application class
-keep class com.nexuzy.chatrivo.** { *; }
-keep class com.teamxdevelopers.SuperChat.** { *; }

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.firebase.**

# Realm
-keep class io.realm.** { *; }
-dontwarn io.realm.**

# Retrofit / OkHttp
-dontwarn okhttp3.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes Exceptions

# Glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule { <init>(...); }
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Agora
-keep class io.agora.** { *; }
-dontwarn io.agora.**

# EventBus
-keepattributes *Annotation*
-keepclassmembers class ** {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }

# ML Kit
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

# Virgil Security
-keep class com.virgilsecurity.** { *; }
-dontwarn com.virgilsecurity.**

# Lottie
-dontwarn com.airbnb.lottie.**
-keep class com.airbnb.lottie.** { *; }

# General
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
