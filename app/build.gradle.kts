plugins {
    alias(libs.plugins.android.application)
   // id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.stv2"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.stv2"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.3.0"))
    implementation(libs.appcompat)
    implementation ("com.google.firebase:firebase-firestore")
    implementation ("com.google.firebase:firebase-auth") // ha auth-ot is használsz
    implementation ("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.16.0")
    implementation ("com.google.firebase:firebase-storage:21.3.0")
    implementation ("com.google.firebase:firebase-storage")
    implementation(libs.material)
    implementation ("com.google.android.material:material:1.9.0")
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.database)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}