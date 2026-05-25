// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.kotlin.compose) apply false
  alias(libs.plugins.google.devtools.ksp) apply false
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.secrets) apply false
}

tasks.register("searchImagesAll") {
    doLast {
        println("--- DUMPING PROJECT TREE FOR IMAGES ---")
        val root = java.io.File(".")
        root.walk()
            .filter { it.isFile && (it.name.endsWith(".png", true) || it.name.endsWith(".jpg", true) || it.name.endsWith(".jpeg", true) || it.name.endsWith(".webp", true)) }
            .forEach { file ->
                println("IMG: " + file.getAbsolutePath() + " - Size: " + file.length())
            }
        val tmp = java.io.File("/tmp")
        if (tmp.exists()) {
            tmp.walk()
                .filter { it.isFile && (it.name.endsWith(".png", true) || it.name.endsWith(".jpg", true) || it.name.endsWith(".jpeg", true) || it.name.endsWith(".webp", true)) }
                .forEach { file ->
                    println("TMP IMG: " + file.getAbsolutePath() + " - Size: " + file.length())
                }
        }
    }
}


