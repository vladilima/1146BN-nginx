package br.univille.contas_service.application.ports;

import br.univille.contas_service.domain.Expense;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository {
    Expense save(Expense expense);
    List<Expense> findAllByFamilyGroupId(Long familyGroupId);
    Optional<Expense> findById(Long id);
}