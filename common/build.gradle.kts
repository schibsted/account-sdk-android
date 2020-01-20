import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
}

description = "The common module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("common-rules.pro")
    }
}

dependencies {
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))

    testImplementation("junit:junit:${Constants.Versions.JUNIT}")
    testImplementation("io.kotlintest:kotlintest-runner-junit5:${Constants.Versions.KOTLINTEST_RUNNER_JUNIT5}")
    testImplementation("com.nhaarman.mockitokotlin2:mockito-kotlin:${Constants.Versions.MOCKITO_KOTLIN}")
    testImplementation("org.slf4j:slf4j-simple:${Constants.Versions.SLF4J}")
}

publishing {
    publications.mavenJar {
        afterEvaluate {
            artifactId = "account-sdk-android-common"
            pom {
                name.set("Schibsted Account SDK Common Module")
            }
        }
    }

    bintray {
        pkg.apply {
            name = "Common"
            version.apply {
                desc = "Account SDK Android Common ${project.version}"
            }
        }
    }
}
