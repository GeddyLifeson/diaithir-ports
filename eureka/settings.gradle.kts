pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven("https://maven.neoforged.net/releases") {
            name = "NeoForged"
        }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.namespace?.startsWith("org.jetbrains.kotlin") == true) {
                val kotlin_version: String by settings
                useVersion(kotlin_version)
            }
        }
    }
}

// 26.2 fork: NeoForge only. The legacy Forge ForgeGradle resolutionStrategy and the fabric module
// are dropped (MC 26.1+ ships unobfuscated; architectury-loom-no-remap replaces the classic loom).
include("common")
include("forge")

rootProject.name = "vs-eureka-mod"
