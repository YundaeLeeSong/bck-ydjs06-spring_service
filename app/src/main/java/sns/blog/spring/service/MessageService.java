package sns.blog.spring.service;

import java.util.List;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sns.blog.spring.entity.Message;
import sns.blog.spring.exception.ClientValidationException;
import sns.blog.spring.repository.AccountRepository;
import sns.blog.spring.repository.MessageRepository;

/**
 * Service class for the Message entity. Responsible for handling all business logic, validation,
 * and connecting controller requests to the database via Repositories.
 *
 * <p>Architectural Pattern: - Service Layer (3-Layer Architecture) pattern, mediating between
 * Controller and Repository components.
 */
@Service
public class MessageService {

  // Design Pattern: Dependency Injection. Uses both MessageRepository and AccountRepository for
  // cross-entity validation.
  private final MessageRepository messageRepository;
  private final AccountRepository accountRepository;

  /**
   * Constructor for dependency injection.
   *
   * @param messageRepository Injected MessageRepository instance.
   * @param accountRepository Injected AccountRepository instance.
   */
  @Autowired
  public MessageService(MessageRepository messageRepository, AccountRepository accountRepository) {
    this.messageRepository = messageRepository;
    this.accountRepository = accountRepository;
  }

  /**
   * Validates and persists a new message.
   *
   * <p>Implements specification: - System Doc Section 3.1: Create New Message.
   *
   * <p>Validation rules: 1. messageText must not be null or blank. 2. messageText must be under 255
   * characters. 3. postedBy must refer to a real, existing user.
   *
   * @param message The requested Message object to create.
   * @return The newly created Message object with its generated messageId.
   * @throws ClientValidationException if validation fails.
   */
  public Message createMessage(Message message) {
    // Implementation detail: Business validation checks for blank text and max-length constraint.
    if (message.getMessageText() == null || message.getMessageText().trim().isEmpty()) {
      throw new ClientValidationException("Message text cannot be blank");
    }
    if (message.getMessageText().length() > 255) {
      throw new ClientValidationException("Message text cannot be over 255 characters");
    }
    // Implementation detail: Cross-reference accountRepository to ensure postedBy user exists.
    if (message.getPostedBy() == null || !accountRepository.existsById(message.getPostedBy())) {
      throw new ClientValidationException("Posted by user does not exist");
    }
    return messageRepository.save(message);
  }

  /**
   * Retrieves all messages.
   *
   * <p>Implements specification: - System Doc Section 3.2: Retrieve All Messages.
   *
   * @return A List of all messages.
   */
  public List<Message> getAllMessages() {
    return messageRepository.findAll();
  }

  /**
   * Retrieves a specific message based on its ID.
   *
   * <p>Implements specification: - System Doc Section 3.3: Retrieve Message by ID.
   *
   * @param messageId The ID of the message to retrieve.
   * @return The Message object, or null if not found.
   */
  public Message getMessageById(Integer messageId) {
    return messageRepository.findById(messageId).orElse(null);
  }

  /**
   * Deletes a message by its ID and returns the number of rows updated.
   *
   * <p>Implements specification: - System Doc Section 3.4: Delete Message by ID.
   *
   * @param messageId The ID of the message to be deleted.
   * @return 1 if the message existed and was deleted, or null if the message did not exist.
   */
  public Integer deleteMessageById(Integer messageId) {
    // Implementation detail: Only attempt deletion if the message physically exists.
    if (messageRepository.existsById(messageId)) {
      messageRepository.deleteById(messageId);
      return 1; // 1 row updated
    }
    return null; // Signal that the message did not exist
  }

  /**
   * Validates and updates the text of an existing message.
   *
   * <p>Implements specification: - System Doc Section 3.5: Update Message by ID.
   *
   * <p>Validation rules: 1. The message ID must exist. 2. The new message text must not be blank.
   * 3. The new message text length must not exceed 255 characters.
   *
   * @param messageId The ID of the message to update.
   * @param newText The new text payload.
   * @return 1 indicating the number of rows updated.
   * @throws ClientValidationException if validation or the database update fails.
   */
  public Integer updateMessageText(Integer messageId, String newText) {
    // Implementation detail: Business logic verifying blank and length conditions.
    if (newText == null || newText.trim().isEmpty()) {
      throw new ClientValidationException("Message text cannot be blank");
    }
    if (newText.length() > 255) {
      throw new ClientValidationException("Message text cannot be over 255 characters");
    }
    Optional<Message> optionalMessage = messageRepository.findById(messageId);
    if (optionalMessage.isPresent()) {
      Message message = optionalMessage.get();
      message.setMessageText(newText);
      messageRepository.save(message);
      return 1; // 1 row updated
    }
    throw new ClientValidationException("Message not found");
  }

  /**
   * Retrieves all messages posted by a specific user.
   *
   * <p>Implements specification: - System Doc Section 3.6: Retrieve Messages by Account ID.
   *
   * @param accountId The user ID to query messages for.
   * @return A List of Message objects posted by the given user.
   */
  public List<Message> getAllMessagesByAccountId(Integer accountId) {
    return messageRepository.findByPostedBy(accountId);
  }
}
