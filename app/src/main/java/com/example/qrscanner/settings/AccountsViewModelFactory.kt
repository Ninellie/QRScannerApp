package com.example.qrscanner.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qrscanner.database.AppDatabase

class AccountsViewModelFactory(
    private val database: AppDatabase,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AccountsViewModel(database, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
