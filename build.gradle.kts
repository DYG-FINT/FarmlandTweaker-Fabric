plugins {
    `maven-publish`
    id("fabric-loom")
}

stonecutter {

}

version = "${property("mod_version")}+Fabric.${property("minecraft_version_min")}${property("version_hyphen")}${property("minecraft_version_max")}"
base.archivesName = property("archives_base_name") as String

repositories {
    // Cloth Config
    maven("https://maven.shedaniel.me/")
    maven("https://maven.terraformersmc.com/releases/")

    // Trinkets、Mod Menu
    maven("https://maven.terraformersmc.com/") // TerraformersMC
    maven("https://maven.ladysnake.org/releases") // Ladysnake Libs
}

val loaderVersion = "${property("loader_version")}"

dependencies {
    minecraft("com.mojang:minecraft:${stonecutter.current.version}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${loaderVersion}")

    modImplementation("me.shedaniel.cloth:cloth-config-fabric:${property("cloth_config_version")}")

    modCompileOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
    modRuntimeOnly("com.terraformersmc:modmenu:${property("modmenu_version")}")
}

loom {
    splitEnvironmentSourceSets()

    mods {
        create("farmland_tweaker") {
            sourceSet(sourceSets["main"])
            sourceSet(sourceSets["client"])
        }
    }

    decompilerOptions.named("vineflower") {
        options.put("mark-corresponding-synthetics", "1") // Adds names to lambdas - useful for mixins
    }

    runConfigs.all {
        ideConfigGenerated(true)
        vmArgs("-Dmixin.debug.export=true") // Exports transformed classes for debugging
        runDir = "../../run" // Shares the run directory between versions
    }
}

java {
    withSourcesJar()
    targetCompatibility = JavaVersion.VERSION_21
    sourceCompatibility = JavaVersion.VERSION_21
}

tasks {
    processResources {
        inputs.property("mod_version", project.property("mod_version"))
        inputs.property("minecraft_version", project.property("minecraft_version"))
        inputs.property("loader_version", loaderVersion)
        inputs.property("cloth_config_version", project.property("cloth_config_version"))
        inputs.property("modmenu_version", project.property("modmenu_version"))

        val props = mapOf(
            "mod_version" to project.property("mod_version"),
            "minecraft_version" to project.property("minecraft_version"),
            "loader_version" to loaderVersion,
            "cloth_config_version" to project.property("cloth_config_version"),
            "modmenu_version" to project.property("modmenu_version")
        )

        filesMatching("fabric.mod.json") { expand(props) }
    }

    // Builds the version into a shared folder in `build/libs/${mod version}/`
    register<Copy>("buildAndCollect") {
        group = "build"
        from(remapJar.map { it.archiveFile }, remapSourcesJar.map { it.archiveFile })
        into(rootProject.layout.buildDirectory.file("libs/${project.property("mod_version")}"))
        dependsOn("build")
    }
}