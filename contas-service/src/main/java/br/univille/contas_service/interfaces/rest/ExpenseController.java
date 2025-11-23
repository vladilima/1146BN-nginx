package br.univille.contas_service.interfaces.rest;

import br.univille.contas_service.domain.Expense;
import br.univille.contas_service.infrastructure.persistence.SpringDataExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    @Autowired
    private SpringDataExpenseRepository expenseRepository;

    @GetMapping
    public List<Expense> list() {
        return expenseRepository.findAll();
    }

    @PostMapping
    public Expense create(@RequestBody Expense expense) {
        return expenseRepository.save(expense);
    }
}