package com.example.moneytrees1.data

class CategoryRepository(private val categoryDao: CategoryDao) {

    // Insert a new category
    suspend fun insert(category: CategoryEntity) {
        categoryDao.insert(category)
    }

    // Get all categories
    suspend fun getAllCategories(): List<CategoryEntity> {
        return categoryDao.getAllCategories()
    }
}
