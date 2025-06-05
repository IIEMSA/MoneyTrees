package com.example.moneytrees1.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytrees1.R
import java.text.NumberFormat

class CategoryTotalAdapter(
    private val currencyFormat: NumberFormat
) : RecyclerView.Adapter<CategoryTotalAdapter.TotalViewHolder>() {

    private val data = mutableListOf<Pair<String, Double>>()

    fun updateData(categoryTotals: Map<String, Double>) {
        data.clear()
        data.addAll(categoryTotals.entries.map { it.key to it.value })
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TotalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return TotalViewHolder(view)
    }

    override fun getItemCount() = data.size

    override fun onBindViewHolder(holder: TotalViewHolder, position: Int) {
        val (category, total) = data[position]
        holder.categoryName.text = category
        holder.totalAmount.text = currencyFormat.format(total)
        holder.categoryTotalType.text = "Category Total"  // static, or you can remove/set to "" if you wish
    }

    class TotalViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryName: TextView = view.findViewById(R.id.tv_category_name)
        val totalAmount: TextView = view.findViewById(R.id.tv_total_amount)
        val categoryTotalType: TextView = view.findViewById(R.id.tv_category_total)
    }
}