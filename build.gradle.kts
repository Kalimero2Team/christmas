import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    `java-library`
    id("xyz.jpenilla.run-paper") version "1.0.6"
    id("net.minecrell.plugin-yml.bukkit") version "0.5.2"
    id("io.papermc.paperweight.userdev") version "1.3.8"
}

group = "de.beyondblocks.plugins"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://papermc.io/repo/repository/maven-public/")
    maven("https://maven.enginehub.org/repo/")
    maven("https://hub.jeff-media.com/nexus/repository/jeff-media-public/")
}

dependencies {
    paperDevBundle("1.19.3-R0.1-SNAPSHOT")
    bukkitLibrary("cloud.commandframework", "cloud-paper", "1.8.0")
    bukkitLibrary("org.xerial", "sqlite-jdbc", "3.40.0.0")
    compileOnly("com.fastasyncworldedit", "FastAsyncWorldEdit-Core", "2.5.0")
    compileOnly("com.fastasyncworldedit","FastAsyncWorldEdit-Bukkit","2.5.0") {
        isTransitive = false
    }
    compileOnly("com.sk89q.worldguard", "worldguard-bukkit", "7.0.8-SNAPSHOT"){
        isTransitive = false
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

bukkit {
    main = "de.beyondblocks.plugins.christmas.ChristmasPlugin"
    apiVersion = "1.19"
    load = BukkitPluginDescription.PluginLoadOrder.POSTWORLD
    authors = listOf("byquanton")
    depend = listOf("FastAsyncWorldEdit", "WorldGuard")
}

