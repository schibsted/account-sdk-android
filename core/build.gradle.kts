import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
    kotlin("android")
    kotlin("android.extensions")
}

description = "The core module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("core-rules.pro")
    }
    useLibrary("android.test.mock")
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

androidExtensions {
    isExperimental = true
}

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))

    api(project(":common"))

    api("com.squareup.retrofit2:converter-gson:${Constants.Versions.RETROFIT}")
    implementation("com.squareup.retrofit2:retrofit:${Constants.Versions.RETROFIT}")
    implementation("com.squareup.okhttp3:okhttp:${Constants.Versions.OKHTTP}")
    implementation("androidx.annotation:annotation:${Constants.Versions.ANDROIDX}")
    implementation("androidx.localbroadcastmanager:localbroadcastmanager:${Constants.Versions.ANDROIDX}")
    implementation("androidx.lifecycle:lifecycle-extensions:${Constants.Versions.LIFECYCLE}")

    testImplementation("junit:junit:${Constants.Versions.JUNIT}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${Constants.Versions.KOTLINTEST_RUNNER_JUNIT5}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Constants.Versions.MOCKITO_KOTLIN}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Constants.Versions.KOTLINX_COROUTINES_CORE}")
    testImplementation("org.slf4j:slf4j-simple:${Constants.Versions.SLF4J}")

    androidTestImplementation("androidx.test:runner:${Constants.Versions.TEST_RUNNER}")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
    androidTestImplementation("org.mockito:mockito-android:${Constants.Versions.MOCKITO_ANDROID}")
    androidTestImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Constants.Versions.MOCKITO_KOTLIN}")
}

publishing {
    publications.mavenJar {
        afterEvaluate {
            artifactId = "account-sdk-android-core"
            pom {
                name.set("Schibsted Account SDK Core Module")
            }
        }
    }

    bintray {
        pkg.apply {
            name = "Core"
            version.apply {
                desc = "Account SDK Android Core ${project.version}"
            }
        }
    }
}
