import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
    kotlin("android")
    kotlin("android.extensions")
}

description = "The UI module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("ui-rules.pro")
        resValue("string", "schacc_redirect_scheme", "")
        resValue("string", "schacc_redirect_host", "")
        testProguardFile("ui-rules.pro")
        vectorDrawables.useSupportLibrary = true
    }
    resourcePrefix("schacc_")
    useLibrary("android.test.mock")
    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
    }
}

dependencies {
    implementation(kotlin("stdlib-jdk7", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))

    api(project(":core"))
    compileOnly(project(":smartlock"))

    implementation("com.android.support:support-annotations:${Constants.Versions.SUPPORT}")
    implementation("com.android.support:design:${Constants.Versions.SUPPORT}")
    implementation("com.android.support.constraint:constraint-layout:${Constants.Versions.CONSTRAINT_LAYOUT}")
    implementation("android.arch.lifecycle:extensions:${Constants.Versions.LIFECYCLE}")

    testImplementation("org.assertj:assertj-core:${Constants.Versions.ASSERTJ_CORE}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Constants.Versions.MOCKITO_KOTLIN}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${Constants.Versions.KOTLINTEST_RUNNER_JUNIT5}") {
        exclude(group="org.jetbrains.kotlin")
    }
    testImplementation("junit:junit:${Constants.Versions.JUNIT}")
    testImplementation(kotlin("test-junit", KotlinCompilerVersion.VERSION))
    testImplementation("org.slf4j:slf4j-simple:${Constants.Versions.SLF4J}")
}

publishing {
    publications.mavenJar {
        afterEvaluate {
            artifactId = "account-sdk-android-ui"
            pom {
                name.set("Schibsted Account SDK UI Module")
            }
        }
    }

    bintray {
        pkg.apply {
            name = "UI"
            version.apply {
                desc = "Account SDK Android UI ${project.version}"
            }
        }
    }
}
