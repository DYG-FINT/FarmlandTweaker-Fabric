plugins {
    id("dev.kikugie.stonecutter")
    id("fabric-loom") version "1.13.4" apply false
}

stonecutter active "1.17.1"
stonecutter parameters {
    swaps["mod_version"] = "\"" + property("mod_version") + "\";"
    swaps["minecraft"] = "\"" + node.metadata.version + "\";"
    constants["release"] = property("mod_id") != "farmland_tweaker"
}
