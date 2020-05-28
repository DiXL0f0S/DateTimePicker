package ru.dixl0f0s.datetimepicker

import android.graphics.Color
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item.view.*

internal class Adapter(var items: List<String>, var listener: ItemClickListener) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        )

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.containerView.tvItem.setText(items[position])
    }

    inner class ViewHolder(val containerView: View) : RecyclerView.ViewHolder(containerView) {
        init {
            containerView.setOnClickListener { listener.onItemClick(adapterPosition) }
        }
    }
}