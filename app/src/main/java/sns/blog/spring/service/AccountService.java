package sns.blog.spring.service;

import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sns.blog.spring.entity.Account;
import sns.blog.spring.exception.ClientValidationException;
import sns.blog.spring.exception.DuplicateResourceException;
import sns.blog.spring.exception.UnauthorizedException;
import sns.blog.spring.repository.AccountRepository;

/**
 * Service class for the Account entity. Responsible for handling all business logic, validation,
 * and passing valid data to the Repository Layer.
 *
 * <p>Architectural Pattern: - Service Layer (3-Layer Architecture) pattern separating
 * business/validation rules from routing and data access.
 */
@Service
public class AccountService {

  // Design Pattern: Dependency Injection. Repository is injected, allowing decoupling.
  private final AccountRepository accountRepository;

  /**
   * Constructor for AccountService.
   *
   * @param accountRepository An injected AccountRepository interface.
   */
  @Autowired
  public AccountService(AccountRepository accountRepository) {
    this.accountRepository = accountRepository;
  }

  /**
   * Registers a new user account if validation criteria are met.
   *
   * <p>Implements specification: - System Doc Section 2.1: User Registration.
   *
   * <p>Validation rules: 1. The username must not be null or blank. 2. The password must not be
   * null and must be at least 4 characters long. 3. The username must not already exist in the
   * database.
   *
   * @param account The Account object representing the requested new user.
   * @return The registered Account object containing the generated accountId.
   * @throws ClientValidationException if the username is blank or the password is under 4
   *     characters.
   * @throws DuplicateResourceException if the username already exists.
   */
  public Account registerAccount(Account account) {
    // Implementation detail: Business validation checks for blank username and min-length password.
    if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
      throw new ClientValidationException("Username cannot be blank");
    }
    if (account.getPassword() == null || account.getPassword().length() < 4) {
      throw new ClientValidationException("Password must be at least 4 characters");
    }
    // Implementation detail: Check for existing username to ensure uniqueness.
    if (accountRepository.findByUsername(account.getUsername()).isPresent()) {
      throw new DuplicateResourceException("Username already exists");
    }
    return accountRepository.save(account);
  }

  /**
   * Authenticates a user login attempt.
   *
   * <p>Implements specification: - System Doc Section 2.2: User Login.
   *
   * @param account The Account object containing the login credentials (username and password).
   * @return The authenticated Account object with its accountId if successful.
   * @throws UnauthorizedException if login fails.
   */
  public Account login(Account account) {
    Optional<Account> optionalAccount = accountRepository.findByUsername(account.getUsername());
    if (optionalAccount.isPresent()
        && optionalAccount.get().getPassword().equals(account.getPassword())) {
      return optionalAccount.get();
    }
    throw new UnauthorizedException("Invalid credentials"); // Signals invalid credentials
  }
}
