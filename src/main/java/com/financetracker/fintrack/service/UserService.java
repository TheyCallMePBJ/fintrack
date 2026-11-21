package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.User;
import com.financetracker.fintrack.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    /**
     * Saves a new user, replacing the plaintext password with a BCrypt hash.
     */
    public User register(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        return repo.save(user);
    }

    public Optional<User> login(String username, String password) {
        Optional<User> found = repo.findByUsername(username);
        if (found.isEmpty()) {
            return Optional.empty();
        }

        User user = found.get();
        String stored = user.getPassword();
        if (stored == null) {
            return Optional.empty();
        }

        if (isBcryptHash(stored)) {
            return encoder.matches(password, stored) ? found : Optional.empty();
        }

        // Account created before passwords were hashed: check the old plaintext
        // value once, then replace it with a hash so it is never stored in plaintext again.
        if (stored.equals(password)) {
            user.setPassword(encoder.encode(password));
            repo.save(user);
            return found;
        }
        return Optional.empty();
    }

    private static boolean isBcryptHash(String value) {
        return value.startsWith("$2a$") || value.startsWith("$2b$") || value.startsWith("$2y$");
    }
}
