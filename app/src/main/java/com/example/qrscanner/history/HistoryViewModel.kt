package com.example.qrscanner.history

import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrscanner.database.AppDatabase
import com.example.qrscanner.database.ReceiptEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(private val database: AppDatabase, context: Context) : ViewModel() {

    private val _receipts = MutableStateFlow<List<ReceiptEntity>>(emptyList())
    val receipts: StateFlow<List<ReceiptEntity>> get() = _receipts

    private val preferences: SharedPreferences = context.getSharedPreferences(
        "AppPreferences", Context.MODE_PRIVATE)

    init {
        loadReceipts()
    }

    fun loadReceipts() {
        viewModelScope.launch {
            // Загружаем чеки из базы данных, сортируя их по времени покупки
            val lastSelectedAccountId = preferences.getInt("lastSelectedAccountId", -1)

            val receipts = database.appDao().getReceiptsByAccount(lastSelectedAccountId)

            _receipts.value = receipts
        }
    }
}
