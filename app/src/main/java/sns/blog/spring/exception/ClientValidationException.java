package sns.blog.spring.exception;

/**
 * Custom exception thrown when client input validation fails. This exception is caught by the
 * GlobalExceptionHandler to return an HTTP 400 Bad Request status.
 *
 * <p>Architectural Pattern: - Exception Handling Pattern: Custom RuntimeException for conveying
 * business validation failures.
 */
public class ClientValidationException extends RuntimeException {

  /**
   * Constructs a new ClientValidationException with the specified detail message.
   *
   * @param message the detail message explaining the reason for the validation failure.
   */
  public ClientValidationException(String message) {
    super(message);
  }
}
