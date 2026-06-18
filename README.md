# SNS Blog Backend

Gradle project with two interchangeable REST backends for a micro-blog API:

- **Javalin 7** — `sns.blog.javalin`
- **Spring Boot 3.3** — `sns.blog.spring`

Java 21. Entry point: `sns.blog.App`.

## Run
To run javalin API server, 
```bash
./gradlew run --args="javalin"
``` 

To run Spring API server, 
```bash
./gradlew run --args="spring"
``` 


## Test

```bash
./gradlew test
```

## Lint
To check formatting and naming
```bash
./gradlew :app:Ylint
```

To apply Spotless, then Checkstyle
```bash
./gradlew :app:Ylint --fix
```


Plugin and maintainer notes live in `buildSrc/src/main/kotlin/sns.blog.lint-conventions.gradle.kts`.

## Layout

| Path | Purpose |
| --- | --- |
| `app/src/main/java/sns/blog/javalin/` | Javalin implementation |
| `app/src/main/java/sns/blog/spring/` | Spring Boot implementation |
| `app/docs/` | Architecture notes (LaTeX and Markdown) |
| `buildSrc/` | Ylint convention plugin |
| `config/checkstyle/` | Naming rules for Checkstyle |
