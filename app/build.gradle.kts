plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.iip_ld"
    compileSdk {
        version = release(36) {
            minorApiLevel = 1
        }
    }

    flavorDimensions += "lab"

    productFlavors {
        create("ld2") {
            dimension = "lab"
            applicationIdSuffix = ".ld2"
        }
        create("ld3") {
            dimension = "lab"
            applicationIdSuffix = ".ld3"
        }
    }

    sourceSets {
        getByName("ld2").setRoot("src/ld2/main")
        getByName("testLd2").setRoot("src/ld2/test")
        getByName("androidTestLd2").setRoot("src/ld2/androidTest")

        getByName("ld3").setRoot("src/ld3/main")
        getByName("testLd3").setRoot("src/ld3/test")
        getByName("androidTestLd3").setRoot("src/ld3/androidTest")
    }

    defaultConfig {
        applicationId = "com.example.iip_ld"
        minSdk = 24
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
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
