repositories {
    google()
    jcenter()
}
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}
gradlePlugin {
    plugins {
        register("SharedConfigurationPlugin") {
            id = "shared-configuration"
            implementationClass = "SharedConfigurationPlugin"
        }
    }
}
dependencies {
    implementation("com.android.tools.build:gradle:3.6.1")
    implementation(kotlin("gradle-plugin", "1.3.61"))
    implementation("org.jetbrains.dokka:dokka-android-gradle-plugin:0.9.18")
    implementation("com.jfrog.bintray.gradle:gradle-bintray-plugin:1.8.4")
    implementation("com.github.ben-manes:gradle-versions-plugin:0.27.0")
}
