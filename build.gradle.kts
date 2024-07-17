// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.1.1" apply false
    id("io.gitlab.arturbosch.detekt") version "1.23.6" apply false
    id("org.sonarqube") version "5.1.0.4882"
    id("com.google.gms.google-services") version "4.4.2" apply false

    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.22" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false

//    id("jacoco")
}

apply(from = "team-props/git-hooks.gradle")

tasks.register<Delete>("clean") {
    delete(layout.buildDirectory)
}
