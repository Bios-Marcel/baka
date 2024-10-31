plugins {
    id("application")
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("antlr")
    id("org.panteleyev.jpackageplugin") version "1.6.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.antlr:antlr4-runtime:4.13.1")
    implementation("org.jspecify:jspecify:0.3.0")
    implementation("org.apache.commons:commons-csv:1.11.0")
    implementation("org.eclipse.store:storage-embedded:2.0.0")
    implementation("org.eclipse.store:storage-embedded-configuration:2.0.0")
    implementation("org.slf4j:slf4j-nop:2.0.13")

    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.10.0")

    antlr("org.antlr:antlr4:4.13.1")
}

application {
    mainModule = "link.biosmarcel.baka"
    mainClass = "link.biosmarcel.baka.Main"
}

tasks.generateGrammarSource {
    maxHeapSize = "64m"
    arguments = arguments +
            listOf(
                "-visitor", "-long-messages",
                "-package", "link.biosmarcel.baka"
            )
    outputDirectory = File("${layout.buildDirectory.get()}/generated-src/antlr/main/link/biosmarcel/baka")
}

javafx {
    version = "22.0.2"
    modules = listOf("javafx.controls")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    useJUnitPlatform()
    systemProperty("file.encoding", "UTF-8")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(22)
    }
}

task("copyDependencies", Copy::class) {
    from(configurations.runtimeClasspath).into("${layout.buildDirectory.get()}/jmods")
}

task("copyJar", Copy::class) {
    from(tasks.jar).into("${layout.buildDirectory.get()}/jmods")
}

tasks.jpackage {
    dependsOn("build", "copyDependencies", "copyJar")

    appVersion = project.version.toString()
    appName = "Baka"
    vendor = "biosmarcel"
    // FIXME Generate runtime
    runtimeImage = System.getProperty("java.home")
    module = "link.biosmarcel.baka/link.biosmarcel.baka.Main"
    modulePaths = listOf("${layout.buildDirectory.get()}/jmods")
    destination = "${layout.buildDirectory.get()}/dist"
    javaOptions = listOf("-Dfile.encoding=UTF-8")

    windows {
        winMenu = true
        winPerUserInstall = true
        icon = "icon.ico"
        winUpgradeUuid = "992b362a-998c-4a30-ab65-7c380540aefa"
    }
}

version = "1.0.0"

