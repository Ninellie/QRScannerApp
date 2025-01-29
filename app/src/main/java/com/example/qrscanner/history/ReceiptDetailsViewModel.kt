package com.example.qrscanner.history

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.qrscanner.database.AppDatabase
import com.example.qrscanner.database.ReceiptEntity
import kotlinx.coroutines.launch

class ReceiptDetailsViewModel(private val database: AppDatabase) : ViewModel() {

    private val _receipt = MutableLiveData<ReceiptEntity?>()
    val receipt: LiveData<ReceiptEntity?> get() = _receipt

    fun setReceipt(receiptId: Int) {
        viewModelScope.launch {
            val receiptFromDb = database.appDao().getReceiptById(receiptId)
            _receipt.postValue(receiptFromDb)
        }
    }

    fun updateComment(receiptId: Int, newComment: String) {
        viewModelScope.launch {
            val receipt = database.appDao().getReceiptById(receiptId)
            if (receipt != null) {
                val updatedReceipt = receipt.copy(comment = newComment)
                database.appDao().updateReceipt(updatedReceipt)
            }
        }
    }

    fun deleteReceipt() {
        viewModelScope.launch {
            try{
                _receipt.value?.let { database.appDao().deleteReceiptById(it.id)
                    Log.d("AppDao", "Deleting receipt with id: $it.id")
                }
            } catch (e: Exception){
                    Log.e("Database", "Ошибка при удалении чека с ID" +
                            " ${_receipt.value?.id}: ${e.message}")
                }
        }

    }
}
