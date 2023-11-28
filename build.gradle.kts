plugins {
    java
}

repositories {
    mavenLocal()
    maven {
        url = uri("https://repo.runelite.net")
    }
    mavenCentral()
}

dependencies {
    val lombokVersion = "1.18.30" // supports JDK 21 and is verified by runelite
    // old annotation processor approach due to runelite plugin hub verification restrictions
    compileOnly(group = "org.projectlombok", name = "lombok", version = lombokVersion)
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = lombokVersion)
    testCompileOnly(group = "org.projectlombok", name = "lombok", version = lombokVersion)
    testAnnotationProcessor(group = "org.projectlombok", name = "lombok", version = lombokVersion)

    // this version of annotations is verified by runelite
    compileOnly(group = "org.jetbrains", name = "annotations", version = "23.0.0")

    val runeLiteVersion = "latest.release"
    compileOnly(group = "net.runelite", name = "client", version = runeLiteVersion)
    testImplementation(group = "net.runelite", name = "client", version = runeLiteVersion)
    testImplementation(group = "net.runelite", name = "jshell", version = runeLiteVersion)

    val junitVersion = "5.5.2" // max version before junit-bom was added to pom files, due to runelite restrictions
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-api", version = junitVersion)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-params", version = junitVersion)
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter-engine", version = junitVersion)

    // mocking and test injection used by runelite client
    testImplementation(group = "org.mockito", name = "mockito-core", version = "4.11.0") // runelite uses 3.1.0
    testImplementation(group = "com.google.inject.extensions", name = "guice-testlib", version = "4.1.0") {
        exclude(group = "com.google.inject", module = "guice") // already provided by runelite client
    }
}

group = "io.cbitler.stealingartefacts"
version = "1.2"

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    val version = "1.8"
    sourceCompatibility = version
    targetCompatibility = version
}

tasks.test {
    useJUnitPlatform()
}