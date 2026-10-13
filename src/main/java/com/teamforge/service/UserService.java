package com.teamforge.service;

import com.teamforge.entity.Account;
import com.teamforge.entity.Role;
import com.teamforge.entity.User;
import com.teamforge.repository.AccountRepository;
import com.teamforge.repository.UserRepository;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class UserService {

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            AccountRepository accountRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // CREATE CANDIDATE AND LOGIN ACCOUNT
    @Transactional
    public User saveUser(User user) {

        if (user == null) {
            throw new IllegalArgumentException(
                    "Candidate information is required."
            );
        }

        if (user.getId() != null) {
            throw new IllegalArgumentException(
                    "Use the update endpoint to edit an existing candidate."
            );
        }

        validateCandidate(user);

        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException(
                    "Password is required for a new candidate."
            );
        }

        if (user.getPassword().length() < 8) {
            throw new IllegalArgumentException(
                    "Password must contain at least 8 characters."
            );
        }

        checkDuplicateEmail(user.getEmail(), null);

        if (accountRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException(
                    "An account with this email already exists."
            );
        }

        // Encrypt password
        String encryptedPassword =
                passwordEncoder.encode(user.getPassword());

        user.setPassword(encryptedPassword);

        // Save candidate profile
        User savedUser = userRepository.save(user);

        // Create linked login account
        Account account = new Account();

        account.setName(savedUser.getName());
        account.setEmail(savedUser.getEmail());
        account.setPassword(encryptedPassword);
        account.setRole(Role.CANDIDATE);
        account.setCandidate(savedUser);

        accountRepository.save(account);

        return savedUser;
    }

    // UPDATE CANDIDATE AND LINKED LOGIN ACCOUNT
    @Transactional
    public User updateUser(Long id, User updatedUser) {

        if (updatedUser == null) {
            throw new IllegalArgumentException(
                    "Candidate information is required."
            );
        }

        User existingUser = userRepository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Candidate not found."
                        )
                );

        validateCandidate(updatedUser);

        checkDuplicateEmail(updatedUser.getEmail(), id);

        Optional<Account> linkedAccount =
                accountRepository.findByCandidate_Id(id);

        // Prevent using an email registered to another account
        Optional<Account> emailAccount =
                accountRepository.findByEmail(updatedUser.getEmail());

        if (emailAccount.isPresent()) {

            boolean belongsToCandidate =
                    linkedAccount.isPresent()
                    && emailAccount.get().getId()
                            .equals(linkedAccount.get().getId());

            if (!belongsToCandidate) {
                throw new IllegalArgumentException(
                        "An account with this email already exists."
                );
            }
        }

        existingUser.setName(updatedUser.getName());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setPreferredRole(updatedUser.getPreferredRole());
        existingUser.setExperienceYears(updatedUser.getExperienceYears());
        existingUser.setAvailable(updatedUser.getAvailable());

        // Update password only when admin supplies a new one
        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {

            if (updatedUser.getPassword().length() < 8) {
                throw new IllegalArgumentException(
                        "New password must contain at least 8 characters."
                );
            }

            String encryptedPassword =
                    passwordEncoder.encode(updatedUser.getPassword());

            existingUser.setPassword(encryptedPassword);
        }

        User savedUser = userRepository.save(existingUser);

        // Synchronize existing linked login account
        if (linkedAccount.isPresent()) {

            Account account = linkedAccount.get();

            account.setName(savedUser.getName());
            account.setEmail(savedUser.getEmail());
            account.setPassword(savedUser.getPassword());

            accountRepository.save(account);
        }

        return savedUser;
    }

    // VALIDATE CANDIDATE DETAILS
    private void validateCandidate(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            throw new IllegalArgumentException(
                    "Candidate name is required."
            );
        }

        String name = user.getName()
                .trim()
                .replaceAll("\\s+", " ");

        if (name.length() < 2 || name.length() > 120) {
            throw new IllegalArgumentException(
                    "Candidate name must contain between 2 and 120 characters."
            );
        }

        user.setName(name);

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException(
                    "Email address is required."
            );
        }

        String email = user.getEmail()
                .trim()
                .toLowerCase();

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException(
                    "Enter a valid email address."
            );
        }

        user.setEmail(email);

        if (user.getPreferredRole() != null) {
            user.setPreferredRole(
                    user.getPreferredRole().trim()
            );
        }

        if (user.getExperienceYears() == null
                || user.getExperienceYears() < 0
                || user.getExperienceYears() > 80) {

            throw new IllegalArgumentException(
                    "Experience must be between 0 and 80 years."
            );
        }

        if (user.getAvailable() == null) {
            throw new IllegalArgumentException(
                    "Candidate availability is required."
            );
        }
    }

    // PREVENT DUPLICATE EMAILS
    private void checkDuplicateEmail(
            String email,
            Long currentUserId
    ) {

        Optional<User> existing =
                userRepository.findByEmail(email);

        if (existing.isPresent()
                && !existing.get().getId().equals(currentUserId)) {

            throw new IllegalArgumentException(
                    "A candidate with this email already exists."
            );
        }
    }

    // GET ALL CANDIDATES
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // GET CANDIDATE BY ID
    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    // GET CANDIDATE BY EMAIL
    public Optional<User> getUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    // DELETE CANDIDATE AND LOGIN ACCOUNT
    @Transactional
    public void deleteUser(Long id) {

        Optional<Account> linkedAccount =
                accountRepository.findByCandidate_Id(id);

        // Delete login account first
        linkedAccount.ifPresent(accountRepository::delete);

        // Delete candidate profile
        userRepository.deleteById(id);
    }
}