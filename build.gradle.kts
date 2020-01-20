tasks.register<Delete>("clean") {
    group = "build"
    delete = rootProject.allprojects.map { it.buildDir }.toSet()
    isFollowSymlinks = false
}
