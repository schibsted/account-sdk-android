import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    `maven-publish`
    id("com.jfrog.bintray")
}

version = projectVersion
group = "com.schibsted.account"
description = "The UI module for the Schibsted Account SDK"

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(28)
        versionName = project.version.toString()
        consumerProguardFiles("ui-rules.pro")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"

        resValue("string", "schacc_redirect_scheme", "")
        resValue("string", "schacc_redirect_host", "")

        testProguardFile("ui-rules.pro")
        vectorDrawables.useSupportLibrary = true
    }

    flavorDimensions("default")
    productFlavors {
        create("singledex") {
            multiDexEnabled = false
        }
        create("multidex") {
            multiDexEnabled = true
        }
    }

    lintOptions {
        isAbortOnError = false
    }

    kotlinOptions {
        apiVersion = "1.3"
        languageVersion = "1.3"
    }

    dexOptions {
        javaMaxHeapSize = "4g"
    }

    resourcePrefix("schacc_")

    useLibrary("android.test.mock")
}

dependencies {
    api(project(":core"))
    compileOnly(project(":smartlock"))

    implementation("com.android.support:support-annotations:28.0.0")
    implementation("com.android.support:design:28.0.0")
    implementation("com.android.support.constraint:constraint-layout:1.0.2")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KotlinCompilerVersion.VERSION}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${KotlinCompilerVersion.VERSION}")
    implementation("android.arch.lifecycle:extensions:1.1.0")

    val multidexImplementation by configurations
    multidexImplementation("com.android.support:multidex:1.0.3")

    testImplementation("org.assertj:assertj-core:3.14.0")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.9")
    testImplementation("junit:junit:4.12")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${KotlinCompilerVersion.VERSION}")
    testImplementation("org.slf4j:slf4j-simple:1.7.26")

    androidTestImplementation("org.mockito:mockito-android:2.13.0")
    androidTestImplementation("com.schibsted.spain:barista:2.3.0") {
        exclude(group="com.android.support")
        exclude(group="org.jetbrains.kotlin")
    }
    androidTestImplementation("androidx.test.espresso:espresso-core:3.2.0")
}

tasks.withType(Test::class) {
    testLogging {
        showCauses = true
        showStackTraces = true
        showExceptions = true
        setExceptionFormat("full")
        showStandardStreams = true
        events("passed", "skipped", "failed")
    }
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJar") {
            afterEvaluate {
                artifactId = "account-sdk-android-ui"
                groupId = project.group.toString()
                version = project.version.toString()

                artifact(tasks["bundleSingledexReleaseAar"])
                //TODO: Add sources and javadoc
//                artifact(tasks["sourcesJar"])
//                artifact(tasks["javadocJar"])

                pom {
                    name.set("Schibsted Account SDK UI Module")
                    fillGenericDetails(project)
                    withXml {
                        collectDependencies(project)
                    }
                }
            }
        }
    }

    tasks.publish { dependsOn("check") }
}

bintray {
    user = findProperty("bintrayUser")?.toString()
            ?: System.getenv("BINTRAY_USER")?.toString()
    if (user == null) logger.error("BINTRAY_USER is null!")
    key = findProperty("bintrayApiKey")?.toString()
            ?: System.getenv("BINTRAY_API_KEY")?.toString()
    if (key == null) logger.error("BINTRAY_API_KEY is null!")

    setPublications("mavenJar")

    pkg.apply {
        repo = "Account-SDK-Android"
        name = "UI"
        description = "UI module for the Schibsted Account SDK"
        userOrg = "schibsted"
        setLicenses("MIT")
        vcsUrl = "https://github.com/schibsted/account-sdk-android.git"
        publish = true

        version.apply {
            name = project.version.toString()
            desc = "Account SDK Android UI ${project.version}"
            vcsTag = gitTag
            released = Date().toString()
        }
    }

    tasks.bintrayUpload { dependsOn("check") }
}
