package com.example.qrscanner.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.qrscanner.R
import com.example.qrscanner.database.AppDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReceiptDetailsFragment : Fragment(R.layout.fragment_receipt_details) {

    private val viewModel: ReceiptDetailsViewModel by viewModels {
        ReceiptDetailsViewModelFactory(AppDatabase.getInstance(requireContext()))
    }

    companion object {
        fun newInstance(receiptId: Int) = ReceiptDetailsFragment().apply {
            arguments = Bundle().apply {
                putInt("receipt_id", receiptId)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val receiptId = requireArguments().getInt("receipt_id")

        viewModel.setReceipt(receiptId)
        val detailsTextView = view.findViewById<TextView>(R.id.receiptDetailsTextView)
        val linkTextView = view.findViewById<TextView>(R.id.linkTextView)
        val commentEditText = view.findViewById<EditText>(R.id.commentEditText)
        val saveButton = view.findViewById<Button>(R.id.saveButton)
        val deleteButton = view.findViewById<ImageButton>(R.id.deleteForeverButton)

        viewModel.receipt.observe(viewLifecycleOwner) { receipt ->
            linkTextView.text = receipt?.linkString
            linkTextView.setOnClickListener {
                copyToClipboard(receipt?.linkString.toString())
                Toast.makeText(requireContext(), "Ссылка скопирована", Toast.LENGTH_SHORT).show()
            }

            if (receipt == null){
                return@observe
            }

            // Форматируем и отображаем все данные чека
            val details = """
                ID: ${receipt!!.id}
                Цена: ${receipt.price}
                Дата покупки: ${formatDate(receipt.purchaseTime)}
                IIC: ${receipt.iic}
                TIN: ${receipt.tin}
                Статус: ${statusToText(receipt.status)}
                Аккаунт: ${receipt.accountId}
                Дата сканирования: ${formatDate(receipt.scanDateTime)}
                Комментарий: ${receipt.comment ?: "Нет"}
                Позиции: ${receipt.items ?: "Нет"}
            """.trimIndent()

            detailsTextView.text = details

            commentEditText.setText(receipt?.comment)
        }

        deleteButton.setOnClickListener{
            showAccountDeletionDialog()
        }

        saveButton.setOnClickListener {
            val newComment = commentEditText.text.toString()
            viewModel.updateComment(receiptId, newComment)
            Toast.makeText(requireContext(), "Комментарий сохранён", Toast.LENGTH_SHORT).show()
        }
    }

    private fun copyToClipboard(text: String) {
        val clipboard = requireContext()
            .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Receipt Link", text)
        clipboard.setPrimaryClip(clip)
    }
    private fun formatDate(timestamp: Long): String {
        val formatter = SimpleDateFormat("dd.MM.yyyy, HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    private fun statusToText(status: Int): String {
        return when (status) {
            0 -> "Нет ответа"
            10 -> "Ошибка"
            20 -> "Принято"
            70 -> "Обработано"
            else -> "Неизвестный статус"
        }
    }

    private fun showAccountDeletionDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete receipt?")
            .setPositiveButton("Yes") { _, _ ->
                viewModel.deleteReceipt()

                val result = Bundle().apply { putBoolean("isDeleted", true) }
                parentFragmentManager.setFragmentResult("receiptUpdate", result)

                requireActivity().supportFragmentManager.popBackStack()
            }
            .setNegativeButton("No", null)
            .show()
    }
}
