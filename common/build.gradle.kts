import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    `maven-publish`
    id("com.jfrog.bintray")
}

version = projectVersion
group = "com.schibsted.account"
description = "The common module for the Schibsted Account SDK"

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(28)
        versionName = project.version.toString()
        consumerProguardFiles("common-rules.pro")
        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
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
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KotlinCompilerVersion.VERSION}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${KotlinCompilerVersion.VERSION}")

    testImplementation("junit:junit:4.12")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.9")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.slf4j:slf4j-simple:1.7.26")
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
                artifactId = "account-sdk-android-common"
                groupId = project.group.toString()
                version = project.version.toString()

                artifact(tasks["bundleReleaseAar"])
                //TODO: Add sources and javadoc
//                artifact(tasks["sourcesJar"])
//                artifact(tasks["javadocJar"])

                pom {
                    name.set("Schibsted Account SDK Common Module")
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
        name = "Common"
        description = "Common module for the Schibsted Account SDK"
        userOrg = "schibsted"
        setLicenses("MIT")
        vcsUrl = "https://github.com/schibsted/account-sdk-android.git"
        publish = true

        version.apply {
            name = project.version.toString()
            desc = "Account SDK Android Common ${project.version}"
            vcsTag = gitTag
            released = Date().toString()
        }
    }

    tasks.bintrayUpload { dependsOn("check") }
}
