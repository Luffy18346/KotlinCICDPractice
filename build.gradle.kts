// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.sonarqube") version "5.1.0.4882"
    id("jacoco")
}

apply(from = "team-props/git-hooks.gradle")

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}