# R8 için Kotlin Reflect kütüphanesi özel kuralları

# Tüm sınıfı koru ama çakışan versiyonu dışla
-keep class kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.BuiltInsResourceLoader { *; }
-dontwarn kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.BuiltInsResourceLoader
-dontwarn META-INF.versions.9.kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.BuiltInsResourceLoader

# Java 9+ altındaki tüm sınıflar için uyarıları kapat
-dontwarn META-INF.versions.**

# R8 optimizasyonunu çakışan sınıflar için kapat
-keepclasseswithmembers class kotlin.reflect.jvm.internal.impl.** { *; }

# Kotlin reflection için temel keep kuralları
-keepnames class kotlin.reflect.jvm.internal.impl.** { *; }
-keep class kotlin.Metadata { *; }
-keep class kotlin.reflect.** { *; }

# R8 sınıf birleştirmesini devre dışı bırakmak için
-optimizations !class/merging/horizontal,!class/merging/vertical

# Harici attributeleri koru
-keepattributes *Annotation*
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeInvisibleAnnotations
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses

# R sınıfları için özel kurallar
-keep class **.R$* { *; }
-keepclassmembers class **.R$* { public static <fields>; } 