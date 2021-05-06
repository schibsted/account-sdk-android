repositories {
    google()
    mavenCentral()
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
    implementation("com.android.tools.build:gradle:4.0.0")
    implementation(kotlin("gradle-plugin", "1.3.61"))
    implementation("org.jetbrains.dokka:dokka-android-gradle-plugin:0.9.18")
}
