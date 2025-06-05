package com.example.moneytrees1.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.moneytrees1.R
import com.example.moneytrees1.data.ExpenseEntity
import java.text.NumberFormat

class ExpenseAdapter(private val currencyFormat: NumberFormat) :
    ListAdapter<ExpenseEntity, ExpenseAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.tv_expense_name) // Changed from name to description
        val amount: TextView = view.findViewById(R.id.tv_amount)
        val category: TextView = view.findViewById(R.id.tv_category)
        val date: TextView = view.findViewById(R.id.tv_date)
        val image: ImageView = view.findViewById(R.id.iv_expense_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = getItem(position)
        holder.apply {
            // Use description instead of name
            description.text = expense.description ?: "No description"
            amount.text = currencyFormat.format(expense.amount)
            category.text = expense.category
            date.text = expense.date

            // Handle image loading safely
            expense.imagePath?.let { path ->
                if (path.isNotBlank()) {
                    image.visibility = View.VISIBLE
                    Glide.with(itemView.context)
                        .load(path)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .placeholder(R.drawable.ic_image_placeholder)
                        .error(R.drawable.ic_broken_image)
                        .into(image)
                } else {
                    image.visibility = View.GONE
                    Glide.with(itemView.context).clear(image)
                }
            } ?: run {
                image.visibility = View.GONE
                Glide.with(itemView.context).clear(image)
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseEntity>() {
        override fun areItemsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean {
            return oldItem == newItem
        }
    }
}