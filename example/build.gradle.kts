plugins {
    id("com.android.application")
}

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(28)

    defaultConfig {
        applicationId = "com.schibsted.account.example"
        minSdkVersion(14)
        targetSdkVersion(28)
        versionName = project.version.toString()
        versionCode = 1
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        missingDimensionStrategy("default", "singledex", "multidex")

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
    implementation("com.android.support:appcompat-v7:28.0.0")
    implementation("com.android.support.constraint:constraint-layout:1.0.2")
    implementation(project(":ui"))
    implementation(project(":smartlock"))
}
