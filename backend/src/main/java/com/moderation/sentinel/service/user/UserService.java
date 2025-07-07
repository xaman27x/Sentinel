package com.moderation.sentinel.service.user;
import com.moderation.sentinel.repository.UserRepository;
import com.moderation.sentinel.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User createUser(User user) {
        if(userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("User already exists");
        }

        return userRepository.save(user);
    };

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getUserByEmail(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.orElse(null);
    }
    
    public User updateUser(User user) {
        if (!userRepository.existsById(user.getUserId())) {
            throw new RuntimeException("User not found");
        }
        return userRepository.save(user);
    }
    
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
