import xyz.jpenilla.resourcefactory.bukkit.BukkitPluginYaml
import xyz.jpenilla.resourcefactory.paper.PaperPluginYaml

plugins {
    `java-library`
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21"
    id("xyz.jpenilla.run-paper") version "3.0.0-beta.1" // Adds runServer and runMojangMappedServer tasks for testing
    id("xyz.jpenilla.resource-factory-paper-convention") version "1.3.1" // Generates plugin.yml based on the Gradle config
}

group = "io.github.Weber588"
version = "1.0.0-SNAPSHOT"
description = "Craftable & survival-configurable wands"

java {
    // Configure the java toolchain. This allows gradle to auto-provision JDK 21 on systems that only have JDK 11 installed for example.
    toolchain.languageVersion = JavaLanguageVersion.of(25)
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.papermc.io/repository/maven-public/")
        url = uri("https://mvn.lib.co.nz/public")
    }
    maven { url = uri("https://repo.codemc.io/repository/maven-releases/") }
    maven { url = uri("https://repo.codemc.io/repository/maven-snapshots/") }
}

dependencies {
    paperweight.paperDevBundle(libs.versions.io.papermc.paper.paper.api)

    api(libs.net.kyori.adventure.text.serializer.ansi)
    compileOnly(libs.io.github.weber588.wbsutils)
    compileOnly(libs.me.libraryaddict.disguises.libsdisguises)
    compileOnly(libs.com.github.retrooper.packetevents.spigot)
}

tasks {
    compileJava {
        // Set the release flag. This configures what version bytecode the compiler will emit, as well as what JDK APIs are usable.
        // See https://openjdk.java.net/jeps/247 for more information.
        options.release = 25
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name() // We want UTF-8 for everything
    }

}

// Configure plugin.yml generation
// - name, version, and description are inherited from the Gradle project.
paperPluginYaml {
    load = BukkitPluginYaml.PluginLoadOrder.STARTUP
    main = "wbs.wandcraft.WbsWandcraft"
    bootstrapper = "wbs.wandcraft.WbsWandcraftBootstrap"
    authors.add("Weber588")
    apiVersion = "26.2"
    dependencies {
        bootstrap.create("WbsUtils", {
            load = PaperPluginYaml.Load.BEFORE
            required = true
        })
        server.create("WbsUtils", {
            load = PaperPluginYaml.Load.BEFORE
            required = true
        })
        server.create("packetevents", {
            load = PaperPluginYaml.Load.BEFORE
            required = false
        })
        server.create("LibsDisguises", {
            load = PaperPluginYaml.Load.BEFORE
            required = false
        })
        server.create("ResourcePackManager", {
            load = PaperPluginYaml.Load.BEFORE
            required = false
        })
    }
}
