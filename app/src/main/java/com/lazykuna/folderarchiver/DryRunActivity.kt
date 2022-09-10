package com.lazykuna.folderarchiver

import android.content.ContextWrapper
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import java.util.*


class DryRunActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dryrun)

        val archiveTaskDefData = intent.getSerializableExtra("data") as ArchiveTaskDefItem
        var validatedTaskDefData = archiveTaskDefData

        // Scan disk and create ArchiveTaskItems from them
        var taskItems = mutableListOf<ArchiveTaskItem>()
        try {
            var archiver = Archiver()
            validatedTaskDefData = archiver.validateTaskDef(archiveTaskDefData)
            archiver.DryRun(validatedTaskDefData)
            taskItems = archiver.GetArchiveTaskItems()
        } catch (e: Exception) {
            showErrorAndExit(e.toString())
        }

        // Update activity
        var textArchiveName = findViewById<TextView>(R.id.text_archive_name)
        textArchiveName.text = "Archive task of " + archiveTaskDefData.text
        var archiveTaskList = findViewById<RecyclerView>(R.id.archive_item_list)
        var archiveTaskAdapter = ArchiveTaskAdapter(this)
        archiveTaskList.adapter = archiveTaskAdapter
        archiveTaskAdapter.items = taskItems
        archiveTaskAdapter.notifyDataSetChanged()

        // Add handler
        var btnStartArchive = findViewById<Button>(R.id.btn_start_archive)
        btnStartArchive.setOnClickListener {
            // NOTE: should not pass the whole ArchiveTasks
            // as it's whole size could be extremely large and takes more time than reprocessing
            // dry-run. Even the application could be crashed.
            // Hence, just pass TaskDef and reprocess dry-run.
//            Intent(this, ArchiveActivity::class.java).apply {
//                putExtra("count", taskItems.size)
//                for (i in taskItems.indices) {
//                    putExtra("data$i", taskItems[i])
//                }
//                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            }.run { startActivity(this) }
            Intent(this, ArchiveActivity::class.java).apply {
                putExtra("data", validatedTaskDefData)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }.run { startActivity(this) }
            finish()
        }
    }

    // showErrorAndExit show error and exit this activity.
    private fun showErrorAndExit(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
        finish()
    }
}