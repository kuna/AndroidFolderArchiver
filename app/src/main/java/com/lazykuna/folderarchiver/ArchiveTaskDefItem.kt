package com.lazykuna.folderarchiver

import android.net.Uri
import java.io.Serializable

data class ArchiveTaskDefItem (
    // text is a title of the given archiving work shown to user.
    val text: String,

    // src_dir is a source directory to be archived.
    // TODO: change it to Uri
    val src_dir: String,

    // dst_dir is destination directory.
    // TODO: change it to Uri
    val dst_dir: String,

    // archive mode is a standard for archive, e.g. daily, monthly, auto_per_1000, all
    val archive_mode: String,

    // filter is for archiving filter by extension. e.g. jpg;png
    // by default is empty, which means targeting for all files.
    val filter: String,

    // keep_file does not deletes original file after archiving if turned on.
    val keep_file: Boolean,

    // prefix is a prefix for all archive files. Empty prefix is set as src folder name by default.
    val prefix: String
) : Serializable {
    constructor() : this("", "", "", "", "", false, "")
}