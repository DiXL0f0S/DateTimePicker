package ru.dixl0f0s.datetimepicker

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item.view.*

internal class Adapter(var listener: ItemClickListener) :
    RecyclerView.Adapter<Adapter.ViewHolder>() {
    var items: MutableList<String> = mutableListOf()

    constructor(start: Int, end: Int, step: Int, listener: ItemClickListener) : this(listener) {
        items.add("")
        for (i in start..end) {
            if (i % step == 0) {
                var str = i.toString()
                if (str.length == 1)
                    str = "0$str"
                items.add(str)
            }
        }
        items.add("")
    }

    constructor(start: Int, end: Int, listener: ItemClickListener) : this(listener) {
        items.add("")
        for (i in start..end) {
            var str = i.toString()
            if (str.length == 1)
                str = "0$str"
            items.add(str)
        }
        items.add("")
    }

    constructor(items: MutableList<String>, listener: ItemClickListener) : this(listener) {
        this.items = items
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        )

    override fun getItemCount(): Int = items.size

    fun updateData(start: Int, end: Int, step: Int) {
        items.add("")
        for (i in start..end) {
            if (i % step == 0) {
                var str = i.toString()
                if (str.length == 1)
                    str = "0$str"
                items.add(str)
            }
        }
        items.add("")
        notifyDataSetChanged()
    }

    fun updateData(start: Int, end: Int) {
        items = mutableListOf()
        items.add("")
        for (i in start..end) {
            var str = i.toString()
            if (str.length == 1)
                str = "0$str"
            items.add(str)
        }
        items.add("")
        notifyDataSetChanged()
    }

    fun updateData(items: MutableList<String>) {
        this.items = items
        this.items.add(0, "")
        this.items.add("")
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.containerView.tvItem.text = items[position]
    }

    fun getItemPosition(item: String): Int {
        val itemName = if (item.length == 1) "0$item" else item
        items.forEachIndexed { i, s ->
            if (s.equals(itemName))
                return i
        }
        return 0
    }

    inner class ViewHolder(val containerView: View) : RecyclerView.ViewHolder(containerView) {
        init {
            containerView.setOnClickListener { listener.onItemClick(adapterPosition) }
        }
    }
}