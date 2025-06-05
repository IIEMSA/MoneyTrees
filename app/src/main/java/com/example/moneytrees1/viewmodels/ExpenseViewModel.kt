package com.example.moneytrees1.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrees1.data.ExpenseEntity
import com.example.moneytrees1.data.ExpenseRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository,
    private val userId: Int
) : ViewModel() {
    private val _recentExpenses = MutableLiveData<List<ExpenseEntity>>()
    val recentExpenses: LiveData<List<ExpenseEntity>> = _recentExpenses

    private val _totalExpenses = MutableLiveData<Double>()
    val totalExpenses: LiveData<Double> = _totalExpenses

    private val _allExpenses = MutableLiveData<List<ExpenseEntity>>()
    val allExpenses: LiveData<List<ExpenseEntity>> = _allExpenses

    init {
        loadRecentExpenses()
        loadTotalExpenses()
        loadAllExpenses()
    }

    private fun loadRecentExpenses() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getRecentExpenses(userId).collect { expenses ->
                _recentExpenses.postValue(expenses)
            }
        }
    }

    private fun loadTotalExpenses() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getTotalExpenses(userId).collect { total ->
                _totalExpenses.postValue(total)
            }
        }
    }

    private fun loadAllExpenses() {
        viewModelScope.launch(Dispatchers.IO) {
            repository.getAllExpensesFlow(userId).collect { expenses ->
                _allExpenses.postValue(expenses)
            }
        }
    }

    fun insertExpense(expense: ExpenseEntity) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertExpense(expense.copy(userId = userId))
            // Refresh data
            loadRecentExpenses()
            loadTotalExpenses()
            loadAllExpenses()
        }
    }

    suspend fun getExpensesBetweenDates(start: String, end: String): List<ExpenseEntity> {
        return repository.getExpensesBetweenDates(userId, start, end)
    }
}