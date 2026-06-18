package sns.blog.spring.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sns.blog.spring.entity.Account;
import sns.blog.spring.entity.Message;
import sns.blog.spring.service.AccountService;
import sns.blog.spring.service.MessageService;

/**
 * Controller class that defines all the HTTP endpoints and handles incoming web requests.
 *
 * <p>Architectural Pattern: - Controller Layer (3-Layer Architecture) pattern handling HTTP
 * routing, parsing JSON requests, and sending JSON responses via the Spring Web MVC framework. -
 * RESTful API Design Pattern: Provides stateless HTTP resource endpoints (GET, POST, DELETE,
 * PATCH). - Global Exception Handling: Note that this class does not contain explicit try-catch
 * blocks for business exceptions. Exceptions thrown by the Service layer are automatically
 * intercepted and handled by the @RestControllerAdvice configured in GlobalExceptionHandler.
 */
@RestController
public class SocialMediaController {

  // Design Pattern: Dependency Injection. Controller delegates business logic to services.
  private final AccountService accountService;
  private final MessageService messageService;

  /**
   * Constructor for SocialMediaController. Instantiates necessary service dependencies via Spring
   * Autowiring.
   *
   * @param accountService injected AccountService
   * @param messageService injected MessageService
   */
  @Autowired
  public SocialMediaController(AccountService accountService, MessageService messageService) {
    this.accountService = accountService;
    this.messageService = messageService;
  }

  /**
   * Handler for User Registration.
   *
   * <p>Implements specification: - System Doc Section 2.1: User Registration.
   *
   * @param account The Account object constructed from the JSON payload.
   * @return ResponseEntity with the created Account and status 200 on success, 409 on conflict, 400
   *     on Client error.
   */
  @PostMapping("/register")
  public ResponseEntity<Account> register(@RequestBody Account account) {
    // Exceptions (ClientValidationException, DuplicateResourceException) thrown by accountService
    // are intercepted by GlobalExceptionHandler, which returns the appropriate 400 or 409 status.
    Account createdAccount = accountService.registerAccount(account);
    // Implementation detail: Status 200 on success
    return ResponseEntity.status(HttpStatus.OK).body(createdAccount);
  }

  /**
   * Handler for User Login.
   *
   * <p>Implements specification: - System Doc Section 2.2: User Login.
   *
   * @param account The Account object constructed from the JSON payload.
   * @return ResponseEntity with the authenticated Account and status 200 on success, 401 on
   *     Unauthorized.
   */
  @PostMapping("/login")
  public ResponseEntity<Account> login(@RequestBody Account account) {
    // UnauthorizedException thrown by accountService on invalid credentials is intercepted
    // by GlobalExceptionHandler, which returns a 401 Unauthorized status.
    Account loggedInAccount = accountService.login(account);
    // Implementation detail: Status 200 on successful auth
    return ResponseEntity.status(HttpStatus.OK).body(loggedInAccount);
  }

  /**
   * Handler for creating a new Message.
   *
   * <p>Implements specification: - System Doc Section 3.1: Create New Message.
   *
   * @param message The Message object constructed from the JSON payload.
   * @return ResponseEntity with the created Message and status 200 on success, 400 on Client error.
   */
  @PostMapping("/messages")
  public ResponseEntity<Message> createMessage(@RequestBody Message message) {
    // ClientValidationException thrown by messageService is intercepted by GlobalExceptionHandler,
    // which returns a 400 Bad Request status.
    Message createdMessage = messageService.createMessage(message);
    // Implementation detail: Status 200 on success
    return ResponseEntity.status(HttpStatus.OK).body(createdMessage);
  }

  /**
   * Handler to retrieve all messages.
   *
   * <p>Implements specification: - System Doc Section 3.2: Retrieve All Messages.
   *
   * @return ResponseEntity containing a list of all messages and status 200.
   */
  @GetMapping("/messages")
  public ResponseEntity<List<Message>> getAllMessages() {
    // Implementation detail: Return JSON list. Returns empty [] via default List serialization if
    // no items exist.
    return ResponseEntity.status(HttpStatus.OK).body(messageService.getAllMessages());
  }

  /**
   * Handler to retrieve a single message by ID.
   *
   * <p>Implements specification: - System Doc Section 3.3: Retrieve Message by ID.
   *
   * @param messageId The ID of the message to retrieve from the path.
   * @return ResponseEntity containing the message (if found) and status 200.
   */
  @GetMapping("/messages/{messageId}")
  public ResponseEntity<Message> getMessageById(@PathVariable Integer messageId) {
    Message message = messageService.getMessageById(messageId);
    // Implementation detail: Returns empty response body with status 200 if not found.
    if (message != null) {
      return ResponseEntity.status(HttpStatus.OK).body(message);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  /**
   * Handler to delete a single message by ID.
   *
   * <p>Implements specification: - System Doc Section 3.4: Delete Message by ID.
   *
   * @param messageId The ID of the message to delete from the path.
   * @return ResponseEntity containing the number of rows affected (1 if existed) and status 200.
   */
  @DeleteMapping("/messages/{messageId}")
  public ResponseEntity<Integer> deleteMessageById(@PathVariable Integer messageId) {
    Integer rowsUpdated = messageService.deleteMessageById(messageId);
    // Implementation detail: Idempotent operation. Returns 1 if existed, otherwise empty body.
    if (rowsUpdated != null) {
      return ResponseEntity.status(HttpStatus.OK).body(rowsUpdated);
    } else {
      return ResponseEntity.status(HttpStatus.OK).build();
    }
  }

  /**
   * Handler to update the text of a single message by ID.
   *
   * <p>Implements specification: - System Doc Section 3.5: Update Message by ID.
   *
   * @param messageId The ID of the message to update from the path.
   * @param message The Message object payload containing the new messageText.
   * @return ResponseEntity containing 1 (rows updated) on success with status 200, 400 on Client
   *     error.
   */
  @PatchMapping("/messages/{messageId}")
  public ResponseEntity<Integer> updateMessageText(
      @PathVariable Integer messageId, @RequestBody Message message) {
    // ClientValidationException thrown by messageService is intercepted by GlobalExceptionHandler,
    // which returns a 400 Bad Request status.
    Integer rowsUpdated = messageService.updateMessageText(messageId, message.getMessageText());
    // Implementation detail: Status 200 + rows updated on success
    return ResponseEntity.status(HttpStatus.OK).body(rowsUpdated);
  }

  /**
   * Handler to retrieve all messages posted by a single user account.
   *
   * <p>Implements specification: - System Doc Section 3.6: Retrieve Messages by Account ID.
   *
   * @param accountId The ID of the user account from the path.
   * @return ResponseEntity containing a list of messages and status 200.
   */
  @GetMapping("/accounts/{accountId}/messages")
  public ResponseEntity<List<Message>> getMessagesByAccountId(@PathVariable Integer accountId) {
    // Implementation detail: Return JSON list, handles empty state safely.
    return ResponseEntity.status(HttpStatus.OK)
        .body(messageService.getAllMessagesByAccountId(accountId));
  }
}
