package com.lazykuna.folderarchiver

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.widget.*
import java.io.File
import java.util.logging.Level
import java.util.logging.Logger


class DialogTaskDef(private val ctx: Context, existingTaskDef: ArchiveTaskDefItem?,
                    callBackAfterCreate: (ArchiveTaskDefItem?, Boolean) -> Unit) : Dialog(ctx) {

    private val existingTaskDef = existingTaskDef
    private val cb = callBackAfterCreate

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Add some selection
        findViewById<Spinner>(R.id.spinner_archive_mode).adapter =
                ArrayAdapter.createFromResource(ctx, R.array.archive_mode_list,
                    android.R.layout.simple_spinner_item)

        // register some handlers (buttons)
        // Note: folder picker button are declared in parent(MainActivity) by now
        // TODO: get parameter as parent activity and move handler to here
        findViewById<Button>(R.id.btn_add_nomedia).setOnClickListener { view ->
            val taskDef = createTaskDef()
            var srcDir = taskDef.src_dir
            if (!srcDir.endsWith("/")) {
                srcDir = "$srcDir/"
            }
            val nomediaPath = "${srcDir}.nomedia"
            if (File(nomediaPath).exists()) {
                Toast.makeText(ctx, "nomedia Already exists", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            } else if (!File(srcDir).isDirectory) {
                Toast.makeText(ctx, "Source directory is not a directory, is it exists?",
                    Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            try {
                if (!File(nomediaPath).createNewFile()) {
                    Toast.makeText(ctx, "Failed to create nomedia file", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(ctx, "Created nomedia file", Toast.LENGTH_SHORT).show()
                }
            } catch (e:Exception) {
                Toast.makeText(ctx, "Exception while creating nomedia file", Toast.LENGTH_SHORT).show()
                val logger = Logger.getLogger("ArchiverDialog")
                logger.log(Level.SEVERE, "Exception: ${e.toString()}, StackTrace: ${e.stackTraceToString()}")
            }
        }
        findViewById<Button>(R.id.btn_ok).setOnClickListener { view ->
            val taskDef = createTaskDef()
            this.cb(taskDef, false)
            dismiss()
        }
        findViewById<Button>(R.id.btn_cancel).setOnClickListener { view ->
            this.cb(null, false)
            dismiss()
        }
        findViewById<Button>(R.id.btn_delete).setOnClickListener { view ->
            this.cb(null, true)
            dismiss()
        }

        // fill if modifying
        if (existingTaskDef != null) {
            fillWithTaskDef(existingTaskDef!!)
        }
    }

    private fun fillWithTaskDef(taskDef : ArchiveTaskDefItem) {
        findViewById<TextView>(R.id.text_taskdef_name).setText(taskDef.text)
        findViewById<TextView>(R.id.text_dstdir).setText(taskDef.dst_dir)
        findViewById<TextView>(R.id.text_srcdir).setText(taskDef.src_dir)
        findViewById<TextView>(R.id.text_filter).setText(taskDef.filter)
        findViewById<TextView>(R.id.text_prefix).setText(taskDef.prefix)
        findViewById<CheckBox>(R.id.cb_keep_file).isChecked = taskDef.keep_file

        val spinner = findViewById<Spinner>(R.id.spinner_archive_mode)
        for (i in 0 until spinner.count) {
            if (spinner.getItemAtPosition(i).toString() == taskDef.archive_mode) {
                spinner.setSelection(i)
                break
            }
        }
    }

    private fun createTaskDef() : ArchiveTaskDefItem {
        val name = findViewById<TextView>(R.id.text_taskdef_name).text.toString()
        val dst_dir = findViewById<TextView>(R.id.text_dstdir).text.toString()
        val src_dir = findViewById<TextView>(R.id.text_srcdir).text.toString()
        val filter = findViewById<TextView>(R.id.text_filter).text.toString()
        val prefix = findViewById<TextView>(R.id.text_prefix).text.toString()
        val keep_file = findViewById<CheckBox>(R.id.cb_keep_file).isChecked

        val spinner = findViewById<Spinner>(R.id.spinner_archive_mode)
        val archive_mode = spinner.selectedItem.toString()

        return ArchiveTaskDefItem(
            text = name,
            dst_dir = dst_dir,
            src_dir = src_dir,
            filter = filter,
            prefix = prefix,
            keep_file =  keep_file,
            archive_mode = archive_mode,
        )
    }
}