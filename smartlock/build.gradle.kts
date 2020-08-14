import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
    kotlin("android")
}

description = "The Smartlock module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("smartlock-rules.pro")
    }
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))

    implementation(project(":common"))
    implementation(project(":core"))
    implementation("androidx.appcompat:appcompat:${Constants.Versions.ANDROIDX}")
    implementation("com.google.android.gms:play-services-auth:${Constants.Versions.PLAY_SERVICES_AUTH}") {
        exclude(group="com.android.support")
    }
}

publishing {
    publications.mavenJar {
        afterEvaluate {
            artifactId = "account-sdk-android-smartlock"
            pom {
                name.set("Schibsted Account SDK Smartlock Module")
            }
        }
    }

    bintray {
        pkg.apply {
            name = "Smartlock"
            version.apply {
                desc = "Account SDK Android Smartlock ${project.version}"
            }
        }
    }
}
