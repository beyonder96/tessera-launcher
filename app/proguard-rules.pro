# Tessera Launcher — Proguard & R8 Optimization Rules

# Manter classes base do Android e componentes declarados no Manifest
-keep public class com.tessera.launcher.TesseraApplication
-keep public class com.tessera.launcher.MainActivity
-keep public class com.tessera.launcher.ui.activity.AddPinShortcutActivity
-keep class com.tessera.launcher.data.receiver.** { *; }
-keep class com.tessera.launcher.data.service.** { *; }
-keep class com.tessera.launcher.data.helper.TesseraCrashHandler { *; }

# Licenciamento offline e integridade criptográfica
-keep class com.tessera.launcher.data.license.** { *; }

# BuildConfig do aplicativo
-keep class com.tessera.launcher.BuildConfig { *; }

# Suporte à anotação @Keep
-keep @androidx.annotation.Keep class * { *; }
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <methods>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <fields>;
}
-keepclasseswithmembers class * {
    @androidx.annotation.Keep <init>(...);
}

# Kotlin Serialization
-keepattributes *Annotation*,InnerClasses,Signature,EnclosingMethod
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class * implements kotlinx.serialization.KSerializer {
    <fields>;
}

# Parcelable e Enums
-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Otimização de logs de debug em builds release
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}
