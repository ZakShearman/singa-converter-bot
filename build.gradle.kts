plugins {
    `java-library`
    id("org.springframework.boot") version "3.0.5"
    id("io.spring.dependency-management") version "1.1.0"
    id("io.freefair.lombok") version "8.0.1"
}

val versionObject = Version(major = "1", minor = "0", revision = "0")

fun buildNumber(): String? {
    return System.getenv("BUILD_NUMBER")
}

project.version = "$versionObject" + if (buildNumber() != null) "_" + buildNumber() else ""
project.group = "pink.zak.discord"

repositories {
    mavenCentral()
    mavenLocal()
    maven("https://jitpack.io")
    maven("https://m2.dv8tion.net/releases")
}

dependencies {
    implementation("com.github.ZakShearman:spring-boot-starter-discord:local")
    implementation("net.dv8tion:JDA:5.0.0-beta.6")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.5")

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("se.michaelthelin.spotify:spotify-web-api-java:8.0.0")
    implementation("me.xdrop:fuzzywuzzy:1.4.0")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.data:spring-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-actuator")

    developmentOnly("org.springframework.boot:spring-boot-devtools")
    runtimeOnly("org.mariadb.jdbc:mariadb-java-client:3.1.2")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

// Taken from JDA https://github.com/DV8FromTheWorld/JDA/blob/master/build.gradle.kts#L319
class Version(
    val major: String,
    val minor: String,
    val revision: String,
    val classifier: String? = null
) {
    companion object {
        fun parse(string: String): Version {
            val (major, minor, revision) = string.substringBefore("-").split(".")
            val classifier = if ("-" in string) string.substringAfter("-") else null
            return Version(major, minor, revision, classifier)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (other === this) return true
        if (other !is Version) return false
        return major == other.major
                && minor == other.minor
                && revision == other.revision
                && classifier == other.classifier
    }

    override fun toString(): String {
        return "$major.$minor.$revision" + if (classifier != null) "-$classifier" else ""
    }
}
