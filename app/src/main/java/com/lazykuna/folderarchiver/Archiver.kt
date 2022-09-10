package com.lazykuna.folderarchiver

import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import kotlin.collections.ArrayList

// Archiver class does everything for archiving.
class Archiver {

    private var taskItems = mutableListOf<ArchiveTaskItem>()
    private var progress: Double = 0.0
    private var messages = mutableListOf<String>()
    private var isRunning = false

    fun SetArchiveTaskItems(items: ArrayList<ArchiveTaskItem>) {
        this.taskItems = items
    }

    fun GetArchiveTaskItems(): ArrayList<ArchiveTaskItem> {
        return ArrayList(this.taskItems)
    }

    fun validateTaskDef(taskDef : ArchiveTaskDefItem) : ArchiveTaskDefItem {
        var validatedTaskDef = taskDef

        // must have slash(separator) at the end of the dst_dir.
        var dstDir = taskDef.dst_dir
        if (!dstDir.endsWith("/")) {
            dstDir = "$dstDir/"
        }

        // check src/dst directory are valid.
        var srcFile = File(validatedTaskDef.src_dir)
        if (!srcFile.isDirectory) {
            throw(Exception("Source directory is invalid: " + validatedTaskDef.src_dir))
        }
        var dstFile = File(dstDir)
        if (!dstFile.isDirectory) {
            throw(Exception("Destination directory is invalid: $dstDir"))
        }

        // check archive_mode is valid.
        when (taskDef.archive_mode) {
            "daily" -> {} // do nothing
            "monthly" -> {} // do nothing
            "auto_monthly" -> {} // do nothing
            "all" -> {} // do nothing
            else -> throw(Exception("Unexpected archive mode " + taskDef.archive_mode))
        }

        // check prefix is empty. If so, use src_dir.
        var prefix = validatedTaskDef.prefix
        if (prefix == "") {
            val dirs = validatedTaskDef.src_dir.split('/')
            prefix = dirs[dirs.size - 1]
        }

        return ArchiveTaskDefItem(
            text = taskDef.text,
            src_dir = taskDef.src_dir,
            dst_dir = dstDir,
            archive_mode = taskDef.archive_mode,
            filter = taskDef.filter,
            keep_file = taskDef.keep_file,
            prefix = prefix
        )
    }

    // DryRun runs dry run that creating archiveTaskItems
    fun DryRun(taskDef: ArchiveTaskDefItem) {
        // init progress
        initProgress()
        taskItems.clear()

        var taskList = ArrayList<ArchiveTaskItem>()
        var fds = mutableListOf<FileAndDate>()

        // Get file absolute path and mod date
        File(taskDef.src_dir).walk().sortedBy{ it.isDirectory }.forEach {
            // Don't archive directory or hidden file
            if (!it.isFile || it.isHidden ) {
                return@forEach
            }
            var fd = FileAndDate(it.absolutePath, it.lastModified())
            fds.add(fd)
        }
        if (fds.size == 0) {
            return
        }

        // Sort by date, ascending
        var fdsSorted = fds.sortedWith(compareBy<FileAndDate> { it.date })

        // test logger
        val logger = Logger.getLogger("ArchiverVerifyTest")

        // prepare filters
        var filters = emptyList<String>()
        if (taskDef.filter != "") {
            filters = taskDef.filter.split(';').map { it.uppercase() }
            // first check the file fits filter before going into main task loop
            fdsSorted = fdsSorted.filter { fd ->
                val f = File(fd.path)
                return@filter filters.indexOf(f.extension.uppercase()) >= 0
            }
        }

        // create task lists
        var currSrcFiles = mutableListOf<String>()
        for (i in fdsSorted.indices) {
            // update status
            var splitTask = false
            var currUnixDate = fdsSorted[i].date
            var nextUnixDate: Long = 0
            if (i < fdsSorted.size - 1) {
                nextUnixDate = fdsSorted[i + 1].date
            } else {
                // if last loop... Must do split.
                splitTask = true
            }
            currSrcFiles.add(fdsSorted[i].path)

            // Check is it a condition to split.
            when (taskDef.archive_mode) {
                "daily" -> {
                    var currDate = Date(currUnixDate)
                    var nextDate = Date(nextUnixDate)
                    var format = SimpleDateFormat("yyyyMMdd")
                    if (format.format(currDate) != format.format(nextDate)) {
                        splitTask = true
                    }
                }
                "monthly" -> {
                    var currDate = Date(currUnixDate)
                    var nextDate = Date(nextUnixDate)
                    var format = SimpleDateFormat("yyyyMM")
                    if (format.format(currDate) != format.format(nextDate)) {
                        splitTask = true
                    }
                }
                "auto_monthly" -> {
                    var currDate = Date(currUnixDate)
                    var nextDate = Date(nextUnixDate)
                    var format = SimpleDateFormat("yyyyMM")
                    if (format.format(currDate) != format.format(nextDate) &&
                        currSrcFiles.size >= 500) {
                        splitTask = true
                    }
                }
                "all" -> {
                    // Do nothing; don't split a task.
                }
                else -> throw(java.lang.Exception("Unexpected archive mode " + taskDef.archive_mode))
            }

            // Do split task
            if (splitTask) {
                // dst_file name should be named like:
                // Twitter_(lastmodifiedDate).zip
                var currDate = Date(currUnixDate)
                var format = SimpleDateFormat("yyyyMMdd")
                var dstFile = taskDef.dst_dir + taskDef.prefix + "_" + format.format(currDate) +
                        ".zip"

                taskList.add(ArchiveTaskItem(
                    dst_file = dstFile,
                    count = currSrcFiles.size,
                    size = 0,
                    src_dir = taskDef.src_dir,
                    src_files = ArrayList(currSrcFiles),
                    keep_files = taskDef.keep_file,
                ))

                currSrcFiles.clear()
            }
        }

        logger.log(Level.INFO, "Total file count: ${taskList.size}")
        taskItems = taskList
    }

