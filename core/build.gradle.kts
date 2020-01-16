import groovy.util.Node
import org.jetbrains.kotlin.config.KotlinCompilerVersion
import java.util.*

plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("android.extensions")
    `maven-publish`
    id("com.jfrog.bintray")
}

val gitTag = findProperty("TRAVIS_TAG")?.toString()
        ?: "git describe --tags --abbrev=0".execute()
version = gitTag.trim().replace("refs/tags/", "").replace("^v".toRegex(), "")
group = "com.schibsted.account"
description = "The core module for the Schibsted Account SDK"

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

    useLibrary("android.test.mock")
}

androidExtensions {
    isExperimental = true
}

dependencies {
    api(project(":common"))

    api("com.squareup.retrofit2:converter-gson:2.3.0")
    implementation("com.squareup.retrofit2:retrofit:2.3.0")
    implementation("com.android.support:support-annotations:28.0.0")
    implementation("com.android.support:support-core-utils:28.0.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:${KotlinCompilerVersion.VERSION}")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${KotlinCompilerVersion.VERSION}")

    testImplementation("junit:junit:4.12")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:3.1.9")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:0.19.3")
    testImplementation("org.slf4j:slf4j-simple:1.7.26")

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
                artifactId = "account-sdk-android-core"
                groupId = project.group.toString()
                version = project.version.toString()

                artifact(tasks["bundleReleaseAar"])
                //TODO: Add sources and javadoc
//                artifact(tasks["sourcesJar"])
//                artifact(tasks["javadocJar"])

                pom {
                    name.set("Schibsted Account SDK Core Module")
                    description.set(project.description)
                    url.set("https://github.com/schibsted/account-sdk-android")
                    licenses {
                        license {
                            name.set("MIT License")
                            url.set("http://www.opensource.org/licenses/mit-license.php")
                        }
                    }
                    developers {
                        developer {
                            name.set("Håvard Kindem")
                            email.set("havard.kindem@schibsted.com")
                            organization.set("Schibsted")
                            organizationUrl.set("http://www.schibsted.com/")
                        }
                        developer {
                            name.set("Antoine Promerova")
                            email.set("antoine.promerova@schibsted.com")
                            organization.set("Schibsted")
                            organizationUrl.set("http://www.schibsted.com/")
                        }
                    }
                    scm {
                        connection.set("scm:git:git://github.com/schibsted/account-sdk-android.git")
                        developerConnection.set("scm:git:ssh://github.com:schibsted/account-sdk-android.git")
                        url.set("https://github.com/schibsted/account-sdk-android")
                    }
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
        name = "Core"
        description = "Core module for the Schibsted Account SDK"
        userOrg = "schibsted"
        setLicenses("MIT")
        vcsUrl = "https://github.com/schibsted/account-sdk-android.git"
        publish = true

        version.apply {
            name = project.version.toString()
            desc = "Account SDK Android Core ${project.version}"
            vcsTag = gitTag
            released = Date().toString()
        }
    }

    tasks.bintrayUpload { dependsOn("check") }
}

/**
 * Executes string as shell command and returns result as string.
 */
fun String.execute(): String = Runtime.getRuntime().exec(this)
        .inputStream.bufferedReader().use { it.readText() }

/**
 * Adds "implementation" and "api" dependencies of a project into a "dependencies" xml node.
 */
fun XmlProvider.collectDependencies(project: Project) {

    @Suppress("UNNECESSARY_SAFE_CALL")
    fun Node.appendExclusion(excludeRule: ExcludeRule) = appendNode("exclusion").apply {
        excludeRule.group?.run { appendNode("groupId", this) }
        excludeRule.module?.run { appendNode("artifactId", this) }
    }

    fun Node.appendExclusions(dep: ModuleDependency) = appendNode("exclusions").apply {
        dep.excludeRules.forEach {
            appendExclusion(it)
        }
    }

    fun Node.appendDependency(dep: Dependency, scope: String) = appendNode("dependency").apply {
        appendNode("groupId", dep.group)
        appendNode("artifactId", dep.name)
        appendNode("version", dep.version)
        appendNode("scope", scope)
        if (dep is ModuleDependency && dep.excludeRules.isNotEmpty()) {
            appendExclusions(dep)
        }
    }

    fun Node.appendDependencies(apiDeps: List<Dependency>, implDeps: List<Dependency>) = appendNode("dependencies").apply {
        fun Dependency.isValid() = group != null && version != null && name != "unspecified"

        apiDeps.filter { it.isValid() }.forEach {
            appendDependency(it, "compile")
        }
        implDeps.filter { it.isValid() }.forEach {
            appendDependency(it, "runtime")
        }
    }

    val apiDeps = project.configurations["api"].allDependencies.toList()
    val implDeps = project.configurations["implementation"].allDependencies
            .filter { it !in apiDeps }

    if (apiDeps.isNotEmpty() || implDeps.isNotEmpty()) {
        asNode().appendDependencies(apiDeps, implDeps)
    }
}
