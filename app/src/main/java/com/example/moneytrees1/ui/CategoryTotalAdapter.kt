package com.example.moneytrees1.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.moneytrees1.R
import java.text.NumberFormat

class CategoryTotalAdapter(private val numberFormat: NumberFormat) :
    RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    private val categoryTotals = mutableListOf<Pair<String, Double>>()

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryName: TextView = itemView.findViewById(R.id.tv_category_name)
        val totalAmount: TextView = itemView.findViewById(R.id.tv_total_amount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val (category, total) = categoryTotals[position]
        holder.categoryName.text = category
        holder.totalAmount.text = numberFormat.format(total)
    }

    override fun getItemCount(): Int = categoryTotals.size

    fun updateData(newData: Map<String, Double>) {
        categoryTotals.clear()
        categoryTotals.addAll(newData.toList())
        notifyDataSetChanged()
    }
}
