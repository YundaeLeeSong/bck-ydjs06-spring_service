/*
 * [buildSrc]
 * Gradle builds this directory before the rest of the project.
 * Contains the Ylint task class and the sns.blog.lint-conventions plugin.
 */
plugins {
    `kotlin-dsl`
}

repositories {
    mavenCentral()
}

dependencies {
    /*
     * [Spotless API]
     * Library that lets sns.blog.lint-conventions.gradle.kts configure Spotless.
     */
    implementation("com.diffplug.spotless:spotless-plugin-gradle:6.25.0")
}
