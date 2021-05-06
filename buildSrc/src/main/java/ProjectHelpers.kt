import org.gradle.api.Project
import org.gradle.api.publish.PublicationContainer
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByName
import org.gradle.plugin.use.PluginDependenciesSpec
import org.gradle.plugin.use.PluginDependencySpec
import org.jetbrains.dokka.gradle.DokkaAndroidTask
import org.jetbrains.dokka.gradle.DokkaTask

/**
 * Gradle plugin implemented by [SharedConfigurationPlugin].
 */
inline val PluginDependenciesSpec.`shared-configuration`: PluginDependencySpec
    get() = id("shared-configuration")

/**
 * The most recent Git tag, extracted either from TRAVIS_TAG (on Travis)
 * or from the most recent tag on a current branch, for example, "v2.3.0".
 */
val Project.gitTag: String
    get() = findProperty("TRAVIS_TAG")?.toString()
            ?: "git describe --tags --abbrev=0".execute()

/**
 * Returns MavenPublication added by [SharedConfigurationPlugin].
 */
inline val PublicationContainer.mavenJar: MavenPublication
    get() = getByName<MavenPublication>(Constants.Names.PUBLICATION)

/**
 * Configures MavenPublication added by [SharedConfigurationPlugin].
 */
fun PublicationContainer.mavenJar(configure: MavenPublication.() -> Unit): Unit =
        getByName(Constants.Names.PUBLICATION, configure)

/**
 * Returns "sourcesJar" task added by [SharedConfigurationPlugin].
 */
inline val TaskContainer.sourcesJar: Jar
    get() = getByName<Jar>(Constants.Names.SOURCES_JAR_TASK)


/**
 * Configures "sourcesJar" task added by [SharedConfigurationPlugin].
 */
fun TaskContainer.sourcesJar(configure: Jar.() -> Unit): Unit =
        getByName(Constants.Names.SOURCES_JAR_TASK, configure)
