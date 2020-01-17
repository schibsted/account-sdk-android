import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    id("org.jetbrains.dokka-android")
    `maven-publish`
    id("com.jfrog.bintray")
}

version = projectVersion
group = "com.schibsted.account"
description = "The Smartlock module for the Schibsted Account SDK"

android {
    buildToolsVersion("29.0.2")
    compileSdkVersion(28)

    defaultConfig {
        minSdkVersion(14)
        targetSdkVersion(28)
        versionName = project.version.toString()
        consumerProguardFiles("smartlock-rules.pro")
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
    implementation(project(":common"))
    implementation(project(":core"))
    implementation("com.android.support:appcompat-v7:28.0.0")
    implementation("com.google.android.gms:play-services-auth:15.0.1") {
        exclude(group="com.android.support")
    }
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))
}

tasks {
    withType(Test::class) {
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

    dokka {
        // See "generate_docs.sh" for usage.
        outputFormat = "html"
        outputDirectory = "${rootProject.buildDir}/docs"
    }
}

publishing {
    tasks.publish { dependsOn("check") }

    val dokkaJavadoc by tasks.registering(DokkaTask::class) {
        outputFormat = "javadoc"
        outputDirectory = "$buildDir/javadoc"
    }
    val javadocJar by tasks.registering(Jar::class) {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        from(dokkaJavadoc)
        archiveClassifier.set("javadoc")
    }
    val sourcesJar by tasks.registering(Jar::class) {
        group = PublishingPlugin.PUBLISH_TASK_GROUP
        from(android.sourceSets["main"].java.srcDirs)
        archiveClassifier.set("sources")
    }

    val mavenJar by publications.registering(MavenPublication::class) {
        afterEvaluate {
            artifactId = "account-sdk-android-smartlock"
            groupId = project.group.toString()
            version = project.version.toString()

            artifact(tasks["bundleReleaseAar"])
            artifact(javadocJar.get())
            artifact(sourcesJar.get())

            pom {
                name.set("Schibsted Account SDK Smartlock Module")
                fillGenericDetails(project)
                withXml {
                    collectDependencies(project)
                }
            }
        }
    }

    bintray {
        tasks.bintrayUpload { dependsOn("check") }

        setPublications(mavenJar.name)

        user = findProperty("bintrayUser")?.toString()
                ?: System.getenv("BINTRAY_USER")?.toString()
        if (user == null) logger.error("BINTRAY_USER is null!")
        key = findProperty("bintrayApiKey")?.toString()
                ?: System.getenv("BINTRAY_API_KEY")?.toString()
        if (key == null) logger.error("BINTRAY_API_KEY is null!")

        pkg.apply {
            repo = "Account-SDK-Android"
            name = "Smartlock"
            description = "Smartlock module for the Schibsted Account SDK"
            userOrg = "schibsted"
            setLicenses("MIT")
            vcsUrl = "https://github.com/schibsted/account-sdk-android.git"
            publish = true

            version.apply {
                name = project.version.toString()
                desc = "Account SDK Android Smartlock ${project.version}"
                vcsTag = gitTag
                released = Date().toString()
            }
        }
    }
}
