plugins {
    id("java")
    application
}

group = "io.github.dawciobiel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // LibGDX Core
    implementation("com.badlogicgames.gdx:gdx:1.12.1")
    
    // LibGDX Desktop Backend (LWJGL3)
    implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:1.12.1")
    implementation("com.badlogicgames.gdx:gdx-platform:1.12.1:natives-desktop")

    // LibGDX AI
    implementation("com.badlogicgames.gdx:gdx-ai:1.8.2")

    // JSON support
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
    
    // TOML support for external configuration files
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-toml:2.15.2")

    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass.set("pl.shooter.game.DesktopLauncher")
}

tasks.test {
    useJUnitPlatform()
}
