package sns.blog

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.options.Option

/**
 * Runs Spotless and Checkstyle on Java sources in the applying module.
 *
 * Invoke from the project root with `./gradlew Ylint` to check, or
 * `./gradlew Ylint --fix` to apply Spotless formatting before the Checkstyle audit.
 *
 * @author Jaehoon Song
 * @since 0.1.0
 */
abstract class YlintTask : DefaultTask() {

    /**
     * The `--fix` command-line flag.
     *
     * When `true`, runs `spotlessApply` before Checkstyle.
     * When `false`, runs `spotlessCheck` alongside Checkstyle.
     *
     * @since 0.1.0
     */
    @set:Option(option = "fix", description = "Apply Spotless formatting, then run Checkstyle")
    @get:Input
    var fix: Boolean = false

    init {
        group = "verification"
        description = "Run Spotless and Checkstyle (see sns.blog.lint-conventions.gradle.kts)."
    }

    @TaskAction
    fun runLint() {
        val action = if (fix) "Fixing" else "Checking"
        logger.lifecycle("Ylint: $action formatting (Spotless) and naming (Checkstyle)...")
        /*
         * [Task graph]
         * This method only prints a log line. Gradle runs the real checks via
         * task dependencies defined in sns.blog.lint-conventions.gradle.kts
         * (spotlessCheck or spotlessApply, then checkstyleMain and checkstyleTest).
         */
    }
}
