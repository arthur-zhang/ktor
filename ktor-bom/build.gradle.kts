plugins {
    id("java-platform")
}

//apply from: rootProject.file('gradle/publish.gradle')

//def name = project.name

//dependencies {
//    constraints {
//        rootProject.subprojects.each {
//            if (it.plugins.hasPlugin("maven-publish") && it.name != name) {
//                it.publishing.publications.all {
//                    if (it.artifactId.endsWith("-metadata")) return
//                    if (it.artifactId.endsWith("-kotlinMultiplatform")) return
//                    api(group: it.groupId, name: it.artifactId, version: it.version)
//                }
//            }
//        }
//    }
//}

//publishing {
//    publications {
//        maven(MavenPublication) {
//            from components.javaPlatform
//        }
//    }
//}
