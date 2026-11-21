package com.financetracker.fintrack.service;

import com.financetracker.fintrack.model.User;
import com.financetracker.fintrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    private UserRepository repo;
    private UserService service;

    @BeforeEach
    void setUp() {
        repo = mock(UserRepository.class);
        when(repo.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));
        service = new UserService(repo);
    }

    private User user(String username, String password) {
        User u = new User();
        u.setUsername(username);
        u.setPassword(password);
        return u;
    }

    @Test
    void registerStoresHashNotPlaintext() {
        User saved = service.register(user("alice", "s3cret"));

        assertNotEquals("s3cret", saved.getPassword());
        assertTrue(saved.getPassword().startsWith("$2"));
    }

    @Test
    void loginSucceedsWithCorrectPasswordAndFailsWithWrongOne() {
        User saved = service.register(user("alice", "s3cret"));
        when(repo.findByUsername("alice")).thenReturn(Optional.of(saved));

        assertTrue(service.login("alice", "s3cret").isPresent());
        assertTrue(service.login("alice", "wrong").isEmpty());
    }

    @Test
    void loginFailsForUnknownUser() {
        when(repo.findByUsername("bob")).thenReturn(Optional.empty());

        assertTrue(service.login("bob", "anything").isEmpty());
    }

    @Test
    void legacyPlaintextPasswordIsUpgradedToHashOnLogin() {
        User legacy = user("carol", "oldpass");
        when(repo.findByUsername("carol")).thenReturn(Optional.of(legacy));

        assertTrue(service.login("carol", "oldpass").isPresent());
        assertTrue(legacy.getPassword().startsWith("$2"));
        verify(repo).save(legacy);

        // The upgraded hash still works, and the plaintext no longer matches as a string.
        assertTrue(service.login("carol", "oldpass").isPresent());
        assertTrue(service.login("carol", legacy.getPassword()).isEmpty());
    }

    @Test
    void legacyPlaintextWithWrongPasswordIsRejectedAndNotUpgraded() {
        User legacy = user("dave", "oldpass");
        when(repo.findByUsername("dave")).thenReturn(Optional.of(legacy));

        assertTrue(service.login("dave", "wrong").isEmpty());
        assertEquals("oldpass", legacy.getPassword());
        verify(repo, never()).save(any());
    }
}
