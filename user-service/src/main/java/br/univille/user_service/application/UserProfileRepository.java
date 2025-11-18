package br.univille.user_service.application;

import br.univille.user_service.domain.UserProfile;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface UserProfileRepository extends CrudRepository<UserProfile, UUID> {}