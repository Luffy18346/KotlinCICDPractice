import org.jlleitschuh.gradle.ktlint.reporter.ReporterType
import java.util.Locale

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("org.jlleitschuh.gradle.ktlint")
    id("io.gitlab.arturbosch.detekt")
    id("org.sonarqube")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("jacoco")
}

android {
    namespace = "com.example.kotlincicdpractice"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.kotlincicdpractice"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "com.example.kotlincicdpractice.HiltTestRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        getByName("debug") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            applicationIdSuffix = ".debug"
            isDebuggable = true
        }
        release {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        create("beta") {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
            initWith(getByName("release"))
            isDebuggable = true
        }
    }

    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            applicationIdSuffix = ".dev"
        }
        create("staging") {
            applicationIdSuffix = ".staging"
        }
        create("production") {}
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources.excludes.addAll(
            listOf(
                "/META-INF/{AL2.0,LGPL2.1}",
                "META-INF/LICENSE.md",
                "META-INF/LICENSE-notice.md",
            ),
        )
    }
    testOptions.unitTests {
        isReturnDefaultValues = true
    }
}

tasks.withType<Test> {
    useJUnit()
    configure<JacocoTaskExtension> {
        isIncludeNoLocationClasses = true
        excludes = arrayListOf("jdk.internal.*")
    }
}

android {
    applicationVariants.all(
        closureOf<com.android.build.gradle.internal.api.BaseVariantImpl> {
            // Extract variant name and capitalize the first letter
            val variant =
                this@closureOf.name.replaceFirstChar {
                    if (it.isLowerCase()) {
                        it.titlecase(
                            Locale.getDefault(),
                        )
                    } else {
                        it.toString()
                    }
                }

            // Define task names for unit tests and Android tests
            val unitTests = "test${variant}UnitTest"
            val androidTests = "connected${variant}AndroidTest"

            tasks.register<JacocoReport>("jacoco${variant}TestReport") {
                if (variant.contains("debug", true)) {
                    dependsOn(listOf(unitTests, androidTests))
                } else {
                    dependsOn(listOf(unitTests))
                }

                // Set task grouping and description
                group = "Reporting"
                description =
                    "Execute UI and unit tests, generate and combine Jacoco coverage report"

                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    csv.required.set(false)
                    html.outputLocation.set(file("build/reports/jacoco"))
                }

                val fileFilter =
                    listOf(
                        "**/R.class",
                        "**/R$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "android/**/*.*",
                    )

                // Set source directories to the main source directory
//                sourceDirectories.setFrom(layout.projectDirectory.dir("src/main"))
//                // Set class directories to compiled Java and Kotlin classes, excluding specified exclusions
//                classDirectories.setFrom(
//                    files(
//                        fileTree(layout.buildDirectory.dir("intermediates/javac/")) {
//                            exclude(fileFilter)
//                        },
//                        fileTree(layout.buildDirectory.dir("tmp/kotlin-classes/")) {
//                            exclude(fileFilter)
//                        },
//                    ),
//                )
//
//            val debugTree =
//                fileTree("${layout.buildDirectory}/intermediates/classes/debug") {
//                    exclude(fileFilter)
//                }
//            val kotlinDebugTree =
//                fileTree("${layout.buildDirectory}/tmp/kotlin-classes/debug") {
//                    exclude(fileFilter)
//                }
//            val mainSrc = "${project.projectDir}/src/main/java"
//            sourceDirectories.setFrom(files(mainSrc))
//            classDirectories.setFrom(files(debugTree, kotlinDebugTree))

                // Set source directories to the main source directory
                sourceDirectories.setFrom(layout.projectDirectory.dir("src/main"))

                // Set class directories to compiled Java and Kotlin classes, excluding specified exclusions
                val debugTree =
                    fileTree("${layout.buildDirectory}/intermediates/classes/$variant") {
                        exclude(fileFilter)
                    }
                val kotlinDebugTree =
                    fileTree("${layout.buildDirectory}/tmp/kotlin-classes/$variant") {
                        exclude(fileFilter)
                    }
                classDirectories.setFrom(files(debugTree, kotlinDebugTree))

                executionData.setFrom(
                    files(
                        fileTree(layout.buildDirectory) { include(listOf("**/*.exec", "**/*.ec")) },
                    ),
                )
            }

            tasks.register<JacocoReport>("jacocoPr${variant}TestReport") {
                if (variant.contains("debug", true)) {
                    dependsOn(listOf(unitTests, androidTests))
                } else {
                    dependsOn(listOf(unitTests))
                }

                // Set task grouping and description
                group = "Reporting"
                description =
                    "Execute UI and unit tests, generate and combine Jacoco coverage report"

                reports {
                    xml.required.set(true)
                    html.required.set(true)
                    csv.required.set(false)
                    html.outputLocation.set(file("build/reports/jacoco"))
                }

                val fileFilter =
                    listOf(
                        "**/R.class",
                        "**/R$*.class",
                        "**/BuildConfig.*",
                        "**/Manifest*.*",
                        "**/*Test*.*",
                        "android/**/*.*",
                    )

                // Set source directories to the main source directory
                sourceDirectories.setFrom(layout.projectDirectory.dir("src/main"))

                // Set class directories to compiled Java and Kotlin classes, excluding specified exclusions
                val debugTree =
                    fileTree("${layout.buildDirectory}/intermediates/javac/$variant") {
                        exclude(fileFilter)
                    }
                val kotlinDebugTree =
                    fileTree("${layout.buildDirectory}/tmp/kotlin-classes/$variant") {
                        exclude(fileFilter)
                    }
                classDirectories.setFrom(files(debugTree, kotlinDebugTree))

                executionData.setFrom(
                    files(
                        fileTree(layout.buildDirectory) { include(listOf("**/*.exec", "**/*.ec")) },
                    ),
                )
            }
        },
    )
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics)

    // Compose dependencies
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.3")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
    implementation(libs.androidx.material.icons)

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0-RC")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")

    // Dagger - Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    ksp("com.google.dagger:hilt-android-compiler:2.48")

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Kotlin Extensions and Coroutines support for Room
    implementation("androidx.room:room-ktx:2.6.1")

    // Local unit tests
    testImplementation("androidx.test:core:1.6.1")
    testImplementation("junit:junit:4.13.2")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("com.google.truth:truth:1.4.2")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    testImplementation("io.mockk:mockk:1.13.10")
    debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.8")
    testImplementation("androidx.test.espresso:espresso-core:3.6.1")
    testImplementation("androidx.test.espresso:espresso-intents:3.6.1")

    // Instrumentation tests
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kspAndroidTest("com.google.dagger:hilt-android-compiler:2.48")
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    androidTestImplementation("androidx.arch.core:core-testing:2.2.0")
    androidTestImplementation("com.google.truth:truth:1.4.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test:core-ktx:1.6.1")
    androidTestImplementation("com.squareup.okhttp3:mockwebserver:4.10.0")
    androidTestImplementation("io.mockk:mockk-android:1.13.12")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.test:runner:1.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.8")
}

