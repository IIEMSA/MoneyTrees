package com.example.moneytrees1.data

class CategoryRepository(private val categoryDao: CategoryDao) {

    suspend fun insert(category: CategoryEntity) {
        // Now need userId for duplicate check
        if (categoryDao.getCategoryByName(category.userId, category.name) != null) {
            throw Exception("Category already exists")
        }
        categoryDao.insert(category)
    }

    suspend fun getAllCategories(userId: Int) = categoryDao.getAllCategories(userId)

    suspend fun deleteCategory(category: CategoryEntity) = categoryDao.delete(category)

    suspend fun getCategoriesByName(userId: Int, name: String) = categoryDao.getCategoriesByName(userId, name)

    suspend fun updateCategory(category: CategoryEntity) = categoryDao.updateCategory(category)

    fun getAllCategoriesFlow(userId: Int) = categoryDao.getAllCategoriesFlow(userId)
}