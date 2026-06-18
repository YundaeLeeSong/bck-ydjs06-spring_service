package sns.blog.javalin.service;

import sns.blog.javalin.dao.AccountDAO;
import sns.blog.javalin.model.Account;

/**
 * Service class for the Account entity. Responsible for handling all business logic, validation,
 * and passing valid data to the Data Access Layer.
 *
 * <p>Architectural Pattern: - Service Layer (3-Layer Architecture) pattern separating
 * business/validation rules from routing and data access.
 */
public class AccountService {
  // Design Pattern: Dependency Injection. DAO is injected or instantiated, allowing decoupling.
  private AccountDAO accountDAO;

  /** Default constructor for AccountService. Instantiates a new AccountDAO. */
  public AccountService() {
    this.accountDAO = new AccountDAO();
  }

  /**
   * Parameterized constructor for AccountService.
   *
   * @param accountDAO An existing AccountDAO object to be injected.
   */
  public AccountService(AccountDAO accountDAO) {
    this.accountDAO = accountDAO;
  }

  /**
   * Registers a new user account if validation criteria are met.
   *
   * <p>Implements specification:
   *
   * <ul>
   *   <li>System Doc Section 2.1: User Registration.
   * </ul>
   *
   * <p>Validation rules:
   *
   * <ol>
   *   <li>The username must not be null or blank.
   *   <li>The password must not be null and must be at least 4 characters long.
   *   <li>The username must not already exist in the database.
   * </ol>
   *
   * @param account The Account object representing the requested new user.
   * @return The registered Account object containing the generated accountId, or null if validation
   *     fails.
   */
  public Account registerAccount(Account account) {
    // Implementation detail: Business validation checks for blank username and min-length password.
    if (account.getUsername() == null
        || account.getUsername().trim().isEmpty()
        || account.getPassword() == null
        || account.getPassword().length() < 4) {
      return null;
    }

    // Implementation detail: Check for existing username to ensure uniqueness.
    Account existingAccount = accountDAO.getAccountByUsername(account.getUsername());
    if (existingAccount != null) {
      return null; // Username already exists
    }

    return accountDAO.createAccount(account);
  }

  /**
   * Authenticates a user login attempt.
   *
   * <p>Implements specification: - System Doc Section 2.2: User Login.
   *
   * @param account The Account object containing the login credentials (username and password).
   * @return The authenticated Account object with its accountId if successful, or null if login
   *     fails.
   */
  public Account login(Account account) {
    return accountDAO.getAccountByUsernameAndPassword(account.getUsername(), account.getPassword());
  }

  /**
   * Retrieves a user account by its ID. This is generally used as a helper method for validating
   * message creation.
   *
   * <p>Implements specification: - System Doc Section 3.1 (Helper validation).
   *
   * @param accountId The ID of the account to retrieve.
   * @return The corresponding Account object, or null if it does not exist.
   */
  public Account getAccountById(int accountId) {
    return accountDAO.getAccountById(accountId);
  }
}
