package com.example.fireworkview.carosel

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.fireworkview.databinding.ListItemBinding

class ItemAdapter(val itemClick: (position: Int, item: Item) -> Unit) : RecyclerView.Adapter<ItemViewHolder>() {

    private var items: List<Item> = listOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder =
        ItemViewHolder(
            ListItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        holder.bind(items[position])
        holder.itemView.setOnClickListener {
            itemClick(position, items[position])
        }
    }

    override fun getItemCount() = items.size

    fun setItems(newItems: List<Item>) {
        items = newItems
        notifyDataSetChanged()
    }
}

class ItemViewHolder(private val view: ListItemBinding) : RecyclerView.ViewHolder(view.root) {
    fun bind(item: Item) {
        view.listItemText.text = "${item.title}"
        view.listItemIcon.setImageResource(item.icon)
    }
}
