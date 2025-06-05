package com.example.moneytrees1.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Delete
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: CategoryEntity)

    @Query("SELECT * FROM categories WHERE userId = :userId")
    suspend fun getAllCategories(userId: Int): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE userId = :userId")
    fun getAllCategoriesFlow(userId: Int): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name LIMIT 1")
    suspend fun getCategoryByName(userId: Int, name: String): CategoryEntity?

    @Query("SELECT * FROM categories WHERE userId = :userId AND name = :name")
    suspend fun getCategoriesByName(userId: Int, name: String): List<CategoryEntity>

    @Delete
    suspend fun delete(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)
}

