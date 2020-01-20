import org.jetbrains.kotlin.config.KotlinCompilerVersion

plugins {
    `shared-configuration`
}

description = "The Smartlock module for the Schibsted Account SDK"

android {
    defaultConfig {
        consumerProguardFiles("smartlock-rules.pro")
    }
}

dependencies {
    implementation(project(":common"))
    implementation(project(":core"))
    implementation("com.android.support:appcompat-v7:${Constants.Versions.SUPPORT}")
    implementation("com.google.android.gms:play-services-auth:${Constants.Versions.PLAY_SERVICES_AUTH}") {
        exclude(group="com.android.support")
    }
    implementation(kotlin("stdlib", KotlinCompilerVersion.VERSION))
    implementation(kotlin("reflect", KotlinCompilerVersion.VERSION))
}

publishing {
    publications.mavenJar {
        afterEvaluate {
            artifactId = "account-sdk-android-smartlock"
            pom {
                name.set("Schibsted Account SDK Smartlock Module")
            }
        }
    }

    bintray {
        pkg.apply {
            name = "Smartlock"
            version.apply {
                desc = "Account SDK Android Smartlock ${project.version}"
            }
        }
    }
}
