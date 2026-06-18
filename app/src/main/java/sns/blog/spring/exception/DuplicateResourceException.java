package sns.blog.spring.exception;

/**
 * Custom exception thrown when attempting to create a resource that already exists (e.g.,
 * registering an account with a username that is already taken). This exception is caught by the
 * GlobalExceptionHandler to return an HTTP 409 Conflict status.
 *
 * <p>Architectural Pattern: - Exception Handling Pattern: Custom RuntimeException for conveying
 * unique constraint violations.
 */
public class DuplicateResourceException extends RuntimeException {

  /**
   * Constructs a new DuplicateResourceException with the specified detail message.
   *
   * @param message the detail message explaining the reason for the conflict.
   */
  public DuplicateResourceException(String message) {
    super(message);
  }
}
