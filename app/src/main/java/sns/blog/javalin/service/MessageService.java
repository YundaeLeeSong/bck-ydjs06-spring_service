package sns.blog.javalin.service;

import java.util.List;
import sns.blog.javalin.dao.AccountDAO;
import sns.blog.javalin.dao.MessageDAO;
import sns.blog.javalin.model.Message;

/**
 * Service class for the Message entity. Responsible for handling all business logic, validation,
 * and connecting controller requests to the database via DAO.
 *
 * <p>Architectural Pattern: - Service Layer (3-Layer Architecture) pattern, mediating between
 * Controller and DAO components.
 */
public class MessageService {
  // Design Pattern: Dependency Injection. Uses both MessageDAO and AccountDAO for cross-entity
  // validation.
  private MessageDAO messageDAO;
  private AccountDAO accountDAO;

  /** Default constructor for MessageService. Instantiates dependencies automatically. */
  public MessageService() {
    this.messageDAO = new MessageDAO();
    this.accountDAO = new AccountDAO();
  }

  /**
   * Parameterized constructor for dependency injection.
   *
   * @param messageDAO Injected MessageDAO instance.
   * @param accountDAO Injected AccountDAO instance.
   */
  public MessageService(MessageDAO messageDAO, AccountDAO accountDAO) {
    this.messageDAO = messageDAO;
    this.accountDAO = accountDAO;
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
   * @return The newly created Message object with its generated messageId, or null if validation
   *     fails.
   */
  public Message createMessage(Message message) {
    // Implementation detail: Business validation checks for blank text and max-length constraint.
    if (message.getMessageText() == null
        || message.getMessageText().trim().isEmpty()
        || message.getMessageText().length() > 255) {
      return null;
    }

    // Implementation detail: Cross-reference accountDAO to ensure postedBy user exists.
    if (accountDAO.getAccountById(message.getPostedBy()) == null) {
      return null;
    }

    return messageDAO.createMessage(message);
  }

  /**
   * Retrieves all messages.
   *
   * <p>Implements specification: - System Doc Section 3.2: Retrieve All Messages.
   *
   * @return A List of all messages.
   */
  public List<Message> getAllMessages() {
    return messageDAO.getAllMessages();
  }

  /**
   * Retrieves a specific message based on its ID.
   *
   * <p>Implements specification: - System Doc Section 3.3: Retrieve Message by ID.
   *
   * @param messageId The ID of the message to retrieve.
   * @return The Message object, or null if not found.
   */
  public Message getMessageById(int messageId) {
    return messageDAO.getMessageById(messageId);
  }

  /**
   * Deletes a message by its ID and returns the deleted message object.
   *
   * <p>Implements specification: - System Doc Section 3.4: Delete Message by ID.
   *
   * @param messageId The ID of the message to be deleted.
   * @return The deleted Message object if it existed, or null if the message did not exist.
   */
  public Message deleteMessageById(int messageId) {
    Message message = messageDAO.getMessageById(messageId);
    // Implementation detail: Only attempt deletion if the message physically exists,
    // thus allowing us to return the previously existing state if successful.
    if (message != null) {
      boolean deleted = messageDAO.deleteMessageById(messageId);
      if (deleted) {
        return message;
      }
    }
    return null; // Could mean it didn't exist or deletion failed
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
   * @return The updated Message object, or null if validation or the database update fails.
   */
  public Message updateMessageText(int messageId, String newText) {
    // Implementation detail: Business logic verifying blank and length conditions.
    if (newText == null || newText.trim().isEmpty() || newText.length() > 255) {
      return null;
    }

    boolean updated = messageDAO.updateMessageText(messageId, newText);
    if (updated) {
      return messageDAO.getMessageById(messageId);
    }
    return null;
  }

  /**
   * Retrieves all messages posted by a specific user.
   *
   * <p>Implements specification: - System Doc Section 3.6: Retrieve Messages by Account ID.
   *
   * @param accountId The user ID to query messages for.
   * @return A List of Message objects posted by the given user.
   */
  public List<Message> getAllMessagesByAccountId(int accountId) {
    return messageDAO.getAllMessagesByAccountId(accountId);
  }
}
