package com.example.moneytrees1.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)

    suspend fun insert(notification: NotificationEntity)

    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY timestamp DESC")

    fun getAllNotificationsForUser(userId: Int): LiveData<List<NotificationEntity>>

    @Query("DELETE FROM notifications WHERE userId = :userId")

    suspend fun clearNotificationsForUser(userId: Int)
}
