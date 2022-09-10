package com.lazykuna.folderarchiver

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.GsonBuilder
import com.lazykuna.folderarchiver.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private val MY_PERMISSION_ACCESS_ALL = 100

    lateinit var folderAdapter: ArchiveTaskDefAdapter
    var folderitems = mutableListOf<ArchiveTaskDefItem>()
    lateinit var dialog : DialogTaskDef

    val prefFile = "Preference"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val uri = Uri.parse("package:${BuildConfig.APPLICATION_ID}")
                startActivity(
                    Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        uri
                    )
                )
            }
        }

        var permissions = arrayOf(
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        var permissionFailed = false
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(this, permission) !=
                PackageManager.PERMISSION_GRANTED) {
                permissionFailed = true
                break
            }
        }
        if (permissionFailed) {
            ActivityCompat.requestPermissions(this, permissions, MY_PERMISSION_ACCESS_ALL)
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        // Event handler for creating new taskDefinition
        binding.fab.setOnClickListener { view ->
            dialog = DialogTaskDef(this, null) { newItem, del ->
                if (newItem == null || del) {
                    return@DialogTaskDef
                }
                folderitems.add(newItem)
                folderAdapter.notifyItemInserted(folderitems.size - 1)
                savePref()
            }
            dialog.setContentView(R.layout.dialog_taskdef)
            dialog.window!!.setLayout(WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT)

            // set some file dialog handler here
            // TODO File explorer won't show in modification as this not existin in adapter.
            dialog.findViewById<Button>(R.id.btn_srcdir).setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, 9998)
            }
            dialog.findViewById<Button>(R.id.btn_dstdir).setOnClickListener {
                val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE)
                startActivityForResult(intent, 9999)
            }

            dialog.show()
        }

        var listViewFolders = findViewById<RecyclerView>(R.id.list_folders)
        folderAdapter = ArchiveTaskDefAdapter(this)
        listViewFolders.adapter = folderAdapter

        // TODO: delete these and depreciate them (only for testing?)
//        folderitems.apply {
//            add(ArchiveTaskDefItem(text = "test1",
//                src_dir = "/sdcard/test/src",
//                dst_dir = "/sdcard/test/out",
//                archive_mode = "auto_monthly",
//                filter = "",
//                keep_file = true,
//                prefix = ""))
//            add(ArchiveTaskDefItem(text = "DC_Monthly",
//                src_dir = "/storage/3739-6132/Pictures/DC",
//                dst_dir = "/storage/3739-6132/Archives",
//                archive_mode = "auto_monthly",
//                filter = "",
//                keep_file = false,
//                prefix = ""))
//            add(ArchiveTaskDefItem(text = "Twitter_Monthly",
//                src_dir = "/mnt/sdcard/Pictures/Twitter",
//                dst_dir = "/storage/3739-6132/Archives",
//                archive_mode = "auto_monthly",
//                filter = "",
//                keep_file = false,
//                prefix = ""))
//            add(ArchiveTaskDefItem(text = "Download_Monthly",
//                src_dir = "/mnt/sdcard/Downloads",
//                dst_dir = "/storage/3739-6132/Archives",
//                archive_mode = "auto_monthly",
//                filter = "",
//                keep_file = false,
//                prefix = ""))
//        }

        loadPref()
        folderAdapter.items = folderitems
        folderAdapter.notifyDataSetChanged()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_by_copyright -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/kuna"))
                startActivity(browserIntent)
                true
            }
            R.id.action_readme -> {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://github.com/kuna/AndroidFolderArchiver"))
                startActivity(browserIntent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && dialog != null && dialog.isShowing) {
            data!!.data.also { uri ->
                when (requestCode) {
                    9998 -> {
                        dialog.findViewById<TextView>(R.id.text_srcdir).setText(uri.toString())
                    }
                    9999 -> {
                        dialog.findViewById<TextView>(R.id.text_dstdir).setText(uri.toString())
                    }
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode === MY_PERMISSION_ACCESS_ALL) {
            if (grantResults.size > 0) {
                for (grant in grantResults) {
                    if (grant != PackageManager.PERMISSION_GRANTED) System.exit(0)
                }
            }
        }
    }

    override fun onDestroy() {
        savePref()
        super.onDestroy()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        savePref()
        super.onWindowFocusChanged(hasFocus)
    }

    private fun loadPref() {
        folderitems.clear()

        val sharedPreferences = getSharedPreferences(prefFile, MODE_PRIVATE)
        val itemcount = sharedPreferences.getInt("itemcount", 0)
        val gson = GsonBuilder().create()
        for (i in 0 until itemcount) {
            val serializedData =  sharedPreferences.getString("item$i", "")
            if (serializedData == "") continue
            val archiveTaskDefData = gson.fromJson<ArchiveTaskDefItem>(serializedData,
                ArchiveTaskDefItem::class.java)
            folderitems.add(archiveTaskDefData)
        }
    }

    private fun savePref() {
        val sharedPreferences = getSharedPreferences(prefFile, MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val gson = GsonBuilder().create()
        editor.putInt("itemcount", folderitems.size)
        for (i in 0 until folderitems.size) {
            editor.putString("item$i", gson.toJson(folderitems[i]))
        }
        editor.commit()
        // TEST; remove this in prod
//        loadPref()
    }
}