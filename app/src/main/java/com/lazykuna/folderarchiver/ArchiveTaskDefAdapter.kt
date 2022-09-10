package com.lazykuna.folderarchiver

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ArchiveTaskDefAdapter(private val context: Context) : RecyclerView.Adapter<ArchiveTaskDefAdapter.ViewHolder>() {

    var items = mutableListOf<ArchiveTaskDefItem>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_archivetaskdef, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val text: TextView = itemView.findViewById(R.id.text_item)
        private val textSrc: TextView = itemView.findViewById(R.id.lvl_srcdir)
        private val textDst: TextView = itemView.findViewById(R.id.lvl_dstdir)

        fun updateWithItem(item: ArchiveTaskDefItem) {
            text.text = item.text
            textSrc.text = "Source: " + item.src_dir
            textDst.text = "Destination: " + item.dst_dir
//            Glide.with(itemView).load(item.img).into(imgProfile)
        }

        fun bind(item: ArchiveTaskDefItem) {
            updateWithItem(item)

            itemView.setOnClickListener {
                Intent(context, DryRunActivity::class.java).apply {
                    putExtra("data", item)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.run { context.startActivity(this) }
            }

            itemView.setOnLongClickListener {
                val dialog = DialogTaskDef(context, item) { newItem, del ->
                    if (newItem == null) {
                        if (del) {
                            for (i in 0 until items.size) {
                                if (items[i].text == item.text) {
                                    items.removeAt(i)
                                    notifyItemRemoved(i)
                                    break
                                }
                            }
                        }
                        return@DialogTaskDef
                    }
                    for (i in 0 until items.size) {
                        if (items[i].text == item.text) {
                            items[i] = newItem
                            updateWithItem(newItem)
                            notifyItemChanged(i)
                            break
                        }
                    }
                }
                dialog.setContentView(R.layout.dialog_taskdef)
                dialog.window!!.setLayout(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT)

                // TODO: set some file dialog handler here
                // File explorer won't show in modifcation mode due to this.

                dialog.show()
                return@setOnLongClickListener(true)
            }
        }
    }
}