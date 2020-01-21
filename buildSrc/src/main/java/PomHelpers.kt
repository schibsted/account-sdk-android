import groovy.util.Node
import org.gradle.api.Project
import org.gradle.api.XmlProvider
import org.gradle.api.artifacts.Dependency
import org.gradle.api.artifacts.ExcludeRule
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.publish.maven.MavenPom
import org.gradle.kotlin.dsl.get

/**
 * Populates POM with project description, repository's url
 * (https://github.com/schibsted/account-sdk-android), license (MIT),
 * developer info (Håvard Kindem and Antoine Promerova) and repository's scm info.
 * These details are shared between all projects in this repository.
 */
@Suppress("UnstableApiUsage")
fun MavenPom.fillGenericDetails(project: Project) {
    description.set(project.description)
    url.set("https://github.com/schibsted/account-sdk-android")
    licenses {
        license {
            name.set("MIT License")
            url.set("https://www.opensource.org/licenses/mit-license.php")
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
}

/**
 * Adds "implementation" and "api" dependencies of a project into a "dependencies" xml node.
 */
fun XmlProvider.collectDependencies(project: Project) {

    fun Node.appendExclusion(excludeRule: ExcludeRule) = appendNode("exclusion").apply {
        appendNode("groupId", excludeRule.group ?: "*")
        appendNode("artifactId", excludeRule.module ?: "*")
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
