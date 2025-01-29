package com.example.qrscanner.database

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "receipts",
    indices = [Index(value = ["iic"], unique = true)])
data class ReceiptEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val linkString: String,
    val scanDateTime: Long,
    //
    val status: Int, // 0: нет ответа, 10: ошибка, 20: принято, 70: обработано
    //
    val accountId: Int, // Ссылка на аккаунт
    val price: Float,
    val purchaseTime: Long,
    val iic: String, // Обозначение уникального идентификатора счёта
    val tin: String, // Обозначение продавца
    val comment: String?,
    val items: String? // JSON-строка позиций покупок
)