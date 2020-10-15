plugins {
    id("com.android.application")
}

repositories {
    google()
    jcenter()
    mavenCentral()
}

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "com.schibsted.account.example"
        minSdkVersion(21)
        targetSdkVersion(28)
        versionName = project.version.toString()
        versionCode = 1
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        resValue("string", "schacc_conf_redirect_scheme",
                findProperty("schacc_redirect_scheme")?.toString() ?: "spid-dummyscheme")
    }

    lintOptions {
        isAbortOnError = false
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs["debug"]
        }
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:1.1.3")
    implementation("androidx.browser:browser:1.0.0")
    implementation(project(":ui"))
    implementation(project(":smartlock"))
}
