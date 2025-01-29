package com.example.qrscanner.history

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.qrscanner.R
import com.example.qrscanner.database.AppDatabase
import com.example.qrscanner.database.ReceiptEntity
import kotlinx.coroutines.launch

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private val viewModel: HistoryViewModel by viewModels {
        HistoryViewModelFactory(AppDatabase.getInstance(requireContext()), requireContext())
    }

    private lateinit var adapter: HistoryAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        parentFragmentManager.setFragmentResultListener(
            "receiptUpdate", this) { _, bundle ->
            if (bundle.getBoolean("isDeleted")) {
                refreshReceipts()
            }
        }

        val recyclerView = view.findViewById<RecyclerView>(R.id.historyRecyclerView)
        adapter = HistoryAdapter { receipt ->
            openReceiptDetails(receipt)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        // Подписываемся на изменения списка чеков
        lifecycleScope.launch {
            viewModel.receipts.collect { receipts ->
                adapter.submitList(receipts)
            }
        }
    }

    private fun refreshReceipts() {
        // Обновляем список чеков
        viewModel.loadReceipts()
    }

    private fun openReceiptDetails(receipt: ReceiptEntity) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, ReceiptDetailsFragment.newInstance(receipt.id))
            .addToBackStack(null)
            .commit()
    }
}
