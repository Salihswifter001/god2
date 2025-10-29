# Kotlin Reflect kütüphanesi çakışmaları için özel kurallar

# META-INF/versions/9 içindeki sınıfları dışla
-dontwarn kotlin.reflect.jvm.internal.impl.serialization.deserialization.builtins.BuiltInsResourceLoader

# Kotlin Reflect için genel kurallar
-keep class kotlin.reflect.** { *; }
-keep class kotlin.reflect.jvm.** { *; }
-keep class kotlin.reflect.jvm.internal.** { *; }
-keep class kotlin.reflect.jvm.internal.impl.** { *; }

# Serialization için
-keepattributes SourceFile,LineNumberTable,*Annotation*,EnclosingMethod,Signature,Exceptions,InnerClasses

# R8 full mode kapatma
-optimizationpasses 1
-dontoptimize
-dontpreverify

# Multi-Release JAR sorunları için
-keeppackagenames kotlin.reflect.** 