package com.moderation.sentinel.service.user;
import com.moderation.sentinel.repository.UserRepository;
import com.moderation.sentinel.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

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
}
