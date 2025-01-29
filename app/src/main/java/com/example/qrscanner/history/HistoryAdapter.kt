package com.example.qrscanner.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.R
import com.example.qrscanner.database.ReceiptEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HistoryAdapter(
    private val onReceiptClick: (ReceiptEntity) -> Unit
) : ListAdapter<ReceiptEntity, HistoryAdapter.HistoryViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_receipt, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position), onReceiptClick)
    }

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val purchaseTime: TextView = view.findViewById(R.id.purchaseTimeTextView)
        private val price: TextView = view.findViewById(R.id.priceTextView)
        private val status: TextView = view.findViewById(R.id.statusTextView)

        fun bind(receipt: ReceiptEntity, onReceiptClick: (ReceiptEntity) -> Unit) {
            // Форматируем дату из миллисекунд в понятный формат
            val formattedDate = SimpleDateFormat("dd.MM.yy HH:mm:ss",
                    Locale.getDefault()).format(Date(receipt.purchaseTime))

            purchaseTime.text = formattedDate
            price.text = receipt.price.toString()
            status.text = receipt.status.toString()

            itemView.setOnClickListener {
                onReceiptClick(receipt)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ReceiptEntity>() {
        override fun areItemsTheSame(oldItem: ReceiptEntity, newItem: ReceiptEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ReceiptEntity, newItem: ReceiptEntity): Boolean {
            return oldItem == newItem
        }
    }
}
