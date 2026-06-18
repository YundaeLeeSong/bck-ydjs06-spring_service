# Publishing Ylint

Planning notes for extracting Ylint from `buildSrc/` and publishing it like any other Gradle plugin (private Maven, GitHub Packages, or Gradle Plugin Portal).

Official references:

- [Convention plugins](https://docs.gradle.org/current/userguide/implementing_gradle_plugins_convention.html)
- [Publishing convention plugins (multi-repo sample)](https://docs.gradle.org/current/samples/sample_publishing_convention_plugins.html)
- [Gradle Plugin Portal](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html)
- [Structuring builds (`buildSrc` vs included build)](https://docs.gradle.org/current/userguide/best_practices_structuring_builds.html)

## Current state

| Artifact | Path | Role |
| --- | --- | --- |
| Convention plugin | `buildSrc/src/main/kotlin/sns.blog.lint-conventions.gradle.kts` | Applies Spotless + Checkstyle, registers `Ylint` |
| Task class | `buildSrc/src/main/kotlin/sns/blog/YlintTask.kt` | `--fix` flag and log orchestration |
| Plugin build | `buildSrc/build.gradle.kts` | `kotlin-dsl`, Spotless API dependency |
| Checkstyle rules | `config/checkstyle/checkstyle.xml` | Referenced via `rootProject.file(...)` |
| Consumer | `app/build.gradle.kts` | `id("sns.blog.lint-conventions")` |

`buildSrc/` is compiled into the current build only. It cannot be pushed to a registry as-is.

## Target outcomes

Pick one primary goal before starting:

| Goal | End state |
| --- | --- |
| **A. Same repo, publishable module** | Replace `buildSrc/` with `ylint-plugin/`; use `includeBuild` locally; `publish` to a Maven repo when ready |
| **B. Separate repo** | New repository containing only the plugin; this project consumes a versioned coordinate |
| **C. Public plugin** | Same as A or B, plus `com.gradle.plugin-publish` and [plugins.gradle.org](https://plugins.gradle.org/) |

For a first migration, **A** is the lowest risk: one PR, `./gradlew :app:Ylint` keeps working, publishing is optional until credentials exist.

## Proposed layout (goal A)

```
sns-blog-backend/
├── settings.gradle.kts          # includeBuild("ylint-plugin")
├── ylint-plugin/
│   ├── build.gradle.kts         # kotlin-dsl + java-gradle-plugin + maven-publish
│   └── src/main/
│       ├── kotlin/sns/blog/YlintTask.kt
│       ├── kotlin/sns.blog.lint-conventions.gradle.kts
│       └── resources/checkstyle/checkstyle.xml   # move rules here
├── config/checkstyle/           # remove after bundling (or keep as override example)
└── app/build.gradle.kts         # unchanged plugin id
```

Plugin ID stays `sns.blog.lint-conventions` so consumers need no rename.

Suggested Maven coordinates:

```
group:    sns.blog
artifact: ylint-gradle-plugin
version:  0.1.0
```

## Blocker to resolve first: Checkstyle config

Today the convention script reads a file outside the plugin:

```kotlin
configFile = rootProject.file("config/checkstyle/checkstyle.xml")
```

Published plugins cannot assume that path exists. Choose one approach:

| Option | Pros | Cons |
| --- | --- | --- |
| **Bundle in JAR** (recommended) | Single artifact; works out of the box | Rule updates require a plugin release |
| **Consumer supplies file** | Flexible per project | Extra setup; easy to misconfigure |
| **Separate config artifact** | Version rules independently | Two coordinates to manage |

Bundled resource sketch:

```kotlin
checkstyle {
    configFile = file(
        YlintTask::class.java.getResource("/checkstyle/checkstyle.xml")!!.toURI()
    )
}
```

Add an extension property later if overrides are needed:

```kotlin
ylint {
    checkstyleConfig = layout.projectDirectory.file("config/checkstyle/checkstyle.xml")
}
```

## Migration checklist

### Phase 1 — Extract (no publishing yet)

- [ ] Create `ylint-plugin/` with `kotlin-dsl` (copy from `buildSrc/build.gradle.kts`)
- [ ] Move `YlintTask.kt` and `sns.blog.lint-conventions.gradle.kts` into `ylint-plugin/src/main/kotlin/`
- [ ] Move `config/checkstyle/checkstyle.xml` into `ylint-plugin/src/main/resources/checkstyle/`
- [ ] Update `configFile` to load the bundled resource
- [ ] Add `includeBuild("ylint-plugin")` to `settings.gradle.kts`
- [ ] Remove `buildSrc/` after `./gradlew :app:Ylint` and `./gradlew :app:test` pass
- [ ] Delete or trim `config/checkstyle/` if no longer used

### Phase 2 — Make the module publishable

Add to `ylint-plugin/build.gradle.kts`:

```kotlin
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
    `maven-publish`
}

group = "sns.blog"
version = "0.1.0"

gradlePlugin {
    plugins {
        create("lintConventions") {
            id = "sns.blog.lint-conventions"
            implementationClass = "sns.blog.LintConventionsPlugin" // only if converting to a class plugin
        }
    }
}

// Precompiled script plugins (sns.blog.lint-conventions.gradle.kts) are discovered
// automatically by kotlin-dsl; java-gradle-plugin adds marker artifacts for the
// plugins { id("...") version "..." } DSL.

publishing {
    repositories {
        maven {
            name = "local"
            url = layout.buildDirectory.dir("repo")
        }
        // maven {
        //     name = "github"
        //     url = uri("https://maven.pkg.github.com/OWNER/REPO")
        //     credentials { ... }
        // }
    }
}
```

Verify locally:

```bash
./gradlew :ylint-plugin:publish
```

Inspect `ylint-plugin/build/repo/sns/blog/ylint-gradle-plugin/0.1.0/`.

- [ ] Add `java-gradle-plugin` and `maven-publish`
- [ ] Set `group` / `version`
- [ ] Configure at least one `publishing.repositories` target
- [ ] Run `publish` and confirm marker + implementation JARs exist

### Phase 3 — Consume from a registry

In a **different** project (or this one after removing `includeBuild`):

`settings.gradle.kts`:

```kotlin
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            url = uri("https://your.repo/releases")
            // credentials { ... }
        }
    }
}
```

`app/build.gradle.kts`:

```kotlin
plugins {
    id("sns.blog.lint-conventions") version "0.1.0"
}
```

- [ ] Publish `0.1.0` to chosen registry
- [ ] Point `pluginManagement` at that registry
- [ ] Replace `includeBuild` with versioned `plugins { }` block
- [ ] Confirm `./gradlew :app:Ylint` on a clean clone without `ylint-plugin/` source

### Phase 4 — Public Plugin Portal (optional)

Only if the plugin should be public:

- [ ] Register at [plugins.gradle.org](https://plugins.gradle.org/)
- [ ] Apply `id("com.gradle.plugin-publish") version "1.x"`
- [ ] Add `gradle.properties` keys: `gradle.publish.key`, `gradle.publish.secret` (never commit)
- [ ] Run `./gradlew publishPlugin --validate-only`, then `publishPlugin`

Docs: [Publishing Plugins to the Gradle Plugin Portal](https://docs.gradle.org/current/userguide/publishing_gradle_plugins.html)

## What gets published

| Output | Purpose |
| --- | --- |
| Implementation JAR | `YlintTask`, convention script, bundled `checkstyle.xml` |
| Plugin marker artifact | Lets Gradle resolve `id("sns.blog.lint-conventions")` without `buildscript` classpath |
| POM | Transitive metadata (Spotless plugin dependency should appear here) |

Spotless is already an `implementation` dependency in `buildSrc`; keep the same in `ylint-plugin` so consumers do not add Spotless manually.

## Versioning and releases

Suggested practice:

- Start at `0.1.0` while the API (plugin id, task name, `--fix` behavior) may still change
- Bump **patch** for checkstyle rule tweaks bundled in the JAR
- Bump **minor** for new extension properties or optional behavior
- Bump **major** if plugin id, task graph, or default rules change in breaking ways

Tag releases in git matching the published version.

## Credentials (do not commit)

| Target | Typical setup |
| --- | --- |
| Local / CI smoke test | `ylint-plugin/build/repo` (no credentials) |
| GitHub Packages | `GITHUB_ACTOR` + `GITHUB_TOKEN` in CI |
| Nexus / Artifactory | Username + password or token in `~/.gradle/gradle.properties` |
| Plugin Portal | `gradle.publish.key` + `gradle.publish.secret` in `~/.gradle/gradle.properties` |

Example `~/.gradle/gradle.properties`:

```properties
ylint.repo.url=https://maven.pkg.github.com/OWNER/ylint
ylint.repo.user=github-actor
ylint.repo.password=ghp_...
```

Reference those properties from `publishing.repositories` via `findProperty`.

## What stays in this repo after publishing

| Keep | Remove or stop tracking |
| --- | --- |
| `app/` application code | `buildSrc/` (after extraction) |
| `gradle/wrapper/` | Duplicated checkstyle XML (if bundled) |
| `README.md` lint section (commands only) | Session / planning noise in README |
| Optional: `config/checkstyle/` as documented override example | |

`PUBLISH.md` can be archived or deleted once migration is complete.

## Quick validation commands

```bash
export JAVA_HOME="/path/to/ext/jdk-21"

# After Phase 1 (includeBuild)
./gradlew :app:Ylint
./gradlew :app:Ylint --fix
./gradlew :app:test

# After Phase 2 (publish locally)
./gradlew :ylint-plugin:publish
ls ylint-plugin/build/repo/sns/blog/

# After Phase 3 (versioned consumer)
./gradlew :app:Ylint --refresh-dependencies
```

## Open decisions (fill in before Phase 2)

| Decision | Choice |
| --- | --- |
| Registry | _local / GitHub Packages / Nexus / Plugin Portal_ |
| Repo layout | _same repo (`ylint-plugin/`) / separate repo_ |
| Checkstyle config | _bundle (recommended) / consumer file / separate artifact_ |
| Group ID | _default: `sns.blog`_ |
| Plugin ID | _keep: `sns.blog.lint-conventions`_ |
