package br.univille.contas_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String description;
    private Double value;
    private LocalDate dueDate;

    @ManyToOne
    private FamilyGroup familyGroup;

    @ManyToMany
    private List<AccountUser> responsibleUsers = new ArrayList<>();
}