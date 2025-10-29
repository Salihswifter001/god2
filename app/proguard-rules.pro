# ProGuard/R8 G�venlik ve Optimizasyon Kurallar1

# Temel Android kurallar1
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider

# OctaApplication - MUTLAKA KORUNMALI
-keep class com.aihackathonkarisacikartim.god2.OctaApplication { *; }
-keepclassmembers class com.aihackathonkarisacikartim.god2.OctaApplication { *; }

# Jetpack Compose
-keep class androidx.compose.** { *; }
-keep class androidx.compose.ui.** { *; }
-dontwarn androidx.compose.**
-keep class androidx.compose.runtime.** { *; }

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}

# Kotlin Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.aihackathonkarisacikartim.god2.**$$serializer { *; }
-keepclassmembers class com.aihackathonkarisacikartim.god2.** {
    *** Companion;
}
-keepclasseswithmembers class com.aihackathonkarisacikartim.god2.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Supabase
-keep class io.github.jan.supabase.** { *; }
-keep class io.ktor.** { *; }
-dontwarn io.ktor.**
-keep class io.github.jan.supabase.gotrue.** { *; }
-keep class io.github.jan.supabase.postgrest.** { *; }
-keep class io.github.jan.supabase.storage.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ExoPlayer/Media3
-keep class androidx.media3.** { *; }
-keep interface androidx.media3.** { *; }
-dontwarn androidx.media3.**

# Coil
-keep class coil.** { *; }
-dontwarn coil.**

# Navigation
-keep class androidx.navigation.** { *; }
-keepnames class androidx.navigation.fragment.NavHostFragment

# DataStore
-keep class androidx.datastore.*.** {*;}

# EncryptedSharedPreferences
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Uygulama s1n1flar1 - �nemli s1n1flar1 koru
-keep class com.aihackathonkarisacikartim.god2.MainActivity { *; }
-keep class com.aihackathonkarisacikartim.god2.OctaApplication { *; }
-keep class com.aihackathonkarisacikartim.god2.BuildConfig { *; }

# Data s1n1flar1
-keep class com.aihackathonkarisacikartim.god2.GeneratedMusicData { *; }
-keep class com.aihackathonkarisacikartim.god2.UserDetails { *; }
-keep class com.musicApi.MusicApiResponse { *; }
-keep class com.musicApi.MusicApiData { *; }

# G�venlik s1n1flar1 - Obfuscate edilmemeli
-keep class com.security.** { *; }
-keep class com.utils.ValidationUtils { *; }

# ViewModel'ler
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    <init>(android.app.Application);
}

# G�venlik i�in hassas bilgileri loglardan kald1r
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int i(...);
    public static int w(...);
    public static int d(...);
    public static int e(...);
}

# System.out.println'leri kald1r
-assumenosideeffects class java.io.PrintStream {
    public void println(...);
    public void print(...);
}