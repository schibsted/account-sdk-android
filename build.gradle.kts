tasks.register<Delete>("clean") {
    group = "build"
    delete = rootProject.allprojects.map { it.buildDir }.toSet()
    isFollowSymlinks = false
}

plugins {
    id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

nexusPublishing {
    repositories {
        sonatype()
    }
}

group = "com.schibsted.account"
version = gitTag.trim().replace("refs/tags/", "").replace("^v".toRegex(), "")
