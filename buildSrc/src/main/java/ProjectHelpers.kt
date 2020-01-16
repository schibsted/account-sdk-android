import org.gradle.api.Project

/**
 * The most recent Git tag, extracted either from TRAVIS_TAG (on Travis)
 * or from the most recent tag on a current branch, for example, "v2.3.0".
 */
val Project.gitTag: String
    get() = findProperty("TRAVIS_TAG")?.toString()
                ?: "git describe --tags --abbrev=0".execute()

/**
 * Project version name, extracted from gitTag, for example, "v2.3.0".
 */
val Project.projectVersion: String
    get() = gitTag.trim().replace("refs/tags/", "").replace("^v".toRegex(), "")