    private data class FileAndDate (
        val path : String,
        val date: Long,
    )

    // Execute executes taskItems currently loaded.
    fun Execute() {
        // init progress
        initProgress()

        var totalCount = taskItems.size
        var processedTaskCount = 0
        for (taskItem in taskItems) {
            var filename = taskItem.dst_file
            var filenameCount = 0

            // break if canceled
            if (!isRunning) break

            // attempt until there is no file
            while (File(filename).exists()) {
                filenameCount++
                var t = taskItem.dst_file
                filename = t.split('.', limit=2)[0] + " ($filenameCount) .zip"
            }

            // create zip file and output stream
            var totalFileCount = taskItem.src_files.size
            var processedFileCount = 0
            var zipOut = ZipOutputStream(BufferedOutputStream(FileOutputStream(filename))).use {
                output -> taskItem.src_files.forEach {
                    val inputFile = File(it)

                    // create zip entry
                    val entry = ZipEntry(inputFile.name)
                    output.putNextEntry(entry)

                    // read and add to zip
                    FileInputStream(inputFile).use { stream ->
                        BufferedInputStream(stream).use { iStream ->
                            iStream.copyTo(output)
                        }
                    }

                    // update progress
                    processedFileCount++
                    progress = 1.0 / this.taskItems.size *
                            (processedTaskCount + processedFileCount.toDouble() / taskItem.src_files.size)
                    if (processedFileCount as Int % 10 == 0) {
                        messages.add("File compressing: $processedFileCount/$totalFileCount")
                    }
                }
            }

            messages.add("Archive process done.")

            // Now delete current file
            if (!taskItem.keep_files) {
                for (filepath in taskItem.src_files) {
                    if (!File(filepath).delete()) {
                        messages.add("Error: deleting $filepath file failed.")
                    }
                }
            }

            processedTaskCount++
            messages.add("[$processedTaskCount/$totalCount] File $filename is complete.")
        }

        // finished
        isRunning = false
        messages.add("Archiving all done.")
    }

    // Cancel cancels current running process if exists.
    fun Cancel() {
        isRunning = false
    }

    // init progress
    private fun initProgress() {
        messages.clear()
        progress = 0.0
        isRunning = true
    }

    data class Progress (
        var progress: Double,
        var messages: ArrayList<String>,
        var isRunning: Boolean
    )

    // GetProgress returns current progress and messages.
    fun GetProgress() : Progress {
        var p = Progress(
            progress, ArrayList(messages), isRunning
        )
        // clear after returning
        messages.clear()
        return p
    }

    // IsRunning returns is executing
    fun IsRunning(): Boolean {
        return isRunning
    }
}