tasks.withType<Test> {
    useJUnit()
}

ktlint {
    android = true
    ignoreFailures = false
    verbose = true
    outputToConsole = true
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.SARIF)
        reporter(ReporterType.JSON)
    }
}

detekt {
    autoCorrect = true
    buildUponDefaultConfig = true
}

// StandAlone Lint Checks on Builds
// tasks.preBuild.dependsOn("detekt").dependsOn("ktlintFormat")

// Apply the git-hooks.gradle file
apply(from = "$rootDir/team-props/git-hooks.gradle")

// Ensure clean, assemble and build tasks depend on installGitHooks
gradle.taskGraph.whenReady(
    closureOf<TaskExecutionGraph> {
        this@closureOf.allTasks.forEach { task ->
            if (task.name.startsWith("pre") && task.name.endsWith("Build")) {
                android.applicationVariants.all { variant ->
                    val variantName =
                        variant.name.replaceFirstChar {
                            if (it.isLowerCase()) {
                                it.titlecase(
                                    Locale.getDefault(),
                                )
                            } else {
                                it.toString()
                            }
                        }

                    if (task.name == "pre${variantName}Build") {
                        task.doFirst {
                            val variantFile = File("${project.rootDir}/build-variant.txt")
                            variantFile.writeText(variantName)
                        }
                    }
                    true
                }
            }
        }
    },
)

afterEvaluate {
    tasks.named("clean") {
        dependsOn("makeGradlewExecutable", "installGitHooks")
    }
}

sonarqube {
    properties {
        property("sonar.projectKey", "KotlinCICDPractice")
        property("sonar.organization", "KotlinCICDPractice")
        property("sonar.host.url", "http://localhost:9000")
//        property("sonar.login", "318109bb61c534e7a847069ad286b3845dd5b0fb")

        property("sonar.sources", "src/main/java")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.tests", "src/test/java")
        property("sonar.test.inclusions", "**/*Test*/**")
        property(
            "sonar.exclusions",
            "**/*Test*/**," + "*.json," + "**/*test*/**," + "**/.gradle/**," + "**/R.class",
        )

        property("sonar.android.lint.reportPaths", "build/reports/lint-results-devDebug.xml")
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.junit.reportPaths", "build/test-results/testDevDebugUnitTest")
//        property("sonar.jacoco.reportPaths", "**/jacoco/*.exec")
//        property("sonar.jacoco.reportPaths", "build/jacoco/testDebugUnitTest.exec")
//        property("sonar.jacoco.reportPaths", "build/reports/jacoco/test/jacoco${variant}TestReport.xml")
        property("sonar.kotlin.detekt.reportPaths", "build/reports/detekt/detekt.xml")
        property(
            "sonar.kotlin.ktlint.reportPaths",
            "build/reports/ktlint/ktlintMainSourceSetCheck/ktlintMainSourceSetCheck.xml",
        )
    }
}

// Task 'createDebugCoverageReport' is ambiguous in root project 'KotlinCICDPractice' and its subprojects.
// Candidates are: 'createDevDebugAndroidTestCoverageReport', 'createDevDebugCoverageReport',
// 'createDevDebugUnitTestCoverageReport', 'createProductionDebugAndroidTestCoverageReport',
// 'createProductionDebugCoverageReport', 'createProductionDebugUnitTestCoverageReport',
// 'createStagingDebugAndroidTestCoverageReport', 'createStagingDebugCoverageReport',
// 'createStagingDebugUnitTestCoverageReport'.
