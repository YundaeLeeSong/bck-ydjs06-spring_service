# Ylint

Developer reference for the `Ylint` custom Gradle task and its `sns.blog.lint-conventions`
convention plugin, both contained in `buildSrc/`.

Official references:

- [Pre-compiled script plugins](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html)
- [Checkstyle plugin](https://docs.gradle.org/current/userguide/checkstyle_plugin.html)
- [Spotless plugin](https://github.com/diffplug/spotless/tree/main/plugin-gradle)
- [buildSrc sharing build logic](https://docs.gradle.org/current/userguide/sharing_build_logic_between_subprojects.html)

## What Ylint does

`./gradlew :app:Ylint` runs two checks in sequence:

| Step | Tool | What it enforces |
| --- | --- | --- |
| Formatting | Spotless (google-java-format) | Indentation, imports, whitespace, newlines |
| Naming | Checkstyle | Package, class, method, field, parameter, local variable, constant naming |

`./gradlew :app:Ylint --fix` runs `spotlessApply` before Checkstyle so formatting
violations are corrected automatically. Naming violations are never auto-fixed; they
must be corrected by hand or with an IDE rename refactor.

## File layout

| File | Role |
| --- | --- |
| `buildSrc/build.gradle.kts` | Declares `kotlin-dsl` and Spotless API dependency |
| `buildSrc/src/main/kotlin/sns.blog.lint-conventions.gradle.kts` | Convention plugin: configures Spotless and Checkstyle, registers `Ylint` task |
| `buildSrc/src/main/kotlin/sns/blog/YlintTask.kt` | Custom task class with `--fix` flag |
| `buildSrc/src/main/resources/sns/blog/checkstyle/checkstyle.xml` | Checkstyle naming rules, bundled inside `buildSrc.jar` |

`buildSrc/` is build tooling only. It is never included in the application artifact.

## Plugin ID convention

The convention plugin file uses the dot-separated flat naming convention:

```
sns.blog.lint-conventions.gradle.kts   ->   plugin id "sns.blog.lint-conventions"
```

Gradle derives the plugin ID directly from the filename. The file must sit directly
under `src/main/kotlin/`, not in a subdirectory, for this to work without a package
declaration (which `.gradle.kts` files do not support).

The `YlintTask.kt` class file uses the standard subdirectory layout (`sns/blog/`) because
it is a regular Kotlin source file and its `package sns.blog` declaration is what
determines its fully qualified name.

## Checkstyle config: bundled in buildSrc

`checkstyle.xml` is stored in `buildSrc/src/main/resources/` and bundled into
`buildSrc.jar` at compile time. It is loaded at configuration time via:

```kotlin
val xmlText = checkNotNull(
    Thread.currentThread().contextClassLoader
        .getResourceAsStream("sns/blog/checkstyle/checkstyle.xml")
) { "checkstyle.xml not found on buildSrc classpath" }
    .bufferedReader()
    .readText()
config = resources.text.fromString(xmlText)
```

### Why not configFile

`CheckstyleExtension.configFile` requires a plain filesystem `File`. At runtime the
resource lives inside `buildSrc.jar` as a `jar:file://...` URL, which Gradle cannot
convert to a `File`. `resources.text.fromString` accepts a `TextResource` instead and
bypasses the filesystem requirement.

### Why not config/checkstyle/ at the project root

The Checkstyle plugin defaults to `config/checkstyle/checkstyle.xml` relative to the
root project. That is the most common industry convention and requires no code in the
plugin at all. Bundling inside `buildSrc` is a valid alternative when the goal is to
keep all linting configuration co-located with the plugin that enforces it.

Both approaches are accepted practice. The Gradle documentation shows the `config/`
path as the default; bundling via classpath resource is documented in community
discussions as the pattern for reusable plugins.

### configFile vs config

`configFile` and `config` are properties of `CheckstyleExtension` -- the receiver
object inside the `checkstyle { }` block. They are not Kotlin reserved words or Gradle
core API. They are defined by the Checkstyle Gradle plugin and are only in scope inside
that block.

This pattern is the same for every Gradle plugin extension:

```kotlin
checkstyle { }   // receiver: CheckstyleExtension  -- configFile, config, toolVersion, ...
spotless  { }   // receiver: SpotlessExtension     -- java { }, kotlin { }, ...
java      { }   // receiver: JavaPluginExtension   -- toolchain, sourceCompatibility, ...
```

Outside those blocks the names are not in scope.

`CheckstyleExtension` exposes two mutually exclusive ways to supply the XML rules:

| Property | Type | When to use |
| --- | --- | --- |
| `configFile` | `File` | File exists on disk at a known filesystem path. Gradle default is `config/checkstyle/checkstyle.xml` relative to the root project. Setting this is enough and requires no extra code. |
| `config` | `TextResource` | XML content is not a plain file (inside a jar, generated, fetched remotely). `resources.text.fromString(...)` creates a `TextResource` from a string. |

Setting one overrides the other. Internally the Checkstyle plugin converts `configFile`
into a `TextResource` anyway; `config` skips that step.

The reason this project uses `config` instead of `configFile`:

At build time `checkstyle.xml` is compiled into `buildSrc.jar` as a classpath resource.
At runtime Gradle resolves `buildSrc.jar` from its transform cache, so the resource URL
is a `jar:file://...` path, not a plain `file://...` path. `configFile` only accepts a
plain filesystem `File` and throws when given a jar-scheme URL. `config` accepts a
`TextResource` built from the string content, which has no filesystem path requirement.

If `checkstyle.xml` is ever moved out of `buildSrc` and back to `config/checkstyle/`
at the project root, replace the `config` block with the simpler one-liner:

```kotlin
// configFile = rootProject.file("config/checkstyle/checkstyle.xml")
```

and delete the `xmlText` loading code entirely.

### Caveats

| Caveat | Detail |
| --- | --- |
| Configuration cache invalidation | The XML content is recorded as a string value in the cache. Editing `checkstyle.xml` alone does not invalidate the cache because the file is inside `buildSrc.jar`, not a tracked filesystem input. Any other change to `buildSrc` sources forces a recompile and invalidates the cache. Running `--rerun-tasks` also forces re-evaluation. |
| Context class loader | `Thread.currentThread().contextClassLoader` is the only reliable loader in a precompiled script plugin context. `this.javaClass.classLoader` may not carry the `buildSrc` resources. |
| Publishing | If Ylint is ever extracted to a standalone published plugin (see `PUBLISH.md`), the same classpath resource approach works unchanged. No migration needed for the config loading code. |

## buildSrc/settings.gradle.kts

This file is not present and is not required. Gradle 8 treats `buildSrc` as an implicit
included build without it. The file would suppress a deprecation warning about missing
settings in Gradle 9, but it also causes the configuration cache to invalidate on every
run with the message "file buildSrc/settings.gradle.kts has been removed" after deletion.
The current working state omits it intentionally.

## Task graph

`Ylint` depends on `checkstyleMain` and `checkstyleTest`. Spotless is wired in
dynamically via `gradle.taskGraph.whenReady` because the `--fix` flag is a runtime
`@Option` and is not available at registration time.

```
Ylint
  +-- checkstyleMain
  |     +-- spotlessApply   (only when --fix)
  +-- checkstyleTest
  |     +-- spotlessApply   (only when --fix)
  +-- spotlessCheck          (only when not --fix, runs in parallel)
```

## Spotless opt-out

A file can opt out of auto-formatting for a specific block:

```java
// spotless:off
// ... lines left as-is ...
// spotless:on
```

Checkstyle still runs on the full file. Only Spotless skips the marked block.
Use this for intentionally aligned columns or generated sections that must not be
reformatted.
