package com.lazykuna.folderarchiver

import android.os.Bundle
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import kotlin.collections.ArrayList
import kotlin.concurrent.timer

class ArchiveActivity: AppCompatActivity() {

    lateinit var adapter: ArchiveLogAdapter
    var logitems = mutableListOf<ArchiveLogItem>()
    var archiver = Archiver()
    lateinit var timerTask : Timer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_archive)

        // add basic handlers
        val btn = findViewById<Button>(R.id.btn_archive_close)
        btn.setOnClickListener {
            timerTask.cancel()
            val isRunning = archiver.IsRunning()
            if (isRunning) {
                Toast.makeText(this, "Running, can't close now", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            finish()
        }

        // read tasks
        var archiveTaskDefData = intent.getSerializableExtra("data") as ArchiveTaskDefItem

        // init log listview
        val listViewLogs = findViewById<RecyclerView>(R.id.list_archivelog)
        adapter = ArchiveLogAdapter(this)
        listViewLogs.adapter = adapter
        adapter.items = logitems

        // read datas
        try {
            archiver.DryRun(archiveTaskDefData)
            logitems.add(ArchiveLogItem("Archiving list-up done."))
        } catch (e: Exception) {
            logitems.add(ArchiveLogItem(e.toString()))
            val logger = Logger.getLogger("Archiver")
            logger.log(Level.SEVERE,
                "Error ${e.toString()}, Stacktrace ${e.stackTraceToString()}")
        }
        adapter.notifyDataSetChanged()

        // get UI progress task (timer)
        timerTask = kotlin.concurrent.timer(period = 500) {
            val p = archiver.GetProgress()
            val progress = findViewById<ProgressBar>(R.id.progress_archive)

            runOnUiThread {
                var beforeCount = logitems.size
                for (t in p.messages) {
                    logitems.add(ArchiveLogItem(t))
                }
                adapter.notifyItemInserted(beforeCount)
                listViewLogs.scrollToPosition(logitems.size - 1)

                progress.setProgress((p.progress * 100).toInt())
            }
        }

        // start task
        Thread {
            try {
                archiver.Execute()
            } catch (e: Exception) {
                val beforeCount = logitems.size
                logitems.add(ArchiveLogItem(e.toString()))
                adapter.notifyItemInserted(beforeCount)
                val logger = Logger.getLogger("Archiver")
                logger.log(Level.SEVERE,
                    "Error ${e.toString()}, Stacktrace ${e.stackTraceToString()}")
                listViewLogs.scrollToPosition(logitems.size - 1)
            }
        }.start()
    }

    override fun onDestroy() {
        timerTask.cancel()
        archiver.Cancel()

        // Can't destroy while running
        if (archiver.IsRunning()) {
            Toast.makeText(this, "Running, can't close now", Toast.LENGTH_SHORT).show()
            return
        }

        super.onDestroy()
    }
}