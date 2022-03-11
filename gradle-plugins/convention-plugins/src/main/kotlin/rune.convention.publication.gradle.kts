import java.util.*

plugins {
    `maven-publish`
    signing
}

ext["secretKey"] = null
ext["signingPassword"] = null
ext["ossrhUsername"] = null
ext["ossrhPassword"] = null

val secretPropsFile = project.rootProject.file("local.properties")
if (secretPropsFile.exists()) {
    secretPropsFile.reader().use {
        Properties().apply {
            load(it)
        }
    }.onEach { (name, value) ->
        ext[name.toString()] = value
    }
} else {
    ext["secretKey"]  = System.getenv("SIGNING_SECRET_KEY")
    ext["signingPassword"] = System.getenv("SIGNING_PASSWORD")
    ext["ossrhUsername"] = System.getenv("OSSRH_USERNAME")
    ext["ossrhPassword"] = System.getenv("OSSRH_PASSWORD")
}

val javadocJar by tasks.registering(Jar::class) {
    archiveClassifier.set("javadoc")
}

fun getExtraString(name: String) = ext[name]?.toString()

publishing {

    repositories {
        maven {
            name = "sonatype"
            val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
            val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            url = if (project.extra["isReleaseVersion"] as Boolean) releasesRepoUrl else snapshotsRepoUrl
            credentials {
                username = getExtraString("ossrhUsername")
                password = getExtraString("ossrhPassword")
            }
        }
    }

    // Configure all publications
    publications.withType<MavenPublication> {

        // Stub javadoc.jar artifact
        artifact(javadocJar.get())

        pom {
            name.set("Rune")
            description.set("Kotlin Game Engine built on LittleKt")
            url.set("https://github.com/LeHaine/rune-kt")

            licenses {
                license {
                    name.set("Apache 2.0")
                    url.set("https://github.com/LeHaine/rune-kt/blob/master/LICENSE")
                }
            }
            developers {
                developer {
                    id.set("LeHaine")
                    name.set("Colt Daily")
                }
            }
            scm {
                url.set("https://github.com/LeHaine/rune-kt")
            }

        }
    }
}

signing {
    setRequired({
        (project.extra["isReleaseVersion"] as Boolean) && gradle.taskGraph.hasTask("publish")
    })
    useInMemoryPgpKeys(getExtraString("secretKey"), getExtraString("signingPassword"))
    sign(publishing.publications)
}