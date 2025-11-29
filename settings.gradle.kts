pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.kikugie.dev/snapshots") { name = "KikuGie Snapshots" }
    }
}

plugins {
    id("dev.kikugie.stonecutter") version "0.7.10"
}

stonecutter {
    create(rootProject) {
        versions(
            "1.18.2",
            "1.19.2", "1.19.3", "1.19.4",
            "1.20.6",
            "1.21.1", "1.21.4", "1.21.8"
        )
        vcsVersion = "1.21.8"
    }
}

rootProject.name = "Farmland Tweaker"