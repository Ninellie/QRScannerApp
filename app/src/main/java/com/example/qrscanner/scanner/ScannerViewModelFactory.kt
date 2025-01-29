package com.example.qrscanner.scanner

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.qrscanner.database.AppDatabase

class ScannerViewModelFactory(
    private val database: AppDatabase,
    private val context: Context
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ScannerViewModel(database, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}