plugins {
    alias(libs.plugins.android.application)
    // Firebase
    id("com.google.gms.google-services")
}

android {
    namespace = "com.teatrack_mcd_253eie502802_group02"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.teatrack_mcd_253eie502802_group02"
        minSdk = 26
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.swiperefreshlayout)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.viewpager2:viewpager2:1.0.0")
    implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    implementation("com.google.firebase:firebase-database")
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Firebase BOM
        implementation(platform("com.google.firebase:firebase-bom:34.14.1"))
    // Firebase Authentication
    implementation("com.google.firebase:firebase-auth")
    // Firebase Realtime Database
    implementation("com.google.firebase:firebase-database:21.0.0")
    // Firebase Storage
    implementation("com.google.firebase:firebase-storage")
    // Firebase Analytics (không bắt buộc)
    implementation("com.google.firebase:firebase-analytics")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Glide
    implementation(libs.glide)
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Chart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")
    // Glide (load ảnh sản phẩm từ Firebase Storage)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    // CircleImageView (avatar topbar)
    implementation("de.hdodenhof:circleimageview:3.1.0")

    // Cloudinary
    implementation("com.cloudinary:cloudinary-android:3.1.2")

    // Gemini AI
    implementation(libs.generativeai)

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    // Firebase Storage
    implementation("com.google.firebase:firebase-storage:20.3.0")
// Glide - hiển thị ảnh từ URL
    implementation("com.github.bumptech.glide:glide:4.16.0")
// CircleImageView (tùy chọn, đẹp hơn)
    implementation("de.hdodenhof:circleimageview:3.1.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")

    // ZXing for QR and Barcode
    implementation("com.google.zxing:core:3.5.3")
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")
}
