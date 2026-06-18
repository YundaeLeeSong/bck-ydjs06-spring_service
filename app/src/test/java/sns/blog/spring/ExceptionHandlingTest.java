package sns.blog.spring;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;

/**
 * Dedicated test class to explicitly verify the GlobalExceptionHandler logic. Tests that custom
 * exceptions are correctly intercepted and mapped to the appropriate HTTP status codes (400, 401,
 * 409).
 */
public class ExceptionHandlingTest {
  ApplicationContext app;
  HttpClient webClient;

  /**
   * Before every test, reset the database, restart the app, and create a new webClient for
   * interacting locally on the web.
   *
   * @throws InterruptedException
   */
  @BeforeEach
  public void setUp() throws InterruptedException {
    webClient = HttpClient.newHttpClient();
    String[] args = new String[] {};
    app = SpringApplication.run(SocialMediaApp.class, args);
    Thread.sleep(500);
  }

  @AfterEach
  public void tearDown() throws InterruptedException {
    Thread.sleep(500);
    SpringApplication.exit(app);
  }

  /**
   * Tests that ClientValidationException maps to 400 Bad Request. Triggers the exception by
   * attempting to register an account with a blank username.
   */
  @Test
  public void testClientValidationExceptionReturns400() throws IOException, InterruptedException {
    String json = "{\"username\":\"\",\"password\":\"password\"}"; // Blank username triggers
    // ClientValidationException
    HttpRequest postRequest =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/register"))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .header("Content-Type", "application/json")
            .build();
    HttpResponse<String> response =
        webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

    Assertions.assertEquals(
        400,
        response.statusCode(),
        "Expected GlobalExceptionHandler to catch ClientValidationException and return 400");
  }

  /**
   * Tests that DuplicateResourceException maps to 409 Conflict. Triggers the exception by
   * registering the same username twice.
   */
  @Test
  public void testDuplicateResourceExceptionReturns409() throws IOException, InterruptedException {
    String json = "{\"username\":\"duplicateUser\",\"password\":\"password\"}";
    HttpRequest postRequest =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/register"))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .header("Content-Type", "application/json")
            .build();

    // First request should succeed
    webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

    // Second request with same username triggers DuplicateResourceException
    HttpResponse<String> response2 =
        webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

    Assertions.assertEquals(
        409,
        response2.statusCode(),
        "Expected GlobalExceptionHandler to catch DuplicateResourceException and return 409");
  }

  /**
   * Tests that UnauthorizedException maps to 401 Unauthorized. Triggers the exception by attempting
   * to login with an invalid username.
   */
  @Test
  public void testUnauthorizedExceptionReturns401() throws IOException, InterruptedException {
    String json =
        "{\"username\":\"nonExistentUser\",\"password\":\"password\"}"; // Invalid user triggers
    // UnauthorizedException
    HttpRequest postRequest =
        HttpRequest.newBuilder()
            .uri(URI.create("http://localhost:8080/login"))
            .POST(HttpRequest.BodyPublishers.ofString(json))
            .header("Content-Type", "application/json")
            .build();
    HttpResponse<String> response =
        webClient.send(postRequest, HttpResponse.BodyHandlers.ofString());

    Assertions.assertEquals(
        401,
        response.statusCode(),
        "Expected GlobalExceptionHandler to catch UnauthorizedException and return 401");
  }
}
