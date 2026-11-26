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
        for (lab in 2..9) {
            create("ld$lab") {
                dimension = "lab"
                applicationIdSuffix = ".ld$lab"
            }
        }
    }

    sourceSets {
        for (lab in 2..9) {
            getByName("ld$lab").setRoot("src/ld$lab/main")
            getByName("testLd$lab").setRoot("src/ld$lab/test")
            getByName("androidTestLd$lab").setRoot("src/ld$lab/androidTest")
        }
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

// Each Run entry installs its own debug variant, then opens that app.
val adbPath = androidComponents.sdkComponents.adb
val baseAppId = android.defaultConfig.applicationId
val mainActivity = "${android.namespace}.MainActivity"

for (lab in 2..9) {
    tasks.register<Exec>("runLd$lab") {
        dependsOn("installLd${lab}Debug")
        doFirst {
            commandLine(
                adbPath.get().asFile.absolutePath,
                "shell", "am", "start", "-W", "-n",
                "$baseAppId.ld$lab/$mainActivity"
            )
        }
    }
}

dependencies {
    add("ld9Implementation", "org.osmdroid:osmdroid-android:6.1.20")
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
