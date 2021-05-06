import com.android.build.gradle.LibraryExtension
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
import org.gradle.plugins.signing.SigningExtension
import org.jetbrains.dokka.gradle.DokkaAndroidTask
import java.util.*

/**
 * All modules of our library share a significant portion of their configuration:
 * applied plugins, android, tests, publishing... This plugin applies them all.
 * Any module can later re-configure its unique parts.
 */
class SharedConfigurationPlugin: Plugin<Project> {

    override fun apply(target: Project): Unit = target.run {
        project.group = rootProject.group
        project.version = rootProject.version

        repositories {
            google()
            mavenCentral()
            jcenter()
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
                minSdkVersion(21)
                targetSdkVersion(28)
                versionName = project.version.toString()
                testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
        apply(plugin="signing")

        tasks {
            getByName<DokkaAndroidTask>("dokka") {
                // Used by "generate_docs.sh" to generate documentation on gh-pages
                outputFormat = "html"
                outputDirectory = "${rootProject.buildDir}/docs"
            }
            register<Jar>(Constants.Names.SOURCES_JAR_TASK) {
                group = "publishing"
                from(android.sourceSets["main"].java.srcDirs)
                archiveClassifier.set("sources")
            }
            getByName<DefaultTask>("publish") {
                dependsOn("build")
            }
        }

        publishing {
            publications.create<MavenPublication>(Constants.Names.PUBLICATION) {
                afterEvaluate {
                    groupId = project.group.toString()
                    version = project.version.toString()

                    artifact(tasks["bundleReleaseAar"])

                    pom {
                        fillGenericDetails(project)
                        withXml {
                            collectDependencies(project)
                        }
                    }
                }
            }
        }

        signing {
            useInMemoryPgpKeys(base64Decode("SIGNING_KEY"), System.getenv("SIGNING_PASSWORD"))
            sign(extensions.getByType(PublishingExtension::class.java).publications)
        }
    }

    private val Project.android: LibraryExtension get() =
        (this as ExtensionAware).extensions.getByName("android") as LibraryExtension

    private fun Project.android(configure: LibraryExtension.() -> Unit): Unit =
            (this as ExtensionAware).extensions.configure("android", configure)

    private fun Project.publishing(configure: PublishingExtension.() -> Unit): Unit =
            (this as ExtensionAware).extensions.configure("publishing", configure)

    private fun Project.signing(configure: SigningExtension.() -> Unit): Unit =
            (this as ExtensionAware).extensions.configure("signing", configure)

    private fun base64Decode(envVar: String): String? {
        return System.getenv(envVar)?.let {
            String(Base64.getDecoder().decode(it)).trim()
        }
    }
}
