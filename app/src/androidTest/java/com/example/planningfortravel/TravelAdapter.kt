package com.example.planningfortravel

import android.net.Uri
import android.view.ContextMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TravelAdapter(
    private var items: MutableList<TravelRecord>,
    private val onClick: (TravelRecord) -> Unit,
    private val onEdit: (TravelRecord) -> Unit,
    private val onDelete: (TravelRecord) -> Unit
) : RecyclerView.Adapter<TravelAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
        val imgThumb: ImageView = itemView.findViewById(R.id.imgThumb)
        val tvPlace: TextView = itemView.findViewById(R.id.tvPlace)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)

        init {
            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(menu: ContextMenu, v: View, menuInfo: ContextMenu.ContextMenuInfo?){
            val pos = adapterPosition
            if (pos == RecyclerView.NO_ID.toInt()) return
            val record = items[pos]
            menu.setHeaderTitle(record.place)
            menu.add(0,MENU_EDIT, 0, "수정").setOnMenuItemClickListener { onEdit(record); true }
            menu.add(0,MENU_DELETE, 0, "삭제").setOnMenuItemClickListener { onDelete(record); true }
        }
    }

    companion object {
        const val MENU_EDIT = 1001
        const val MENU_DELETE = 1002
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_travel, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvPlace.text = item.place
        holder.tvDate.text = item.visitDate
        if (item.photoUri.isNotEmpty()) {
            try{
                holder.imgThumb.setImageURI(Uri.parse(item.photoUri))
            } catch (e: Exception) {
                holder.imgThumb.setImageResource(R.drawable.ic_placeholder)
            }
        } else {
            holder.imgThumb.setImageResource(R.drawable.ic_placeholder)
        }
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<TravelRecord>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}