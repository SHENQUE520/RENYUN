package org.tenacitycodex.renyun.common.dto;

import lombok.*;
import org.tenacitycodex.renyun.module.user.entity.User;

import java.time.LocalDateTime;

@Data
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String email;
    private String name;
    private LocalDateTime createdAt = LocalDateTime.now();

    public static UserDTO fromUser(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getUsername())
                .createdAt(user.getCreatedAt())
                .build();
    }

    public User toUser() {
        User user = new User();
        user.setId(this.id);
        user.setEmail(this.email);
        user.setUsername(this.name);
        return user;
    }
}
