package sns.blog.spring.exception;

/**
 * Custom exception thrown when authentication fails due to invalid credentials. This exception is
 * caught by the GlobalExceptionHandler to return an HTTP 401 Unauthorized status.
 *
 * <p>Architectural Pattern: - Exception Handling Pattern: Custom RuntimeException for conveying
 * authentication failures.
 */
public class UnauthorizedException extends RuntimeException {

  /**
   * Constructs a new UnauthorizedException with the specified detail message.
   *
   * @param message the detail message explaining the reason for the unauthorized access.
   */
  public UnauthorizedException(String message) {
    super(message);
  }
}
