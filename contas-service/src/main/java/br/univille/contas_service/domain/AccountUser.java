package br.univille.contas_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.UUID;

@Entity
@Getter @Setter
public class AccountUser {
    @Id
    private UUID id; // Mesmo ID do Auth Service

    private String name;
    private String email;
    private Double income; // Renda (específico deste domínio)

    @ManyToOne
    private FamilyGroup familyGroup;
}