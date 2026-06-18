package sns.blog.spring.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sns.blog.spring.entity.Message;

/**
 * Data Access Object (Repository) interface for the Message entity. Extends JpaRepository to
 * provide standard database operations automatically.
 *
 * <p>Architectural Pattern: - Employs the Repository Pattern via Spring Data JPA to abstract and
 * encapsulate data access.
 */
@Repository
public interface MessageRepository extends JpaRepository<Message, Integer> {

  /**
   * Retrieves all messages posted by a specific user account.
   *
   * <p>Implements specification: - System Doc Section 3.6: Retrieve Messages by Account ID.
   *
   * @param postedBy The unique identifier of the account (accountId).
   * @return A List of Message objects posted by the given account. An empty list if none exist.
   */
  List<Message> findByPostedBy(Integer postedBy);
}
