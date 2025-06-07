package com.example.moneytrees1.data

class NotificationRepository(private val dao: NotificationDao) {
    suspend fun insert(notification: NotificationEntity) = dao.insert(notification)
    fun getAllNotificationsForUser(userId: Int) = dao.getAllNotificationsForUser(userId)
    suspend fun clearAll(userId: Int) = dao.clearNotificationsForUser(userId)
}
