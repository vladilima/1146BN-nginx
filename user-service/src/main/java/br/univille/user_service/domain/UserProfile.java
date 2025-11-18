package br.univille.user_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_profile")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {

    @Id
    @Column(nullable = false, updatable = false)
    private UUID userId; // Usa o ID do usuário do auth-service

    @Column(nullable = true)
    private String profilePictureUrl;

    public UserProfile(UUID userId) {
        this.userId = userId;
    }

    public void updateProfilePicture(String url) {
        this.profilePictureUrl = url;
    }
}