package com.lazykuna.folderarchiver

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchiveLogAdapter(private val context: Context) : RecyclerView.Adapter<ArchiveLogAdapter.ViewHolder>() {

    var items = mutableListOf<ArchiveLogItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArchiveLogAdapter.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_logtext, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ArchiveLogAdapter.ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val logText: TextView = itemView.findViewById(R.id.text_log)

        fun bind(item: ArchiveLogItem) {
            logText.text = item.text
        }
    }
}