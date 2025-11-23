package br.univille.contas_service.infrastructure.persistence;

import br.univille.contas_service.domain.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SpringDataExpenseRepository extends JpaRepository<Expense, Long> {
    List<Expense> findByFamilyGroupId(Long familyGroupId);
}