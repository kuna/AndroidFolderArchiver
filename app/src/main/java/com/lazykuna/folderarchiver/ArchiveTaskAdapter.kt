package com.lazykuna.folderarchiver

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchiveTaskAdapter(private val context: Context) : RecyclerView.Adapter<ArchiveTaskAdapter.ViewHolder>() {

    var items = mutableListOf<ArchiveTaskItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveTaskAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_archivetask, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ArchiveTaskAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val dstText: TextView = itemView.findViewById(R.id.text_dst)
        private val dstDescText: TextView = itemView.findViewById(R.id.text_dst_desc)

        fun bind(item: ArchiveTaskItem) {
            dstText.text = item.dst_file
            dstDescText.text = "Count " + item.count
        }
    }
}