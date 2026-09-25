package org.tenacitycodex.renyun.common.config.security;

import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.tenacitycodex.renyun.common.exceptions.UserNotFoundException;

@Slf4j
@Component
public class SecurityUtil {
    public static @NotNull AuthenticatedUser getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser)) {
            throw new UserNotFoundException("找不到该user_id");
        }
        return (AuthenticatedUser) authentication.getPrincipal();
    }

    public static Long getCurrentUserId() {
        AuthenticatedUser user = getCurrentAuthenticatedUser();
        return user.getUserId();
    }


    public static UserDetails getCurrentUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetails)) {
            log.warn("No authenticated user found");
            return null;
        }
        return (UserDetails) authentication.getPrincipal();
    }

    public static String getCurrentUsername() {
        UserDetails userDetails = getCurrentUserDetails();
        return userDetails != null ? userDetails.getUsername() : null;
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Auth Object: {}", authentication);
        return authentication != null && authentication.isAuthenticated() &&
                !(authentication.getPrincipal() instanceof String) &&
                getCurrentUserId() != null;
    }

    public static void requireAuthentication() {
        if (!isAuthenticated()) {
            throw new SecurityException("User not authenticated");
        }
    }
}
