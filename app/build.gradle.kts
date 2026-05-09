import java.util.Properties

plugins {
    id("com.android.application")
}

android {
    namespace = "com.example.usmentz"
    compileSdk = 34

    // Load API keys from properties file
    val localFile = file("api-keys.properties")
    val apiKeys = Properties()
    if (localFile.exists()) {
        localFile.inputStream().use { apiKeys.load(it) }
    }

    defaultConfig {
        applicationId = "com.example.usmentz"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        // Inject API key into manifest
        manifestPlaceholders["MAPS_API_KEY"] = apiKeys.getProperty("MAPS_API_KEY", "")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")

    // Google Maps
    implementation("com.google.android.gms:play-services-maps:18.2.0")

    // Charts
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}