import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
    kotlin("android.extensions")
}

description = "The core module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("core-rules.pro")
    }
    useLibrary("android.test.mock")
}

androidExtensions {
    isExperimental = true
}

dependencies {
    api(project(":common"))

    api("com.squareup.retrofit2:converter-gson:${Constants.Versions.RETROFIT}")
    implementation("com.squareup.retrofit2:retrofit:${Constants.Versions.RETROFIT}")
    implementation("com.android.support:support-annotations:${Constants.Versions.SUPPORT}")
    implementation("com.android.support:support-core-utils:${Constants.Versions.SUPPORT}")
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))

    testImplementation("junit:junit:${Constants.Versions.JUNIT}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${Constants.Versions.KOTLINTEST_RUNNER_JUNIT5}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Constants.Versions.MOCKITO_KOTLIN}")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${Constants.Versions.KOTLINX_COROUTINES_CORE}")
    testImplementation("org.slf4j:slf4j-simple:${Constants.Versions.SLF4J}")
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
