plugins {
    java
}

group = "de.opalium"

// Version automatisch aus GitHub Actions ableiten:
// - Auf GitHub Actions ist GITHUB_RUN_NUMBER gesetzt (1,2,3,...)
// - Lokal (IDE / kein Actions) bekommst du eine feste SNAPSHOT-Version
val ciRunNumber = System.getenv("GITHUB_RUN_NUMBER")
version = if (ciRunNumber != null) {
    "1.0.$ciRunNumber"    // z.B. 1.0.96, 1.0.97 ...
} else {
    "1.0.0-SNAPSHOT"      // lokale Dev-Version
}

repositories {
    mavenCentral()

    // Paper API
    maven("https://repo.papermc.io/repository/maven-public/")

    // PlaceholderAPI
    maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")

    // VaultAPI über JitPack
    maven("https://jitpack.io")
}

dependencies {
    // Paper-API für 1.21.10
    compileOnly("io.papermc.paper:paper-api:1.21.10-R0.1-SNAPSHOT")

    // Vault-API (funktionierende Koordinate, nicht 1.7.1)
    compileOnly("com.github.MilkBowl:VaultAPI:1.7") {
        // Bukkit/Spigot nicht mitziehen, da vom Server bereitgestellt
        exclude(group = "org.bukkit", module = "bukkit")
        exclude(group = "org.spigotmc", module = "spigot-api")
    }

    // PlaceholderAPI
    compileOnly("me.clip:placeholderapi:2.11.6")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.processResources {
    filesMatching("plugin.yml") {
        expand("version" to project.version)
    }
}
