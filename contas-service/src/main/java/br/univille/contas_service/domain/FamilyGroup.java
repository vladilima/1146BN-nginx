package br.univille.contas_service.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter @Setter
public class FamilyGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @OneToMany(mappedBy = "familyGroup", cascade = CascadeType.ALL)
    private List<AccountUser> members = new ArrayList<>();

    @OneToMany(mappedBy = "familyGroup", cascade = CascadeType.ALL)
    private List<Expense> expenses = new ArrayList<>();
}