import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
    kotlin("android")
}

description = "The common module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("common-rules.pro")
    }
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))

    testImplementation("junit:junit:${Constants.Versions.JUNIT}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${Constants.Versions.KOTLINTEST_RUNNER_JUNIT5}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Constants.Versions.MOCKITO_KOTLIN}")
    testImplementation("org.slf4j:slf4j-simple:${Constants.Versions.SLF4J}")

    androidTestImplementation("androidx.test:runner:${Constants.Versions.TEST_RUNNER}")
    androidTestImplementation("androidx.test.ext:junit:1.1.1")
}

publishing {
    publications.mavenJar {
        afterEvaluate {
            artifactId = "account-sdk-android-common"
            pom {
                name.set("Schibsted Account SDK Common Module")
            }
        }
    }
}
