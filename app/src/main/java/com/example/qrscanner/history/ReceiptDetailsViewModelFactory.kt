package com.example.qrscanner.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qrscanner.database.AppDatabase

class ReceiptDetailsViewModelFactory(
    private val database: AppDatabase
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReceiptDetailsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ReceiptDetailsViewModel(database) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
