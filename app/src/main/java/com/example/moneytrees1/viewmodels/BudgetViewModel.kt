package com.example.moneytrees1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrees1.data.Budget
import com.example.moneytrees1.data.BudgetRepository
import com.example.moneytrees1.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val budgetRepository: BudgetRepository,
    private val expenseRepository: ExpenseRepository,
    private val userId: Int
) : ViewModel() {
    private val _currentBudget = MutableLiveData<Budget?>()
    val currentBudget: LiveData<Budget?> = _currentBudget

    private val _progressPercentage = MutableLiveData<Int>()
    val progressPercentage: LiveData<Int> = _progressPercentage

    private val _totalSpent = MutableLiveData<Double>()
    val totalSpent: LiveData<Double> = _totalSpent

    init {
        loadLatestBudget()
        loadTotalSpent()
    }

    private fun loadLatestBudget() {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.getLatestBudgetFlow(userId).collect { budget ->
                _currentBudget.postValue(budget)
                updateProgress()
            }
        }
    }

    private fun loadTotalSpent() {
        viewModelScope.launch(Dispatchers.IO) {
            expenseRepository.getTotalExpenses(userId).collect { total ->
                _totalSpent.postValue(total)
                updateProgress()
            }
        }
    }

    private fun updateProgress() {
        val budget = _currentBudget.value
        val spent = _totalSpent.value ?: 0.0

        if (budget != null && budget.budgetAmount > 0) {
            val percentage = (spent / budget.budgetAmount * 100).toInt().coerceAtMost(100)
            _progressPercentage.postValue(percentage)
        } else {
            _progressPercentage.postValue(0)
        }
    }

    fun insertBudget(budget: Budget) {
        viewModelScope.launch(Dispatchers.IO) {
            budgetRepository.insertBudget(budget)
            loadLatestBudget()
        }
    }
}