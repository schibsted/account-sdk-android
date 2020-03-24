import com.android.build.gradle.LibraryExtension
import com.jfrog.bintray.gradle.BintrayExtension
import com.jfrog.bintray.gradle.tasks.BintrayUploadTask
import org.gradle.api.DefaultTask
import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtensionAware
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.*
import org.jetbrains.dokka.gradle.DokkaAndroidTask
import java.util.*

/**
 * All modules of our library share a significant portion of their configuration:
 * applied plugins, android, tests, publishing... This plugin applies them all.
 * Any module can later re-configure its unique parts.
 */
class SharedConfigurationPlugin: Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
        project.group = "com.schibsted.account"
        project.version = gitTag.trim().replace("refs/tags/", "").replace("^v".toRegex(), "")

        apply(plugin="com.github.ben-manes.versions") // adds "dependencyUpdates" task

        repositories {
            google()
            jcenter()
            mavenCentral()
        }

        configureAndroid()
        configureTest()
        configurePublishing()
    }

    private fun Project.configureAndroid() = run {
        apply(plugin="com.android.library")

        android {
            buildToolsVersion("29.0.2")
            compileSdkVersion(28)

            defaultConfig {
                minSdkVersion(14)
                targetSdkVersion(28)
                versionName = project.version.toString()
                testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
            }

            compileOptions {
                sourceCompatibility = JavaVersion.VERSION_1_8
                targetCompatibility = JavaVersion.VERSION_1_8
            }

            lintOptions {
                isAbortOnError = false
            }

            dexOptions {
                javaMaxHeapSize = "4g"
            }
        }
    }

    private fun Project.configureTest() = tasks.withType(Test::class).configureEach {
        useJUnitPlatform()
        testLogging {
            showCauses = true
            showStackTraces = true
            showExceptions = true
            setExceptionFormat("full")
            showStandardStreams = true
            events("passed", "skipped", "failed")
        }
    }

    private fun Project.configurePublishing() = run {
        apply(plugin="org.jetbrains.dokka-android")
        apply(plugin="maven-publish")
        apply(plugin="com.jfrog.bintray")

        tasks {
            getByName<DokkaAndroidTask>("dokka") {
                // Used by "generate_docs.sh" to generate documentation on gh-pages
                outputFormat = "html"
                outputDirectory = "${rootProject.buildDir}/docs"
            }
            register<DokkaAndroidTask>(Constants.Names.JAVADOC_TASK) {
                outputFormat = "javadoc"
                outputDirectory = "$buildDir/javadoc"
            }
            register<Jar>(Constants.Names.JAVADOC_JAR_TASK) {
                group = "publishing"
                from(named(Constants.Names.JAVADOC_TASK))
                archiveClassifier.set("javadoc")
            }
            register<Jar>(Constants.Names.SOURCES_JAR_TASK) {
                group = "publishing"
                from(android.sourceSets["main"].java.srcDirs)
                archiveClassifier.set("sources")
            }
            getByName<DefaultTask>("publish") {
                dependsOn("build")
            }
            getByName<BintrayUploadTask>("bintrayUpload") {
                dependsOn("build")
            }
        }

        publishing {
            publications.create<MavenPublication>(Constants.Names.PUBLICATION) {
                afterEvaluate {
                    groupId = project.group.toString()
                    version = project.version.toString()

                    artifact(tasks["bundleReleaseAar"])
                    artifact(tasks[Constants.Names.JAVADOC_JAR_TASK])
                    artifact(tasks[Constants.Names.SOURCES_JAR_TASK])

                    pom {
                        fillGenericDetails(project)
                        withXml {
                            collectDependencies(project)
                        }
                    }
                }
            }
        }

        bintray {
            setPublications(Constants.Names.PUBLICATION)

            user = findProperty("bintrayUser")?.toString()
                    ?: System.getenv("BINTRAY_USER")?.toString()
            if (user == null) logger.error("BINTRAY_USER is null!")

            key = findProperty("bintrayApiKey")?.toString()
                    ?: System.getenv("BINTRAY_API_KEY")?.toString()
            if (key == null) logger.error("BINTRAY_API_KEY is null!")

            pkg.apply {
                repo = "Account-SDK-Android"
                userOrg = "schibsted"
                description = project.description
                setLicenses("MIT")
                vcsUrl = "https://github.com/schibsted/account-sdk-android.git"
                publish = true

                version.apply {
                    name = project.version.toString()
                    desc = "${project.description} ${project.version}"
                    vcsTag = gitTag
                    released = Date().toString()
                }
            }
        }
    }

    private val Project.android: LibraryExtension get() =
        (this as ExtensionAware).extensions.getByName("android") as LibraryExtension

    private fun Project.android(configure: LibraryExtension.() -> Unit): Unit =
            (this as ExtensionAware).extensions.configure("android", configure)

    private fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
            (this as ExtensionAware).extensions.configure("publishing", configure)

    private fun Project.bintray(configure: BintrayExtension.() -> Unit): Unit =
            (this as ExtensionAware).extensions.configure("bintray", configure)
}
