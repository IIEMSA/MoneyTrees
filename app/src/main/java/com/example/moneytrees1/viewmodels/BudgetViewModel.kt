package com.example.moneytrees1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.moneytrees1.data.Budget
import com.example.moneytrees1.data.BudgetRepository
import com.example.moneytrees1.data.ExpenseRepository
import com.example.moneytrees1.data.ExpenseEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    // Budget state flows
    private val _currentBudget = MutableStateFlow<Budget?>(null)
    val currentBudget: StateFlow<Budget?> = _currentBudget.asStateFlow()

    private val _totalSpent = MutableStateFlow(0.0)
    val totalSpent: StateFlow<Double> = _totalSpent.asStateFlow()

    private val _progressPercentage = MutableStateFlow(0)
    val progressPercentage: StateFlow<Int> = _progressPercentage.asStateFlow()

    private val _categoryBreakdown = MutableStateFlow<Map<String, Double>>(emptyMap())
    val categoryBreakdown: StateFlow<Map<String, Double>> = _categoryBreakdown.asStateFlow()

    // Expense tracking
    val totalExpenses = expenseRepository.getTotalExpenses()
        .stateIn(viewModelScope, SharingStarted.Lazily, 0.0)
        .asLiveData()

    // Filtered expenses state
    private val _filteredExpenses = MutableStateFlow<List<ExpenseEntity>>(emptyList())
    val filteredExpenses: StateFlow<List<ExpenseEntity>> = _filteredExpenses.asStateFlow()

    init {
        loadLatestBudget()
    }

    private fun loadLatestBudget() {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.getLatestBudgetFlow().collect { budget ->
                _currentBudget.value = budget
                budget?.let { calculateTotals(it) }
            }
        }
    }

    fun saveBudget(budget: Budget) {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.insertBudget(budget)
            _currentBudget.value = budget
            calculateTotals(budget)
        }
    }

    fun loadExpensesBetweenDates(start: String, end: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _filteredExpenses.value = expenseRepository.getExpensesBetweenDates(start, end)
        }
    }

    private fun calculateTotals(budget: Budget) {
        val spent = budget.groceriesAmount + budget.transportAmount + budget.entertainmentAmount
        _totalSpent.value = spent

        val percentage = if (budget.budgetAmount > 0) {
            ((spent / budget.budgetAmount) * 100).coerceAtMost(100.0).toInt()
        } else 0
        _progressPercentage.value = percentage

        _categoryBreakdown.value = mapOf(
            "Groceries" to budget.groceriesAmount,
            "Transport" to budget.transportAmount,
            "Entertainment" to budget.entertainmentAmount
        )
    }
}