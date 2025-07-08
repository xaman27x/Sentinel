package com.moderation.sentinel.api.controller;

import com.moderation.sentinel.api.dto.request.ChangePasswordRequest;
import com.moderation.sentinel.api.dto.request.UpdateProfileRequest;
import com.moderation.sentinel.api.dto.response.ApiResponse;
import com.moderation.sentinel.api.dto.response.UserProfileResponse;
import com.moderation.sentinel.model.User;
import com.moderation.sentinel.security.JwtTokenProvider;
import com.moderation.sentinel.service.user.UserService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserProfileController {

    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

    @Autowired private UserService userService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private JwtTokenProvider jwtTokenProvider;


    @GetMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getCurrentUserProfile(
            @RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.replace("Bearer ", "");
            String email = jwtTokenProvider.getEmailFromToken(token);

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("User not found", "USER_NOT_FOUND")
                );
            }

            UserProfileResponse profile = new UserProfileResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getSubscriptionTier(),
                    user.getIsActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success("Profile retrieved successfully", profile));

        } catch (Exception e) {
            logger.error("Error retrieving user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error retrieving user profile: " + e.getMessage(), "INTERNAL_ERROR")
            );
        }
    }


    @PutMapping("/profile")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateUserProfile(
            @Valid @RequestBody UpdateProfileRequest req,
            @RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.replace("Bearer ", "");
            String email = jwtTokenProvider.getEmailFromToken(token);

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("User not found", "USER_NOT_FOUND")
                );
            }

            if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
            if (req.getLastName() != null)  user.setLastName(req.getLastName());
            if (req.getUsername() != null)  user.setUsername(req.getUsername());

            user = userService.updateUser(user);

            UserProfileResponse profile = new UserProfileResponse(
                    user.getUserId(),
                    user.getEmail(),
                    user.getUsername(),
                    user.getFirstName(),
                    user.getLastName(),
                    user.getRole(),
                    user.getSubscriptionTier(),
                    user.getIsActive(),
                    user.getCreatedAt(),
                    user.getUpdatedAt()
            );

            return ResponseEntity.ok(ApiResponse.success("Profile updated successfully", profile));

        } catch (Exception e) {
            logger.error("Error updating user profile", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error updating user profile: " + e.getMessage(), "INTERNAL_ERROR")
            );
        }
    }


    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest req,
            @RequestHeader("Authorization") String bearerToken) {
        try {
            String token = bearerToken.replace("Bearer ", "");
            String email = jwtTokenProvider.getEmailFromToken(token);

            User user = userService.getUserByEmail(email);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                        ApiResponse.error("User not found", "USER_NOT_FOUND")
                );
            }

            if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                        ApiResponse.error("Current password is incorrect", "INVALID_PASSWORD")
                );
            }

            user.setPassword(passwordEncoder.encode(req.getNewPassword()));
            userService.updateUser(user);

            return ResponseEntity.ok(ApiResponse.success("Password changed successfully", null));

        } catch (Exception e) {
            logger.error("Error changing password", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    ApiResponse.error("Error changing password: " + e.getMessage(), "INTERNAL_ERROR")
            );
        }
    }

}
