package com.lazykuna.folderarchiver

import java.io.Serializable

data class ArchiveTaskItem (
    // archive destination file
    val dst_file : String,

    // total count of the files to be archived
    val count : Number,

    // total size of the files to be archived
    val size : Number,

    // archive source directory
    val src_dir : String,

    // files to be archived
    val src_files : ArrayList<String>,

    // should keep original files?
    val keep_files: Boolean,
) : Serializable