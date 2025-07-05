package com.moderation.sentinel.service.auth;

import com.moderation.sentinel.model.User;
import com.moderation.sentinel.repository.UserRepository;
import com.moderation.sentinel.security.JwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public User registerUser(String email, String username, String firstName, String lastName, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("User with email already exists");
        }


        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole("USER");
        user.setSubscriptionTier("FREE");
        user.setIsActive(true);
        
        return userRepository.save(user);
    }
    
    public String authenticateUser(String email, String password) {
        Optional<User> userOptional = userRepository.findByEmail(email);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if (!user.getIsActive()) {
                throw new RuntimeException("User account is deactivated");
            }
            
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                return jwtTokenProvider.generateToken(user.getEmail(), user.getUserId(), user.getRole());
            } else {
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            throw new RuntimeException("User not found");
        }
    }
    
    public User getUserFromToken(String token) {
        if (jwtTokenProvider.validateToken(token)) {
            String email = jwtTokenProvider.getEmailFromToken(token);
            return userRepository.findByEmail(email).orElse(null);
        }
        return null;
    }
    
    public boolean validateToken(String token) {
        return jwtTokenProvider.validateToken(token);
    }
    
    public User getCurrentUser(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    public User updateUserProfile(Long userId, String firstName, String lastName) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setFirstName(firstName);
            user.setLastName(lastName);
            return userRepository.save(user);
        }
        
        throw new RuntimeException("User not found");
    }
    
    public boolean changePassword(Long userId, String currentPassword, String newPassword) {
        Optional<User> userOptional = userRepository.findById(userId);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            
            if (passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            } else {
                throw new RuntimeException("Current password is incorrect");
            }
        }
        
        throw new RuntimeException("User not found");
    }
}