package br.univille.user_service.interfaces.rest;

import br.univille.user_service.application.UserProfileRepository;
import br.univille.user_service.domain.UserProfile;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/users/profile")
@RequiredArgsConstructor
public class UserProfileController {
    private final UserProfileRepository profileRepository;

    public record ProfileUpdatePictureRequest(String profilePictureUrl) {}
    
    // Helper para obter o ID do usuário autenticado (assumindo injeção do Gateway)
    private UUID getAuthenticatedUserId(String userIdHeader) {
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user ID in header.");
        }
    }

    // Endpoint para trocar a foto de perfil do usuário autenticado
    @PutMapping
    public ResponseEntity<UserProfile> updateProfilePicture(
            @RequestHeader("X-User-ID") String userIdHeader,
            @RequestBody ProfileUpdatePictureRequest request) {

        UUID authenticatedUserId = getAuthenticatedUserId(userIdHeader);

        UserProfile profile = profileRepository.findById(authenticatedUserId)
                .orElseGet(() -> new UserProfile(authenticatedUserId));

        profile.updateProfilePicture(request.profilePictureUrl());
        UserProfile saved = profileRepository.save(profile);

        return ResponseEntity.ok(saved);
    }

    // Endpoint para obter o perfil de um usuário (necessário para "iniciar uma conversa com outra pessoa")
    @GetMapping("/{userId}")
    public ResponseEntity<UserProfile> getProfile(@PathVariable UUID userId) {
        return profileRepository.findById(userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}