package sns.blog.javalin.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import sns.blog.javalin.model.Message;
import sns.blog.javalin.util.ConnectionUtil;

/**
 * Data Access Object (DAO) class for the Message entity. This class encapsulates all database
 * operations related to the Message table.
 *
 * <p>Architectural Pattern: - Employs the Data Access Object (DAO) pattern to abstract and
 * encapsulate all access to the data source.
 */
public class MessageDAO {

  /**
   * Persists a new Message into the database.
   *
   * <p>Implements specification: - System Doc Section 3.1: Create New Message.
   *
   * @param message The message object containing postedBy, messageText, and timePostedEpoch.
   * @return The newly created Message object, complete with the auto-generated messageId, or null
   *     if insertion fails.
   */
  public Message createMessage(Message message) {
    Connection connection = ConnectionUtil.getConnection();
    // TODOs: As per todo.md, try-with-resources is not used to prevent
    // early closure of the pooled connections provided by ConnectionUtil.
    try {
      String sql =
          "INSERT INTO message (posted_by, message_text, time_posted_epoch) VALUES (?, ?, ?)";
      // Design Pattern: Utilizing PreparedStatement for parameterized queries, improving security
      // against SQL injection.
      PreparedStatement preparedStatement =
          connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
      preparedStatement.setInt(1, message.getPostedBy());
      preparedStatement.setString(2, message.getMessageText());
      preparedStatement.setLong(3, message.getTimePostedEpoch());

      int affectedRows = preparedStatement.executeUpdate();
      if (affectedRows > 0) {
        ResultSet pkeyResultSet = preparedStatement.getGeneratedKeys();
        if (pkeyResultSet.next()) {
          int generatedMessageId = (int) pkeyResultSet.getLong(1);
          return new Message(
              generatedMessageId,
              message.getPostedBy(),
              message.getMessageText(),
              message.getTimePostedEpoch());
        }
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Retrieves all messages currently stored in the database.
   *
   * <p>Implements specification: - System Doc Section 3.2: Retrieve All Messages.
   *
   * @return A List of all Message objects. An empty list is returned if no messages exist.
   */
  public List<Message> getAllMessages() {
    Connection connection = ConnectionUtil.getConnection();
    List<Message> messages = new ArrayList<>();
    try {
      String sql = "SELECT * FROM message";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        Message message =
            new Message(
                rs.getInt("message_id"),
                rs.getInt("posted_by"),
                rs.getString("message_text"),
                rs.getLong("time_posted_epoch"));
        messages.add(message);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return messages;
  }

  /**
   * Retrieves a specific message from the database by its ID.
   *
   * <p>Implements specification: - System Doc Section 3.3: Retrieve Message by ID.
   *
   * @param messageId The unique identifier of the message to retrieve.
   * @return The Message object corresponding to the ID, or null if no such message is found.
   */
  public Message getMessageById(int messageId) {
    Connection connection = ConnectionUtil.getConnection();
    try {
      String sql = "SELECT * FROM message WHERE message_id = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, messageId);

      ResultSet rs = preparedStatement.executeQuery();
      if (rs.next()) {
        return new Message(
            rs.getInt("message_id"),
            rs.getInt("posted_by"),
            rs.getString("message_text"),
            rs.getLong("time_posted_epoch"));
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return null;
  }

  /**
   * Deletes a message from the database by its ID.
   *
   * <p>Implements specification: - System Doc Section 3.4: Delete Message by ID.
   *
   * @param messageId The unique identifier of the message to be deleted.
   * @return true if one or more rows were affected (message deleted), false otherwise (idempotent
   *     operation).
   */
  public boolean deleteMessageById(int messageId) {
    Connection connection = ConnectionUtil.getConnection();
    try {
      String sql = "DELETE FROM message WHERE message_id = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, messageId);

      int affectedRows = preparedStatement.executeUpdate();
      return affectedRows > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Updates the text content of an existing message in the database.
   *
   * <p>Implements specification: - System Doc Section 3.5: Update Message by ID.
   *
   * @param messageId The unique identifier of the message to update.
   * @param messageText The new text content for the message.
   * @return true if the update was successful, false if the messageId does not exist.
   */
  public boolean updateMessageText(int messageId, String messageText) {
    Connection connection = ConnectionUtil.getConnection();
    try {
      String sql = "UPDATE message SET message_text = ? WHERE message_id = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setString(1, messageText);
      preparedStatement.setInt(2, messageId);

      int affectedRows = preparedStatement.executeUpdate();
      return affectedRows > 0;
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return false;
  }

  /**
   * Retrieves all messages posted by a specific user account.
   *
   * <p>Implements specification: - System Doc Section 3.6: Retrieve Messages by Account ID. -
   * Readme Requirement 8: Retrieve all messages written by a particular user.
   *
   * @param accountId The unique identifier of the account (postedBy).
   * @return A List of Message objects posted by the given account. An empty list if none exist.
   */
  public List<Message> getAllMessagesByAccountId(int accountId) {
    Connection connection = ConnectionUtil.getConnection();
    List<Message> messages = new ArrayList<>();
    try {
      String sql = "SELECT * FROM message WHERE posted_by = ?";
      PreparedStatement preparedStatement = connection.prepareStatement(sql);
      preparedStatement.setInt(1, accountId);
      ResultSet rs = preparedStatement.executeQuery();
      while (rs.next()) {
        Message message =
            new Message(
                rs.getInt("message_id"),
                rs.getInt("posted_by"),
                rs.getString("message_text"),
                rs.getLong("time_posted_epoch"));
        messages.add(message);
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
    return messages;
  }
}
