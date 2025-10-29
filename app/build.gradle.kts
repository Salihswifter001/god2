plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.0"
}

android {
    namespace = "com.aihackathonkarisacikartim.god2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aihackathonkarisacikartim.god2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        multiDexEnabled = true
    }

    signingConfigs {
        // signingConfigs {
        //     create("release") {
        //         storeFile = file("../keystore/release.keystore")
        //         storePassword = "android"
        //         keyAlias = "androidkey"
        //         keyPassword = "android"
        //     }
        //     
        //     getByName("debug") {
        //         storeFile = file("debug.keystore")
        //         storePassword = "android"
        //         keyAlias = "androiddebugkey"
        //         keyPassword = "android"
        //     }
        // }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            buildConfigField("Boolean", "IS_RELEASE", "true")
        }
        debug {
            isMinifyEnabled = false
            buildConfigField("Boolean", "IS_RELEASE", "false")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
        isCoreLibraryDesugaringEnabled = true
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
            excludes += "META-INF/versions/9/**"
            excludes += "kotlin/reflect/jvm/internal/impl/serialization/deserialization/builtins/BuiltInsResourceLoader.class"
            
            pickFirsts.clear()
            pickFirsts.add("kotlin/reflect/jvm/internal/impl/serialization/deserialization/builtins/BuiltInsResourceLoader.class")
        }
        jniLibs {
            useLegacyPackaging = true
        }
    }
    aaptOptions {
        noCompress("jar", "zip", "apk")
    }
}

// Çakışan sınıfları yeniden paketlemek için JarJar yapılandırması
configurations {
    create("jarjar")
}

dependencies {
    "jarjar"("com.googlecode.jarjar:jarjar:1.3")
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs:2.0.3")
    
    // Reflection exclude kaldırıldı
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.9.0") 
    
    // JAR'ı doğrudan kullan - ama önce fixReflectionJars görevini çalıştır
    implementation(fileTree(mapOf("dir" to "${layout.buildDirectory.get()}/libs", "include" to listOf("*.jar"))))
    
    implementation("androidx.compose.ui:ui:1.5.4")
    implementation("androidx.compose.material3:material3:1.1.2")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.4")
    implementation("androidx.compose.material:material-icons-extended:1.5.4")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.compose.ui:ui-text:1.5.4")
    
    // TextFieldValue ve TextRange için ek bağımlılıklar
    implementation("androidx.compose.foundation:foundation:1.5.4")
    implementation("androidx.compose.ui:ui-text-core:1.5.4")
    
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Supabase bağımlılıkları - exclude'lar kaldırıldı
    implementation("io.github.jan-tennert.supabase:postgrest-kt:1.4.1")
    implementation("io.github.jan-tennert.supabase:gotrue-kt:1.4.1")
    implementation("io.github.jan-tennert.supabase:storage-kt:1.4.1")
    implementation("io.github.jan-tennert.supabase:supabase-kt:1.4.1")
    implementation("io.ktor:ktor-client-android:2.3.3")

    // Material IconsExtended for icons
    implementation("androidx.compose.material:material-icons-extended:1.5.4")

    // Kotlinx serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.5.1")
    
    // Glide for image loading (Güncellendi)
    implementation("com.github.bumptech.glide:glide:4.15.1")
    
    // Kotlinx coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.0.0")
    
    // AppCompat for locale management
    implementation("androidx.appcompat:appcompat:1.6.1")
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-Xskip-metadata-version-check",
            "-Xskip-prerelease-check",
            "-Xsuppress-version-warnings"
        )
    }
}

configurations.all {
    resolutionStrategy {
        // Sadece force kuralları kaldı
        force("org.jetbrains.kotlin:kotlin-reflect:1.9.0")
        force("org.jetbrains.kotlin:kotlin-stdlib:1.9.0")
        // Genel exclude kuralı kaldırıldı
    }
    // Tüm bağımlılıklardan kotlin-reflect'i tamamen hariç tut
    exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
}

import org.gradle.api.tasks.Copy
import org.gradle.jvm.tasks.Jar

// 1. kotlin-reflect JAR'ını META-INF/versions/9/** dışlayarak filtrele
val fixKotlinReflectJar by tasks.registering(Copy::class) {
    val reflectArtifact = configurations.runtimeClasspath.get()
        .resolvedConfiguration
        .resolvedArtifacts
        .first { it.moduleVersion.id.name == "kotlin-reflect" }
        .file
    from(zipTree(reflectArtifact))
    exclude("META-INF/versions/9/**")
    into("$buildDir/filteredKotlinReflect")
}

// 2. Filtrelenmiş dosyaları yeni bir JAR olarak paketle
val packageFixedReflect by tasks.registering(Jar::class) {
    dependsOn(fixKotlinReflectJar)
    archiveFileName.set("kotlin-reflect-fixed.jar")
    from("$buildDir/filteredKotlinReflect")
    destinationDirectory.set(file("$buildDir/libs"))
}

// 3. Orijinal kotlin-reflect'i hariç tutup, sabitlenmiş JAR'ı kullan
configurations {
    implementation {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
}

dependencies {
    implementation(files("$buildDir/libs/kotlin-reflect-fixed.jar"))
}

// 4. Prebuild adımından önce sabitlenmiş JAR görevini çalıştır
tasks.named("preBuild") {
    dependsOn(packageFixedReflect)
}