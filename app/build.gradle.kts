plugins {
    id("com.android.application")
    kotlin("android")
    id("com.google.android.libraries.mapsplatform.secrets-gradle-plugin")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("kotlin-parcelize")
}

android {
    compileSdk = libs.versions.compile.sdk.version.get().toInt()

    defaultConfig {
        minSdk = libs.versions.min.sdk.version.get().toInt()
        targetSdk = libs.versions.target.sdk.version.get().toInt()

        applicationId = AppCoordinates.APP_ID
        versionCode = AppCoordinates.APP_VERSION_CODE
        versionName = AppCoordinates.APP_VERSION_NAME
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    lint {
        warningsAsErrors = true
        abortOnError = true
    }

    // Use this block to configure different flavors
//    flavorDimensions("version")
//    productFlavors {
//        create("full") {
//            dimension = "version"
//            applicationIdSuffix = ".full"
//        }
//        create("demo") {
//            dimension = "version"
//            applicationIdSuffix = ".demo"
//        }
//    }
}

dependencies {
    implementation(fileTree("libs"))

    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.constraint.layout)
    implementation(libs.androidx.core.ktx)

    implementation(libs.amazon.s3)

    implementation(libs.google.gson)
    implementation(libs.google.material)
    implementation(libs.google.exoplayer)

    implementation(platform(libs.google.firebase.bom))
    implementation(libs.google.firebase.crashlytics.ktx)
    implementation(libs.google.firebase.analytics.ktx)
    implementation(libs.google.firebase.auth.ktx)

    implementation(libs.other.lottie)
    implementation(libs.other.okhttp)
    implementation(libs.other.picasso)
    implementation(libs.other.avatarview)
    implementation(libs.other.retrofit)
    implementation(libs.other.retrofit.gson)

    testImplementation(libs.junit)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.ext.junit.ktx)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.core)
}
