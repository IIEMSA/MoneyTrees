
package com.example.moneytrees1.viewmodels
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.moneytrees1.data.NotificationEntity
import com.example.moneytrees1.data.NotificationRepository
import kotlinx.coroutines.launch
class NotificationViewModel(private val repo: NotificationRepository) : ViewModel() {
    fun insert(notification: NotificationEntity) = viewModelScope.launch {
        repo.insert(notification)
    }     fun getNotifications(userId: Int) = repo.getAllNotificationsForUser(userId)
}
