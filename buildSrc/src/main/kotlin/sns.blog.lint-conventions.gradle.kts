import sns.blog.YlintTask
    
/*
 * [Linting Conventions]
 * Configures Checkstyle and Spotless for static analysis and code formatting (linting).
 * - Checkstyle:    validates naming conventions and code quality rules without modifying source files.
 * - Spotless:      fixes code formatting, import optimization, and whitespace cleanup. 
 *                  (It can be either check-only or auto-fix based on the --fix flag.)
 */
plugins {
    checkstyle
    id("com.diffplug.spotless")
}

/*
 * [Checkstyle audit-only]
 * Scans Java files and fails the build on bad names and code quality rules as follows.
 * 1. naming conventions (e.g., class names, method names, field names, package names)
 * 2. code quality rules (e.g., no empty catch blocks, no magic numbers, no unused variables/imports, no javadoc issues)
 *
 * It never edits your files or rename/refactor/modify codes with violations.
 */
checkstyle {
    toolVersion = "10.16.0"
    /*
     * [Past version]
     * configFile = rootProject.file("config/checkstyle/checkstyle.xml")  // Checkstyle config
     *
     * At runtime checkstyle.xml is packed inside buildSrc.jar as a classpath resource.
     * - 'configFile' requires a plain filesystem File and cannot accept a jar: URL.
     * - 'config' allows us to read the XML from the classpath and pass it as a TextResource.

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

/**
 * [Spotless Configuration]
 * Across Java source sets, it enforces and handles
 * 1. unified formatting with google-java-format:
 *    - Java code: indentation, line breaks, spacing
 *    - Javadoc: preserves multi-paragraph structure by inserting <p> tags for blank lines
 * 2. import optimization (e.g., removing unused imports, sorting imports), and
 * 3. automatically corrects whitespace layout issues
 *
 * It can be run in two modes:
 * - Check-only: validates formatting and fails on violations without modifying files.
 * - Auto-fix (--fix): applies formatting corrections before Checkstyle validation.
 */
spotless {
    java {
        /*
         * [Spotless opt-out]
         * Allows a Java file to opt out (bypassed) of auto-format for custom block layouts.
         * In source, wrap the block:
         *   // spotless:off
         *   ... lines you want left as-is ...
         *   // spotless:on
         * 
         * Use when --fix would break intentional layout (aligned columns, ASCII art).
         * [Caution] Checkstyle still runs on the full file, only Spotless skips that block.
         */
        toggleOffOn()
        target("src/main/java/**/*.java", "src/test/java/**/*.java")
        /*
         * [Javadoc HTML]
         * Blank lines between Javadoc paragraphs trigger <p> insertion; markdown-style breaks
         * are not preserved. Prefer explicit HTML (e.g., <p>, <ul>, <li>) for multi-paragraph docs.
         */
        googleJavaFormat("1.22.0")
        removeUnusedImports()
        trimTrailingWhitespace()
        endWithNewline()
    }
}

// Register the custom aggregation task for project linting
tasks.register<YlintTask>("Ylint") {
    dependsOn("checkstyleMain", "checkstyleTest")
}

/*
 * Dynamic task graph for Ylint.
 *
 * The --fix flag is a runtime @Option, so it is not known when the register block
 * above runs. whenReady intercepts the graph only when Ylint is targeted and wires
 * Spotless in based on that flag:
 *   --fix        -> spotlessApply runs before Checkstyle (format, then lint)
 *   no --fix     -> spotlessCheck runs in parallel with Checkstyle
 */
gradle.taskGraph.whenReady {
    val ylint = tasks.findByName("Ylint") as? YlintTask ?: return@whenReady
    if (!hasTask(ylint)) return@whenReady

    val spotlessName = if (ylint.fix) "spotlessApply" else "spotlessCheck"
    val checkstyleTasks = listOf("checkstyleMain", "checkstyleTest")

    if (ylint.fix) {
        // Inject a strict ordering constraint: format files before linting them
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
