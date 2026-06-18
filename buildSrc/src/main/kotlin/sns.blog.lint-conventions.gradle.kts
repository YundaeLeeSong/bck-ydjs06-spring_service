import sns.blog.YlintTask

plugins {
    checkstyle
    id("com.diffplug.spotless")
}

/*
 * [Checkstyle audit-only]
 * Scans Java files and fails the build on bad names (package, class, field, etc.).
 * It never edits your files. Rename violations by hand or with a refactor.
 * Spacing and imports are Spotless's job, not Checkstyle's.
 */
checkstyle {
    toolVersion = "10.16.0"
    /*
     * [Past version]
     * configFile = rootProject.file("config/checkstyle/checkstyle.xml")  // Checkstyle config
     *
     * At runtime checkstyle.xml is packed inside buildSrc.jar as a classpath resource.
     * configFile requires a plain filesystem File and cannot accept a jar: URL.
     * Switched to resources.text.fromString so the XML is read from the buildSrc
     * classpath and passed as a TextResource, keeping the config fully inside buildSrc.
     */
    val xmlText = checkNotNull(
        Thread.currentThread().contextClassLoader
            .getResourceAsStream("sns/blog/checkstyle/checkstyle.xml")
    ) { "checkstyle.xml not found on buildSrc classpath" }
        .bufferedReader()
        .readText()
    config = resources.text.fromString(xmlText)  // Checkstyle config
    isIgnoreFailures = false
    maxWarnings = 0
}

spotless {
    java {
        /*
         * [Spotless opt-out]
         * Allows a Java file to opt out of auto-format for one section.
         * In source, wrap the block:
         *   // spotless:off
         *   ... lines you want left as-is ...
         *   // spotless:on
         * Use when --fix would break intentional layout (aligned columns, ASCII art).
         * Checkstyle still runs on the full file; only Spotless skips that block.
         */
        toggleOffOn()
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        /*
         * [Javadoc HTML]
         * google-java-format treats Javadoc as text unless you use HTML tags.
         * A line like "- step one" is not a list; it gets merged into one long line.
         * Use <ul>/<li> for bullets, <ol>/<li> for numbered steps, <p> for paragraphs.
         * We fixed this in SocialMediaController, AccountService, and
         * DeleteMessageByMessageIdTest (expected "Status Code" in <li>).
         */
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

tasks.register<YlintTask>("Ylint") {
    dependsOn("checkstyleMain", "checkstyleTest")
}

/*
 * [Task graph --fix]
 * The fix flag is a runtime @Option, so it is not available when the register
 * block above runs. whenReady wires Spotless into the graph only when Ylint is
 * requested: spotlessApply before Checkstyle for --fix, spotlessCheck in parallel
 * otherwise.
 */
gradle.taskGraph.whenReady {
    val ylint = tasks.findByName("Ylint") as? YlintTask ?: return@whenReady
    if (!hasTask(ylint)) return@whenReady

    val spotlessName = if (ylint.fix) "spotlessApply" else "spotlessCheck"
    val checkstyleTasks = listOf("checkstyleMain", "checkstyleTest")

    if (ylint.fix) {
        checkstyleTasks.forEach { name ->
            tasks.named(name).configure {
                dependsOn(spotlessName)
                mustRunAfter(spotlessName)
            }
        }
    } else {
        ylint.dependsOn(spotlessName)
    }
}
